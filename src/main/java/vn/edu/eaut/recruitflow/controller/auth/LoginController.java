package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/** Handles authentication only; credential verification is delegated to {@link UserService}. */
@WebServlet(name = "LoginController", urlPatterns = "/login")
public class LoginController extends BaseController {
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "HR", "INTERVIEWER", "CANDIDATE");
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 15L * 60L * 1000L;
    private static final Map<String, LoginAttempt> LOGIN_ATTEMPTS = new HashMap<>();

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setUtf8(request, response);

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") instanceof Integer
                && session.getAttribute("role") instanceof String) {
            redirectByRole(request, response, (String) session.getAttribute("role"));
            return;
        }

        view(request, response, "/WEB-INF/views/auth/login.jsp", "Đăng nhập | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setUtf8(request, response);

        try {
            String email = normalizeAndValidateEmail(RequestUtil.text(request, "email"));
            String password = validatePassword(RequestUtil.text(request, "password"));
            String attemptKey = clientAddress(request) + '|' + email;
            if (isTemporarilyLocked(attemptKey)) {
                redirectWithError(request, response, "/login", "Đăng nhập bị tạm khóa do thử sai quá nhiều lần. Vui lòng thử lại sau 15 phút.");
                return;
            }
            User user = userService.authenticate(email, password);

            if (user == null) {
                recordFailure(attemptKey);
                redirectWithError(request, response, "/login", "Email hoặc mật khẩu không chính xác, hoặc tài khoản không hoạt động.");
                return;
            }

            String role = normalizeRole(user.getRoleName());
            if (!VALID_ROLES.contains(role)) {
                // Do not create an authenticated session for a malformed account record.
                redirectWithError(request, response, "/login", "Tài khoản chưa được gán quyền truy cập hợp lệ.");
                return;
            }

            AuthSession.establish(request, user);
            clearFailures(attemptKey);
            FlashMessage.success(request.getSession(false), "Đăng nhập thành công.");
            redirectByRole(request, response, role);
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/login", ex.getMessage());
        }
    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, String role) throws IOException {
        String normalizedRole = normalizeRole(role);
        switch (normalizedRole) {
            case "ADMIN":
                redirect(request, response, "/admin/dashboard");
                break;
            case "HR":
                redirect(request, response, "/hr/dashboard");
                break;
            case "INTERVIEWER":
                redirect(request, response, "/interviewer/dashboard");
                break;
            case "CANDIDATE":
                redirect(request, response, "/home");
                break;
            default:
                redirect(request, response, "/home");
                break;
        }
    }

    private String normalizeAndValidateEmail(String email) throws BusinessException {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || normalized.length() > 254
                || !normalized.matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$")) {
            throw new BusinessException("Vui lòng nhập địa chỉ email hợp lệ.");
        }
        return normalized;
    }

    private String validatePassword(String password) throws BusinessException {
        if (password == null || password.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mật khẩu.");
        }
        // BCrypt only considers the first 72 bytes, so reject longer values explicitly.
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Mật khẩu không được vượt quá 72 byte.");
        }
        return password;
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }

    private String clientAddress(HttpServletRequest request) {
        // Do not trust X-Forwarded-For here unless a trusted reverse proxy is configured to sanitize it.
        String address = request.getRemoteAddr();
        return address == null ? "unknown" : address;
    }

    private static synchronized boolean isTemporarilyLocked(String key) {
        LoginAttempt attempt = LOGIN_ATTEMPTS.get(key);
        if (attempt == null) return false;
        if (attempt.lockedUntil <= System.currentTimeMillis()) {
            LOGIN_ATTEMPTS.remove(key);
            return false;
        }
        return attempt.failures >= MAX_FAILED_ATTEMPTS;
    }

    private static synchronized void recordFailure(String key) {
        long now = System.currentTimeMillis();
        LoginAttempt current = LOGIN_ATTEMPTS.get(key);
        int failures = current == null || current.lockedUntil <= now ? 1 : current.failures + 1;
        LOGIN_ATTEMPTS.put(key, new LoginAttempt(failures, now + LOCK_MILLIS));
        if (LOGIN_ATTEMPTS.size() > 10_000) {
            LOGIN_ATTEMPTS.entrySet().removeIf(entry -> entry.getValue().lockedUntil <= now);
            while (LOGIN_ATTEMPTS.size() > 10_000) {
                LOGIN_ATTEMPTS.remove(LOGIN_ATTEMPTS.keySet().iterator().next());
            }
        }
    }

    private static synchronized void clearFailures(String key) {
        LOGIN_ATTEMPTS.remove(key);
    }

    private static final class LoginAttempt {
        private final int failures;
        private final long lockedUntil;

        private LoginAttempt(int failures, long lockedUntil) {
            this.failures = failures;
            this.lockedUntil = lockedUntil;
        }
    }

    private void setUtf8(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}
