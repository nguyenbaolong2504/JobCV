package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.GoogleOAuthService;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/** Registers either an immediately usable Candidate or an Admin-reviewed recruiter account. */
@WebServlet(name = "RegisterController", urlPatterns = "/register")
public class RegisterController extends BaseController {
    private UserService userService;
    private GoogleOAuthService googleOAuthService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
        googleOAuthService = new GoogleOAuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setUtf8(request, response);

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") instanceof Integer
                && session.getAttribute("role") instanceof String) {
            redirect(request, response, AuthSession.landingPath((String) session.getAttribute("role")));
            return;
        }

        request.setAttribute("googleOAuthEnabled", googleOAuthService.isEnabled());
        view(request, response, "/WEB-INF/views/auth/register.jsp", "Đăng ký | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setUtf8(request, response);

        try {
            String accountType = validateAccountType(request.getParameter("accountType"));
            String fullName = AuthValidation.fullName(request.getParameter("fullName"));
            String email = AuthValidation.email(request.getParameter("email"));
            String password = AuthValidation.newPassword(request.getParameter("password"));
            String confirmPassword = request.getParameter("confirmPassword");
            if (!password.equals(confirmPassword)) {
                throw new BusinessException("Xác nhận mật khẩu không khớp.");
            }
            if (!"on".equalsIgnoreCase(request.getParameter("termsAccepted"))) {
                throw new BusinessException("Vui lòng xác nhận bạn đồng ý với điều khoản sử dụng.");
            }

            userService.registerAccount(email, password, fullName, accountType,
                    request.getParameter("organizationName"), request.getParameter("jobTitle"), request.getParameter("workPhone"));
            String message = "HR".equals(accountType)
                    ? "Đã gửi yêu cầu tài khoản Nhà tuyển dụng. Admin sẽ kiểm duyệt trước khi bạn có thể đăng nhập."
                    : "Đăng ký thành công. Vui lòng đăng nhập để tìm việc và hoàn thiện hồ sơ.";
            redirectWithSuccess(request, response, "/login", message);
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/register", ex.getMessage());
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
