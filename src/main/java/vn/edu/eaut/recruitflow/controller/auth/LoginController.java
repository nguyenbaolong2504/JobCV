package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.BusinessException;
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

/** Handles authentication only; credential verification is delegated to {@link UserService}. */
@WebServlet(name = "LoginController", urlPatterns = "/login")
public class LoginController extends BaseController {
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "HR", "INTERVIEWER", "CANDIDATE");
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

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
            User user = userService.authenticate(email, password);

            if (user == null) {
                redirectWithError(request, response, "/login", "Email hoặc mật khẩu không chính xác, hoặc tài khoản không hoạt động.");
                return;
            }

            String role = normalizeRole(user.getRoleName());
            if (!VALID_ROLES.contains(role)) {
                // Do not create an authenticated session for a malformed account record.
                redirectWithError(request, response, "/login", "Tài khoản chưa được gán quyền truy cập hợp lệ.");
                return;
            }

            establishAuthenticatedSession(request, user, role);
            FlashMessage.success(request.getSession(false), "Đăng nhập thành công.");
            redirectByRole(request, response, role);
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/login", ex.getMessage());
        }
    }

    /**
     * Invalidating a possible anonymous session before creating the authenticated session
     * prevents a session identifier supplied before login from being retained.
     */
    private void establishAuthenticatedSession(HttpServletRequest request, User user, String role) {
        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession authenticatedSession = request.getSession(true);
        authenticatedSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        authenticatedSession.setAttribute("userId", user.getId());
        authenticatedSession.setAttribute("fullName", user.getFullName() == null ? "" : user.getFullName());
        authenticatedSession.setAttribute("role", role);
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
                redirect(request, response, "/candidate/dashboard");
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

    private void setUtf8(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}
