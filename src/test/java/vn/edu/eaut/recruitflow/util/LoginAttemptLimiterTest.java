package vn.edu.eaut.recruitflow.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptLimiterTest {
    @Test
    void blocksAnAccountAndAddressPairAfterFiveFailedPasswords() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();

        for (int attempt = 0; attempt < 5; attempt++) {
            limiter.recordFailure("candidate@example.com", "127.0.0.1");
        }

        assertTrue(limiter.retryAfterSeconds("candidate@example.com", "127.0.0.1") > 0);
        assertEquals(0, limiter.retryAfterSeconds("candidate@example.com", "127.0.0.2"));
    }

    @Test
    void verifiedPasswordClearsOnlyTheMatchingAccountAndAddressPair() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int attempt = 0; attempt < 3; attempt++) {
            limiter.recordFailure("candidate@example.com", "127.0.0.1");
        }

        limiter.recordSuccess("candidate@example.com", "127.0.0.1");

        assertEquals(0, limiter.retryAfterSeconds("candidate@example.com", "127.0.0.1"));
    }

    @Test
    void blocksAClientThatCyclesThroughManyDifferentEmails() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int attempt = 0; attempt < 20; attempt++) {
            limiter.recordFailure("candidate" + attempt + "@example.com", "192.0.2.44");
        }

        assertTrue(limiter.retryAfterSeconds("another@example.com", "192.0.2.44") > 0);
        assertEquals(0, limiter.retryAfterSeconds("another@example.com", "192.0.2.45"));
    }
}
