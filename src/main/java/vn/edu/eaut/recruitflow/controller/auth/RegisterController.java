package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Registers a new job-seeker or recruiter account through the service layer. */
@WebServlet(name = "RegisterController", urlPatterns = "/register")
public class RegisterController extends BaseController {
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
        if (AuthSession.isAuthenticated(session)) {
            redirectAuthenticatedUser(request, response, (String) session.getAttribute("role"));
            return;
        }

        view(request, response, "/WEB-INF/views/auth/register.jsp", "Đăng ký | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setUtf8(request, response);

        try {
            String fullName = validateFullName(RequestUtil.text(request, "fullName"));
            String email = normalizeAndValidateEmail(RequestUtil.text(request, "email"));
            String password = validatePassword(RequestUtil.text(request, "password"));
            String confirmPassword = RequestUtil.text(request, "confirmPassword");
            String accountType = validateAccountType(RequestUtil.text(request, "accountType"));

            // The current form does not require a confirmation input, but validate it when supplied.
            if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
                throw new BusinessException("Xác nhận mật khẩu không khớp.");
            }

            userService.registerAccount(email, password, fullName, accountType,
                    RequestUtil.text(request, "organizationName"), RequestUtil.text(request, "jobTitle"),
                    RequestUtil.text(request, "workPhone"));
            redirectWithSuccess(request, response, "/login", "HR".equals(accountType)
                    ? "Đã gửi đăng ký Nhà tuyển dụng. Admin sẽ xác minh công ty trước khi kích hoạt tài khoản."
                    : "Đăng ký thành công. Bạn có thể đăng nhập và bắt đầu tìm việc.");
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/register", ex.getMessage());
        }
    }

    private void redirectAuthenticatedUser(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
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

    private String validateFullName(String fullName) throws BusinessException {
        String normalized = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 2 || normalized.length() > 100) {
            throw new BusinessException("Họ và tên phải có từ 2 đến 100 ký tự.");
        }
        return normalized;
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
        if (password == null || password.length() < 6) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Mật khẩu không được vượt quá 72 byte.");
        }
        return password;
    }

    private String validateAccountType(String accountType) throws BusinessException {
        String normalized = accountType == null ? "" : accountType.trim().toUpperCase(Locale.ROOT);
        if (!"CANDIDATE".equals(normalized) && !"HR".equals(normalized)) {
            throw new BusinessException("Vui lòng chọn loại tài khoản hợp lệ.");
        }
        return normalized;
    }

    private void setUtf8(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}
