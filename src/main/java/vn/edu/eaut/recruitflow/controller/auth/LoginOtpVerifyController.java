package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.LoginOtpService;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.LoginOtpSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Completes a password login only after the configured email OTP is verified. */
@WebServlet(name = "LoginOtpVerifyController", urlPatterns = "/login/verify-otp")
public class LoginOtpVerifyController extends BaseController {
    private LoginOtpService loginOtpService;

    @Override
    public void init() throws ServletException {
        loginOtpService = new LoginOtpService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (AuthSession.isAuthenticated(session)) {
            redirect(request, response, AuthSession.landingPath((String) session.getAttribute("role")));
            return;
        }
        if (!loginOtpService.isRequired() || LoginOtpSession.pendingUserId(session) == null) {
            redirectWithError(request, response, "/login", "Phiên xác minh OTP đã hết hạn. Vui lòng đăng nhập lại.");
            return;
        }
        request.setAttribute("maskedEmail", LoginOtpSession.maskedEmail(session));
        view(request, response, "/WEB-INF/views/auth/login-verify-otp.jsp", "Xác minh OTP | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Integer userId = LoginOtpSession.pendingUserId(session);
        if (!loginOtpService.isRequired() || userId == null) {
            redirectWithError(request, response, "/login", "Phiên xác minh OTP đã hết hạn. Vui lòng đăng nhập lại.");
            return;
        }
        try {
            User user = loginOtpService.verifyOtp(userId, request.getParameter("otp"));
            LoginOtpSession.clear(session);
            HttpSession authenticatedSession = AuthSession.establish(request, user);
            FlashMessage.success(authenticatedSession, "Xác minh OTP thành công. Bạn đã đăng nhập.");
            redirect(request, response, AuthSession.landingPath(user.getRoleName()));
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/login/verify-otp", exception.getMessage());
        }
    }
}
