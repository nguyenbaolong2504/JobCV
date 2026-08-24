package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.PasswordResetOtpDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.PasswordResetOtp;
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

/** Password reset workflow: rate-limited issue, hashed OTP verification, then BCrypt password update. */
public class PasswordResetService {
    public static final int OTP_EXPIRY_MINUTES = 10;
    public static final int OTP_MAX_ATTEMPTS = 5;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;

    private final UserDAO userDAO;
    private final PasswordResetOtpDAO otpDAO;
    private final GmailOtpMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService() {
        this(new UserDAO(), new PasswordResetOtpDAO(), new GmailOtpMailSender());
    }

    PasswordResetService(UserDAO userDAO, PasswordResetOtpDAO otpDAO, GmailOtpMailSender mailSender) {
        this.userDAO = userDAO;
        this.otpDAO = otpDAO;
        this.mailSender = mailSender;
    }

    public boolean isAvailable() {
        return mailSender.isAvailable();
    }

    /**
     * Returns false for an unknown or inactive email, deliberately allowing the controller to use
     * an enumeration-safe response. A configured SMTP service is required before looking up users.
     */
    public boolean requestOtp(String email) throws BusinessException {
        String normalizedEmail = AuthValidation.email(email);
        if (!mailSender.isAvailable()) {
            throw new BusinessException("Khôi phục mật khẩu hiện chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        }

        User user;
        try {
            user = userDAO.findByEmail(normalizedEmail);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xử lý yêu cầu khôi phục mật khẩu lúc này.", exception);
        }
        if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
            return false;
        }

        String otp = generateOtp();
        PasswordResetOtp issuedOtp = issueOtp(user.getId(), otp);
        try {
            mailSender.sendPasswordResetOtp(user.getEmail(), user.getFullName(), otp, OTP_EXPIRY_MINUTES);
            return true;
        } catch (BusinessException exception) {
            try {
                otpDAO.consume(issuedOtp.getId());
            } catch (SQLException ignored) {
                // The already expired/invalid OTP is never exposed; preserve the original mail error.
            }
            throw exception;
        }
    }

    /** Returns the verified local user ID. The OTP is consumed on success. */
    public int verifyOtp(String email, String otp) throws BusinessException {
        String normalizedEmail = AuthValidation.email(email);
        String normalizedOtp = AuthValidation.otp(otp);

        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User user = userDAO.findByEmail(connection, normalizedEmail);
                if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
                    throw invalidOtp();
                }

                PasswordResetOtp currentOtp = otpDAO.findLatestActiveForUpdate(connection, user.getId());
                if (currentOtp == null) {
                    throw invalidOtp();
                }
                if (currentOtp.getAttemptCount() >= OTP_MAX_ATTEMPTS) {
                    otpDAO.consume(connection, currentOtp.getId());
                    connection.commit();
                    throw new BusinessException("Mã OTP đã vượt quá số lần thử. Vui lòng yêu cầu mã mới.");
                }
                if (!PasswordUtil.matches(normalizedOtp, currentOtp.getOtpHash())) {
                    int attempts = currentOtp.getAttemptCount() + 1;
                    otpDAO.recordFailedAttempt(connection, currentOtp.getId(), attempts);
                    if (attempts >= OTP_MAX_ATTEMPTS) {
                        otpDAO.consume(connection, currentOtp.getId());
                        connection.commit();
                        throw new BusinessException("Mã OTP không đúng. Bạn đã hết số lần thử, vui lòng yêu cầu mã mới.");
                    }
                    connection.commit();
                    throw invalidOtp();
                }

                otpDAO.consume(connection, currentOtp.getId());
                connection.commit();
                return user.getId();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể xác minh mã OTP lúc này.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác minh mã OTP lúc này.", exception);
        }
    }

    public void resetPassword(int userId, String newPassword) throws BusinessException {
        if (userId <= 0) {
            throw new BusinessException("Phiên đặt lại mật khẩu không hợp lệ. Vui lòng yêu cầu mã OTP mới.");
        }
        String validatedPassword = AuthValidation.newPassword(newPassword);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User user = userDAO.findById(connection, userId);
                if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
                    throw new BusinessException("Tài khoản không còn khả dụng để đặt lại mật khẩu.");
                }
                if (!userDAO.updatePassword(connection, userId, PasswordUtil.hash(validatedPassword))) {
                    throw new BusinessException("Không thể đặt lại mật khẩu lúc này.");
                }
                otpDAO.invalidateOutstanding(connection, userId);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể đặt lại mật khẩu lúc này.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đặt lại mật khẩu lúc này.", exception);
        }
    }

    private PasswordResetOtp issueOtp(int userId, String otp) throws BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Timestamp lastIssuedAt = otpDAO.findMostRecentCreatedAtForUpdate(connection, userId);
                Instant now = Instant.now();
                if (lastIssuedAt != null && lastIssuedAt.toInstant().isAfter(now.minusSeconds(OTP_RESEND_COOLDOWN_SECONDS))) {
                    throw new BusinessException("Bạn vừa yêu cầu mã OTP. Vui lòng chờ khoảng 1 phút trước khi gửi lại.");
                }
                otpDAO.invalidateOutstanding(connection, userId);
                PasswordResetOtp resetOtp = new PasswordResetOtp();
                resetOtp.setUserId(userId);
                resetOtp.setOtpHash(PasswordUtil.hash(otp));
                resetOtp.setExpiresAt(Timestamp.from(now.plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES)));
                resetOtp.setAttemptCount(0);
                otpDAO.insert(connection, resetOtp);
                connection.commit();
                return resetOtp;
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể tạo mã OTP lúc này.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tạo mã OTP lúc này.", exception);
        }
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private BusinessException invalidOtp() {
        return new BusinessException("Mã OTP không đúng hoặc đã hết hạn.");
    }
}
