package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Set;

/** Session lifecycle and role landing rules shared by password and OAuth sign-in. */
public final class AuthSession {
    public static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    public static final String SESSION_VERSION_ATTRIBUTE = "sessionVersion";
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "HR", "INTERVIEWER", "CANDIDATE");

    private AuthSession() {
    }

    public static HttpSession establish(HttpServletRequest request, User user) {
        String role = normalizeRole(user == null ? null : user.getRoleName());
        if (!VALID_ROLES.contains(role) || user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Cannot create a session for an invalid account.");
        }

        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute("userId", user.getId());
        session.setAttribute("fullName", user.getFullName() == null ? "" : user.getFullName());
        session.setAttribute("role", role);
        session.setAttribute(SESSION_VERSION_ATTRIBUTE, user.getSessionVersion());
        return session;
    }

    public static String landingPath(String role) {
        return switch (normalizeRole(role)) {
            case "ADMIN" -> "/admin/dashboard";
            case "HR" -> "/hr/dashboard";
            case "INTERVIEWER" -> "/interviewer/dashboard";
            // Job seekers begin at the public opportunity portal and can open their dashboard from the menu.
            case "CANDIDATE" -> "/home";
            default -> "/home";
        };
    }

    public static boolean isSupportedRole(String role) {
        return VALID_ROLES.contains(normalizeRole(role));
    }

    public static String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }
}
