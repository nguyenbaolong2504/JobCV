package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Sends a password-reset OTP through a Gmail-compatible STARTTLS SMTP server. */
public class GmailOtpMailSender {
    private final MailConfiguration configuration;

    public GmailOtpMailSender() {
        this(MailConfiguration.load());
    }

    GmailOtpMailSender(MailConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean isAvailable() {
        return configuration.isEnabled();
    }

    public String unavailableReason() {
        return configuration.getDisabledReason();
    }

    public void sendPasswordResetOtp(String recipient, String fullName, String otp, int expiryMinutes)
            throws BusinessException {
        sendOtp(recipient, fullName, otp, expiryMinutes,
                "Mã OTP đặt lại mật khẩu RecruitFlow",
                "đặt lại mật khẩu");
    }

    public void sendLoginOtp(String recipient, String fullName, String otp, int expiryMinutes)
            throws BusinessException {
        sendOtp(recipient, fullName, otp, expiryMinutes,
                "Mã OTP đăng nhập RecruitFlow",
                "hoàn tất đăng nhập");
    }

    private void sendOtp(String recipient, String fullName, String otp, int expiryMinutes, String subject, String purpose)
            throws BusinessException {
        if (!configuration.isEnabled()) {
            throw new BusinessException("Khôi phục mật khẩu hiện chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.host", configuration.getHost());
        properties.put("mail.smtp.port", String.valueOf(configuration.getPort()));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configuration.getUsername(), configuration.getPassword());
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(configuration.getFromAddress(), "RecruitFlow"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            String greeting = fullName == null || fullName.isBlank() ? "bạn" : fullName;
            message.setText("Chào " + greeting + ",\n\n"
                    + "Mã OTP RecruitFlow để " + purpose + " là: " + otp + "\n"
                    + "Mã có hiệu lực trong " + expiryMinutes + " phút và chỉ dùng một lần.\n\n"
                    + "Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email. Không chia sẻ mã OTP cho bất kỳ ai.\n\n"
                    + "RecruitFlow", StandardCharsets.UTF_8.name());
            Transport.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException exception) {
            // Do not include SMTP credentials, recipient details, or low-level transport output in the UI.
            throw new BusinessException("Không thể gửi email OTP lúc này. Vui lòng thử lại sau.", exception);
        }
    }
}
