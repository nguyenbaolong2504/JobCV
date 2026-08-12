package vn.edu.eaut.recruitflow.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {
    }

    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String plainText, String passwordHash) {
        try {
            return plainText != null && passwordHash != null && BCrypt.checkpw(plainText, passwordHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
