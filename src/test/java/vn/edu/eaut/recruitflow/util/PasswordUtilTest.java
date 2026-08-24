package vn.edu.eaut.recruitflow.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void hashesAndVerifiesPasswords() {
        String hash = PasswordUtil.hash("RecruitFlow@2026");

        assertNotEquals("RecruitFlow@2026", hash);
        assertTrue(PasswordUtil.matches("RecruitFlow@2026", hash));
        assertFalse(PasswordUtil.matches("wrong-password", hash));
    }

    @Test
    void rejectsNullAndMalformedHashes() {
        assertFalse(PasswordUtil.matches(null, "$2a$12$invalid"));
        assertFalse(PasswordUtil.matches("password", null));
        assertFalse(PasswordUtil.matches("password", "not-a-bcrypt-hash"));
    }
}
