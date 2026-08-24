package vn.edu.eaut.recruitflow.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/** Shared, server-side validation for credentials and public authentication forms. */
public final class AuthValidation {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+(). -]{8,30}$");
    private static final Pattern OTP_PATTERN = Pattern.compile("^[0-9]{6}$");

    private AuthValidation() {
    }

    public static String email(String value) throws BusinessException {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("Vui lòng nhập địa chỉ email hợp lệ.");
        }
        return normalized;
    }

    public static String fullName(String value) throws BusinessException {
        String normalized = normalizeSpaces(value);
        if (normalized.length() < 2 || normalized.length() > 100) {
            throw new BusinessException("Họ và tên phải có từ 2 đến 100 ký tự.");
        }
        return normalized;
    }

    public static String organizationName(String value) throws BusinessException {
        String normalized = normalizeSpaces(value);
        if (normalized.length() < 2 || normalized.length() > 150) {
            throw new BusinessException("Tên công ty/tổ chức phải có từ 2 đến 150 ký tự.");
        }
        return normalized;
    }

    public static String jobTitle(String value) throws BusinessException {
        String normalized = normalizeSpaces(value);
        if (normalized.length() < 2 || normalized.length() > 100) {
            throw new BusinessException("Chức danh tuyển dụng phải có từ 2 đến 100 ký tự.");
        }
        return normalized;
    }

    public static String workPhone(String value) throws BusinessException {
        String normalized = value == null ? "" : value.trim();
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("Số điện thoại công việc không hợp lệ.");
        }
        return normalized;
    }

    /** BCrypt only uses the first 72 bytes; reject longer credentials explicitly. */
    public static String newPassword(String value) throws BusinessException {
        if (value == null || value.length() < 8 || value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Mật khẩu phải có từ 8 đến 72 byte.");
        }
        boolean hasLetter = value.codePoints().anyMatch(Character::isLetter);
        boolean hasDigit = value.codePoints().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException("Mật khẩu phải có ít nhất một chữ cái và một chữ số.");
        }
        return value;
    }

    public static String loginPassword(String value) throws BusinessException {
        if (value == null || value.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mật khẩu.");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Mật khẩu không được vượt quá 72 byte.");
        }
        return value;
    }

    public static String otp(String value) throws BusinessException {
        String normalized = value == null ? "" : value.trim();
        if (!OTP_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("Mã OTP phải gồm đúng 6 chữ số.");
        }
        return normalized;
    }

    public static String normalizeSpaces(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
