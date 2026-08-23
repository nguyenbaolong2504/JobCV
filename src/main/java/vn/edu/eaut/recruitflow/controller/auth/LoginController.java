package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.GoogleOAuthService;
import vn.edu.eaut.recruitflow.service.LoginOtpService;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.LoginAttemptLimiter;
import vn.edu.eaut.recruitflow.util.LoginOtpSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Password login; an email OTP second step is activated only by explicit runtime configuration. */
@WebServlet(name = "LoginController", urlPatterns = "/login")
public class LoginController extends BaseController {
    private UserService userService;
    private LoginOtpService loginOtpService;
    private GoogleOAuthService googleOAuthService;
    private LoginAttemptLimiter loginAttemptLimiter;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
        loginOtpService = new LoginOtpService();
        googleOAuthService = new GoogleOAuthService();
        loginAttemptLimiter = new LoginAttemptLimiter();
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
        request.setAttribute("loginOtpRequired", loginOtpService.isRequired());
        request.setAttribute("loginOtpMisconfigured", loginOtpService.isMisconfigured());
        view(request, response, "/WEB-INF/views/auth/login.jsp", "Đăng nhập | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setUtf8(request, response);

        try {
            String email = AuthValidation.email(request.getParameter("email"));
            String password = AuthValidation.loginPassword(request.getParameter("password"));
            String remoteAddress = request.getRemoteAddr();
            long retryAfterSeconds = loginAttemptLimiter.retryAfterSeconds(email, remoteAddress);
            if (retryAfterSeconds > 0) {
                redirectWithError(request, response, "/login",
                        "Đăng nhập tạm thời bị giới hạn. Vui lòng thử lại sau " + retryAfterSeconds + " giây.");
                return;
            }
            User user = userService.authenticate(email, password);

            if (user == null) {
                loginAttemptLimiter.recordFailure(email, remoteAddress);
                redirectWithError(request, response, "/login", "Email hoặc mật khẩu không chính xác, hoặc tài khoản không hoạt động.");
                return;
            }
            if (!AuthSession.isSupportedRole(user.getRoleName())) {
                redirectWithError(request, response, "/login", "Tài khoản chưa được gán quyền truy cập hợp lệ.");
                return;
            }
            loginAttemptLimiter.recordSuccess(email, remoteAddress);
            if (loginOtpService.isMisconfigured()) {
                redirectWithError(request, response, "/login",
                        "Xác minh OTP đăng nhập đang được yêu cầu nhưng Gmail SMTP chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
                return;
            }
            if (loginOtpService.isRequired()) {
                loginOtpService.requestOtp(user);
                HttpSession pendingSession = request.getSession(true);
                LoginOtpSession.start(pendingSession, user);
                FlashMessage.success(pendingSession, "Mã OTP đã được gửi đến email của bạn. Vui lòng nhập mã để hoàn tất đăng nhập.");
                redirect(request, response, "/login/verify-otp");
                return;
            }

            HttpSession authenticatedSession = AuthSession.establish(request, user);
            FlashMessage.success(authenticatedSession, "Đăng nhập thành công.");
            redirect(request, response, AuthSession.landingPath(user.getRoleName()));
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/login", ex.getMessage());
        }
    }

    private void setUtf8(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}
