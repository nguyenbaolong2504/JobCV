package vn.edu.eaut.recruitflow.util;

import javax.servlet.http.HttpSession;

/** Session-bound proof that an email OTP has just been verified for a password reset. */
public final class PasswordResetSession {
    private static final String EMAIL = "passwordResetEmail";
    private static final String VERIFIED_USER_ID = "passwordResetVerifiedUserId";
    private static final String VERIFIED_AT = "passwordResetVerifiedAt";
    private static final long VERIFICATION_MAX_AGE_MILLIS = 10 * 60 * 1000L;

    private PasswordResetSession() {
    }

    public static void setEmail(HttpSession session, String email) {
        session.setAttribute(EMAIL, email);
    }

    public static String email(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(EMAIL);
        return value instanceof String email ? email : "";
    }

    public static void markVerified(HttpSession session, int userId) {
        session.setAttribute(VERIFIED_USER_ID, userId);
        session.setAttribute(VERIFIED_AT, System.currentTimeMillis());
    }

    public static Integer verifiedUserId(HttpSession session) {
        long now = System.currentTimeMillis();
        if (session == null || !(session.getAttribute(VERIFIED_USER_ID) instanceof Integer userId)
                || !(session.getAttribute(VERIFIED_AT) instanceof Long verifiedAt)
                || userId <= 0 || now - verifiedAt < 0 || now - verifiedAt > VERIFICATION_MAX_AGE_MILLIS) {
            clearVerification(session);
            return null;
        }
        return userId;
    }

    public static void clearVerification(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(VERIFIED_USER_ID);
        session.removeAttribute(VERIFIED_AT);
    }

    public static void clearAll(HttpSession session) {
        clearVerification(session);
        if (session != null) {
            session.removeAttribute(EMAIL);
        }
    }
}
