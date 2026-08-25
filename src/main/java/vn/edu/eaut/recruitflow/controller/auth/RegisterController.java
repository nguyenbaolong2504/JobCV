package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
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
        if (session != null && session.getAttribute("userId") instanceof Integer
                && session.getAttribute("role") instanceof String) {
            redirectAuthenticatedUser(request, response, (String) session.getAttribute("role"));
            return;
        }

        view(request, response, "/WEB-INF/views/auth/register.jsp", "Đăng ký | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setUtf8(request, response);

        try {
            String fullName = AuthValidation.fullName(RequestUtil.text(request, "fullName"));
            String email = AuthValidation.email(RequestUtil.text(request, "email"));
            String password = AuthValidation.newPassword(RequestUtil.text(request, "password"));
            String confirmPassword = RequestUtil.text(request, "confirmPassword");
            String accountType = validateAccountType(RequestUtil.text(request, "accountType"));
            String organizationName = "HR".equals(accountType) ? RequestUtil.text(request, "organizationName") : null;
            String jobTitle = "HR".equals(accountType) ? RequestUtil.text(request, "jobTitle") : null;
            String workPhone = "HR".equals(accountType) ? RequestUtil.text(request, "workPhone") : null;

            // The current form does not require a confirmation input, but validate it when supplied.
            if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
                throw new BusinessException("Xác nhận mật khẩu không khớp.");
            }

            userService.registerAccount(email, password, fullName, accountType,
                    organizationName, jobTitle, workPhone);
            redirectWithSuccess(request, response, "/login", "HR".equals(accountType)
                    ? "Đăng ký thành công. Hồ sơ nhà tuyển dụng đang chờ quản trị viên xét duyệt."
                    : "Đăng ký thành công. Vui lòng đăng nhập để tiếp tục.");
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
