package vn.edu.eaut.recruitflow.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class RequestUtil {
    private RequestUtil() {
    }

    public static String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static int requiredPositiveInt(HttpServletRequest request, String name, String label) throws BusinessException {
        try {
            int value = Integer.parseInt(text(request, name));
            if (value <= 0) {
                throw new BusinessException(label + " phải lớn hơn 0.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }

    public static int nonNegativeInt(HttpServletRequest request, String name, String label) throws BusinessException {
        try {
            int value = Integer.parseInt(text(request, name));
            if (value < 0) {
                throw new BusinessException(label + " không được âm.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }

    public static int page(HttpServletRequest request) {
        try {
            return Math.max(1, Integer.parseInt(text(request, "page")));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    public static int pageSize(HttpServletRequest request) {
        try {
            int value = Integer.parseInt(text(request, "pageSize"));
            return value == 20 || value == 50 ? value : 10;
        } catch (NumberFormatException ignored) {
            return 10;
        }
    }

    public static BigDecimal decimal(HttpServletRequest request, String name, String label) throws BusinessException {
        try {
            return new BigDecimal(text(request, name));
        } catch (NumberFormatException ex) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }

    public static LocalDate date(HttpServletRequest request, String name, String label) throws BusinessException {
        try {
            return LocalDate.parse(text(request, name));
        } catch (DateTimeParseException ex) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }

    public static int currentUserId(HttpServletRequest request) throws BusinessException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            throw new BusinessException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return (Integer) session.getAttribute("userId");
    }
}
