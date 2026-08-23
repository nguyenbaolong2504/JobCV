package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.model.User;

import javax.servlet.http.HttpSession;

/** Keeps the unauthenticated, short-lived state between password entry and login OTP verification. */
public final class LoginOtpSession {
    private static final String USER_ID = "pendingLoginUserId";
    private static final String EMAIL = "pendingLoginEmail";
    private static final String ISSUED_AT = "pendingLoginOtpIssuedAt";
    private static final long MAX_AGE_MILLIS = 10 * 60 * 1000L;

    private LoginOtpSession() {
    }

    public static void start(HttpSession session, User user) {
        clear(session);
        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(EMAIL, user.getEmail());
        session.setAttribute(ISSUED_AT, System.currentTimeMillis());
        session.setMaxInactiveInterval((int) (MAX_AGE_MILLIS / 1000L));
    }

    public static Integer pendingUserId(HttpSession session) {
        if (!isCurrent(session)) {
            clear(session);
            return null;
        }
        Object value = session.getAttribute(USER_ID);
        return value instanceof Integer userId && userId > 0 ? userId : null;
    }

    public static String maskedEmail(HttpSession session) {
        if (!isCurrent(session)) {
            return "";
        }
        Object value = session.getAttribute(EMAIL);
        if (!(value instanceof String email) || email.isBlank()) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + (at >= 0 ? email.substring(at) : "");
        }
        return email.substring(0, 1) + "***" + email.substring(at);
    }

    public static void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(USER_ID);
        session.removeAttribute(EMAIL);
        session.removeAttribute(ISSUED_AT);
    }

    private static boolean isCurrent(HttpSession session) {
        if (session == null || !(session.getAttribute(ISSUED_AT) instanceof Long issuedAt)) {
            return false;
        }
        long age = System.currentTimeMillis() - issuedAt;
        return issuedAt > 0 && age >= 0 && age <= MAX_AGE_MILLIS;
    }
}
