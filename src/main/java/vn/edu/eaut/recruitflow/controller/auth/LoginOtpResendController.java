package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.LoginOtpService;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.LoginOtpSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** CSRF-protected resend endpoint for a still-pending password-login OTP. */
@WebServlet(name = "LoginOtpResendController", urlPatterns = "/login/verify-otp/resend")
public class LoginOtpResendController extends BaseController {
    private LoginOtpService loginOtpService;
    private UserService userService;

    @Override
    public void init() throws ServletException {
        loginOtpService = new LoginOtpService();
        userService = new UserService();
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
            User user = userService.getById(userId);
            loginOtpService.requestOtp(user);
            FlashMessage.success(session, "Đã gửi mã OTP mới. Mã cũ không còn hiệu lực.");
            redirect(request, response, "/login/verify-otp");
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/login/verify-otp", exception.getMessage());
        }
    }
}
