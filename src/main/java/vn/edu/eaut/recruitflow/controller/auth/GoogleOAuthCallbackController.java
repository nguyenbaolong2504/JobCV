package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.GoogleOAuthService;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.GoogleOAuthState;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Validates OAuth state, verifies the Google ID token server-side, then creates a fresh local session. */
@WebServlet(name = "GoogleOAuthCallbackController", urlPatterns = "/oauth/google/callback")
public class GoogleOAuthCallbackController extends BaseController {
    private GoogleOAuthService googleOAuthService;

    @Override
    public void init() throws ServletException {
        googleOAuthService = new GoogleOAuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!GoogleOAuthState.consumeAndMatches(session, request.getParameter("state"))) {
            redirectWithError(request, response, "/login", "Phiên đăng nhập Google không hợp lệ hoặc đã hết hạn.");
            return;
        }
        if (request.getParameter("error") != null) {
            redirectWithError(request, response, "/login", "Đăng nhập Google đã bị hủy hoặc không được chấp thuận.");
            return;
        }
        try {
            User user = googleOAuthService.authenticate(request.getParameter("code"));
            HttpSession authenticatedSession = AuthSession.establish(request, user);
            FlashMessage.success(authenticatedSession, "Đăng nhập Google thành công.");
            redirect(request, response, AuthSession.landingPath(user.getRoleName()));
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/login", exception.getMessage());
        }
    }
}
