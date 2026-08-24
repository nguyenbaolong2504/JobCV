package vn.edu.eaut.recruitflow.util;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Session-bound state nonce used to defend the OAuth callback from login CSRF. */
public final class GoogleOAuthState {
    private static final String STATE = "googleOAuthState";
    private static final String ISSUED_AT = "googleOAuthStateIssuedAt";
    private static final long MAX_AGE_MILLIS = 10 * 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private GoogleOAuthState() {
    }

    public static String issue(HttpSession session) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(STATE, state);
        session.setAttribute(ISSUED_AT, System.currentTimeMillis());
        return state;
    }

    /** Always consumes the stored nonce, including when validation fails. */
    public static boolean consumeAndMatches(HttpSession session, String suppliedState) {
        if (session == null) {
            return false;
        }
        Object expectedValue = session.getAttribute(STATE);
        Object issuedValue = session.getAttribute(ISSUED_AT);
        session.removeAttribute(STATE);
        session.removeAttribute(ISSUED_AT);
        if (!(expectedValue instanceof String expected) || !(issuedValue instanceof Long issuedAt)
                || suppliedState == null || suppliedState.length() > 256) {
            return false;
        }
        long age = System.currentTimeMillis() - issuedAt;
        return age >= 0 && age <= MAX_AGE_MILLIS && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), suppliedState.getBytes(StandardCharsets.UTF_8));
    }
}
