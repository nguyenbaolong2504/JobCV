package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.GoogleOAuthService;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.GoogleOAuthState;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Begins the Google authorization-code flow; no provider secret is rendered to the browser. */
@WebServlet(name = "GoogleOAuthStartController", urlPatterns = "/oauth/google")
public class GoogleOAuthStartController extends BaseController {
    private GoogleOAuthService googleOAuthService;

    @Override
    public void init() throws ServletException {
        googleOAuthService = new GoogleOAuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") instanceof Integer
                && session.getAttribute("role") instanceof String) {
            redirect(request, response, AuthSession.landingPath((String) session.getAttribute("role")));
            return;
        }
        try {
            HttpSession oauthSession = request.getSession(true);
            String state = GoogleOAuthState.issue(oauthSession);
            response.sendRedirect(googleOAuthService.authorizationUrl(state));
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/login", exception.getMessage());
        }
    }
}
