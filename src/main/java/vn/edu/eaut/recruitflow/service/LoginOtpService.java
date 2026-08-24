package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.LoginOtpDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.LoginOtp;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Optional email-based second factor after a valid password has been entered. */
public class LoginOtpService {
    public static final int OTP_EXPIRY_MINUTES = 10;
    public static final int OTP_MAX_ATTEMPTS = 5;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;

    private final UserDAO userDAO;
    private final LoginOtpDAO otpDAO;
    private final GmailOtpMailSender mailSender;
    private final LoginOtpConfiguration configuration;
    private final SecureRandom secureRandom = new SecureRandom();

    public LoginOtpService() {
        this(new UserDAO(), new LoginOtpDAO(), new GmailOtpMailSender());
    }

    LoginOtpService(UserDAO userDAO, LoginOtpDAO otpDAO, GmailOtpMailSender mailSender) {
        this.userDAO = userDAO;
        this.otpDAO = otpDAO;
        this.mailSender = mailSender;
        this.configuration = LoginOtpConfiguration.load(MailConfiguration.load());
    }

    public boolean isRequired() {
        return configuration.isRequired();
    }

    public boolean isMisconfigured() {
        return configuration.isMisconfigured();
    }

    public void requestOtp(User authenticatedPasswordUser) throws BusinessException {
        if (!isRequired()) {
            throw new BusinessException("Xác minh OTP đăng nhập hiện không được bật.");
        }
        if (authenticatedPasswordUser == null || authenticatedPasswordUser.getId() <= 0
                || !UserStatus.ACTIVE.name().equals(authenticatedPasswordUser.getStatus())) {
            throw new BusinessException("Tài khoản không còn khả dụng để xác minh đăng nhập.");
        }
        String otp = generateOtp();
        LoginOtp issuedOtp = issueOtp(authenticatedPasswordUser.getId(), otp);
        try {
            mailSender.sendLoginOtp(authenticatedPasswordUser.getEmail(), authenticatedPasswordUser.getFullName(), otp,
                    OTP_EXPIRY_MINUTES);
        } catch (BusinessException exception) {
            try {
                otpDAO.consume(issuedOtp.getId());
            } catch (SQLException ignored) {
                // Preserve the user-safe mail error and never leave a usable code after delivery failed.
            }
            throw exception;
        }
    }

    /** Re-checks user status before accepting a code and returns the freshly loaded account. */
    public User verifyOtp(int userId, String otp) throws BusinessException {
        if (userId <= 0) {
            throw new BusinessException("Phiên xác minh đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
        }
        String normalizedOtp = AuthValidation.otp(otp);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User user = userDAO.findById(connection, userId);
                if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
                    throw invalidOtp();
                }
                LoginOtp currentOtp = otpDAO.findLatestActiveForUpdate(connection, userId);
                if (currentOtp == null) {
                    throw invalidOtp();
                }
                if (currentOtp.getAttemptCount() >= OTP_MAX_ATTEMPTS) {
                    otpDAO.consume(connection, currentOtp.getId());
                    connection.commit();
                    throw new BusinessException("Mã OTP đã vượt quá số lần thử. Vui lòng đăng nhập lại.");
                }
                if (!PasswordUtil.matches(normalizedOtp, currentOtp.getOtpHash())) {
                    int attempts = currentOtp.getAttemptCount() + 1;
                    otpDAO.recordFailedAttempt(connection, currentOtp.getId(), attempts);
                    if (attempts >= OTP_MAX_ATTEMPTS) {
                        otpDAO.consume(connection, currentOtp.getId());
                        connection.commit();
                        throw new BusinessException("Mã OTP không đúng. Bạn đã hết số lần thử, vui lòng đăng nhập lại.");
                    }
                    connection.commit();
                    throw invalidOtp();
                }
                otpDAO.consume(connection, currentOtp.getId());
                connection.commit();
                return user;
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể xác minh mã OTP đăng nhập lúc này.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác minh mã OTP đăng nhập lúc này.", exception);
        }
    }

    private LoginOtp issueOtp(int userId, String otp) throws BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Timestamp lastIssuedAt = otpDAO.findMostRecentCreatedAtForUpdate(connection, userId);
                Instant now = Instant.now();
                if (lastIssuedAt != null && lastIssuedAt.toInstant().isAfter(now.minusSeconds(OTP_RESEND_COOLDOWN_SECONDS))) {
                    throw new BusinessException("Bạn vừa nhận mã OTP. Vui lòng chờ khoảng 1 phút trước khi gửi lại.");
                }
                otpDAO.invalidateOutstanding(connection, userId);
                LoginOtp loginOtp = new LoginOtp();
                loginOtp.setUserId(userId);
                loginOtp.setOtpHash(PasswordUtil.hash(otp));
                loginOtp.setExpiresAt(Timestamp.from(now.plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES)));
                loginOtp.setAttemptCount(0);
                otpDAO.insert(connection, loginOtp);
                connection.commit();
                return loginOtp;
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể tạo mã OTP đăng nhập lúc này.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tạo mã OTP đăng nhập lúc này.", exception);
        }
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private BusinessException invalidOtp() {
        return new BusinessException("Mã OTP không đúng hoặc đã hết hạn.");
    }
}
