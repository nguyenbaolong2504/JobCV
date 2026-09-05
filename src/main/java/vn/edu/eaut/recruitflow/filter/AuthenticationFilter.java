package vn.edu.eaut.recruitflow.filter;

import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.AuthSession;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    private UserDAO userDAO;

    @Override
    public void init(FilterConfig filterConfig) {
        userDAO = new UserDAO();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }
        boolean protectedPath = requiresAuthentication(path);
        boolean loggedIn = AuthSession.isAuthenticated(session);
        boolean stalePrincipal = !loggedIn && AuthSession.hasPrincipalAttributes(session);

        SessionValidation sessionValidation = loggedIn
                ? validateAuthenticatedAccount(session)
                : (stalePrincipal ? SessionValidation.INVALID : SessionValidation.CURRENT);
        boolean accountChanged = sessionValidation == SessionValidation.INVALID;
        if (accountChanged && session != null) {
            // A lock, deactivation, deleted record, or role change must take effect at the
            // next request rather than leaving a stale privileged session alive.
            session.invalidate();
            session = null;
            loggedIn = false;
        }

        // Do not treat a temporary database outage as a role/status mutation. A protected
        // endpoint fails closed, while public pages remain usable and the session is retained.
        if (sessionValidation == SessionValidation.UNAVAILABLE && protectedPath) {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể xác thực phiên đăng nhập. Vui lòng thử lại sau.");
            return;
        }

        if (loggedIn && sessionValidation == SessionValidation.CURRENT) {
            exposePrincipalForView(req, session);
        }

        if (!protectedPath) {
            if (accountChanged) {
                req.getSession(true).setAttribute("flashWarning",
                        "Tài khoản hoặc quyền truy cập của bạn đã thay đổi. Vui lòng đăng nhập lại.");
                // Restart the GET after creating the replacement session. This also prevents a
                // CSRF token produced by a preceding filter for the invalidated session from
                // being rendered into a login/public form.
                String redirectTarget = req.getContextPath() + path;
                if (req.getQueryString() != null && !req.getQueryString().isBlank()) {
                    redirectTarget += "?" + req.getQueryString();
                }
                res.sendRedirect(redirectTarget);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if (loggedIn) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession anonymousSession = req.getSession(true);
        anonymousSession.setAttribute("flashWarning", accountChanged
                ? "Tài khoản hoặc quyền truy cập của bạn đã thay đổi. Vui lòng đăng nhập lại."
                : "Vui lòng đăng nhập để tiếp tục.");
        res.sendRedirect(req.getContextPath() + "/login");
    }

    private SessionValidation validateAuthenticatedAccount(HttpSession session) {
        Object idValue = session.getAttribute("userId");
        Object roleValue = session.getAttribute("role");
        Object versionValue = session.getAttribute(AuthSession.SESSION_VERSION_ATTRIBUTE);
        if (!(idValue instanceof Integer userId) || !(roleValue instanceof String sessionRole)
                || !(versionValue instanceof Integer sessionVersion)) {
            return SessionValidation.INVALID;
        }
        try {
            User currentUser = userDAO.findById(userId);
            return currentUser != null
                    && UserStatus.ACTIVE.name().equals(currentUser.getStatus())
                    && currentUser.getRoleName() != null
                    && currentUser.getRoleName().equalsIgnoreCase(sessionRole.trim())
                    && currentUser.getSessionVersion() == sessionVersion
                    ? SessionValidation.CURRENT : SessionValidation.INVALID;
        } catch (SQLException exception) {
            return SessionValidation.UNAVAILABLE;
        }
    }

    private enum SessionValidation {
        CURRENT,
        INVALID,
        UNAVAILABLE
    }

    /**
     * Public JSPs deliberately run with {@code session="false"} so a guest browsing jobs does
     * not receive a needless JSESSIONID. Copy the already-validated identity into request scope
     * for the shared navbar instead of making those JSPs create/access a session themselves.
     */
    private void exposePrincipalForView(HttpServletRequest request, HttpSession session) {
        request.setAttribute("currentUserId", session.getAttribute("userId"));
        request.setAttribute("currentRole", session.getAttribute("role"));
        request.setAttribute("currentFullName", session.getAttribute("fullName"));
    }

    private boolean requiresAuthentication(String path) {
        return path.startsWith("/candidate/")
                || path.startsWith("/hr/")
                || path.startsWith("/interviewer/")
                || path.startsWith("/admin/");
    }

    @Override
    public void destroy() {}
}
