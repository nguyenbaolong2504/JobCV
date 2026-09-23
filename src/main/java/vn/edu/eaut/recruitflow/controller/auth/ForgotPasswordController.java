package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.PasswordResetService;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.PasswordResetSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Starts the enumeration-safe Gmail OTP password-reset flow. */
@WebServlet(name = "ForgotPasswordController", urlPatterns = "/forgot-password")
public class ForgotPasswordController extends BaseController {
    private PasswordResetService passwordResetService;

    @Override
    public void init() throws ServletException {
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("passwordResetAvailable", passwordResetService.isAvailable());
        view(request, response, "/WEB-INF/views/auth/forgot-password.jsp", "Quên mật khẩu | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String email = AuthValidation.email(request.getParameter("email"));
            passwordResetService.requestOtp(email);
            HttpSession session = request.getSession(true);
            PasswordResetSession.setEmail(session, email);
            PasswordResetSession.clearVerification(session);
            // Same successful response whether or not an active account owns the email.
            FlashMessage.success(session, "Nếu email thuộc một tài khoản đang hoạt động, mã OTP đã được gửi. Vui lòng kiểm tra hộp thư.");
            redirect(request, response, "/forgot-password/verify");
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/forgot-password", exception.getMessage());
        }
    }
}
