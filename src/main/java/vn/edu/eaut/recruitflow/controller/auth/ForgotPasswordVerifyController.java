package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.PasswordResetService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.PasswordResetSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Verifies a reset OTP before creating a short-lived, session-bound reset grant. */
@WebServlet(name = "ForgotPasswordVerifyController", urlPatterns = "/forgot-password/verify")
public class ForgotPasswordVerifyController extends BaseController {
    private PasswordResetService passwordResetService;

    @Override
    public void init() throws ServletException {
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = PasswordResetSession.email(session);
        if (email.isBlank()) {
            redirectWithError(request, response, "/forgot-password", "Vui lòng yêu cầu mã OTP trước.");
            return;
        }
        request.setAttribute("resetEmail", email);
        view(request, response, "/WEB-INF/views/auth/forgot-password-verify.jsp", "Xác minh OTP | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String email = PasswordResetSession.email(session);
        if (email.isBlank()) {
            redirectWithError(request, response, "/forgot-password", "Phiên khôi phục mật khẩu đã hết hạn. Vui lòng yêu cầu mã mới.");
            return;
        }
        try {
            int userId = passwordResetService.verifyOtp(email, request.getParameter("otp"));
            // Rotate the anonymous session ID before storing the reset grant.
            request.changeSessionId();
            session = request.getSession(false);
            PasswordResetSession.markVerified(session, userId);
            FlashMessage.success(session, "Mã OTP hợp lệ. Hãy tạo mật khẩu mới.");
            redirect(request, response, "/forgot-password/reset");
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/forgot-password/verify", exception.getMessage());
        }
    }
}
