package vn.edu.eaut.recruitflow.util;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory throttle for password sign-in. It deliberately keys an account by both
 * normalized email and remote address, so a stranger cannot lock an account globally just by
 * submitting its email. A wider per-address limit also stops a single client from cycling
 * through many emails. For a multi-node deployment, replace this with a shared rate-limit store.
 */
public final class LoginAttemptLimiter {
    private static final int ACCOUNT_MAX_FAILURES = 5;
    private static final int ADDRESS_MAX_FAILURES = 20;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(15).toMillis();
    private static final long BLOCK_MILLIS = Duration.ofMinutes(15).toMillis();
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    /** Returns the delay before another password check may be attempted, or zero when allowed. */
    public long retryAfterSeconds(String normalizedEmail, String remoteAddress) {
        long now = System.currentTimeMillis();
        // A read-only check must not create two map entries for every arbitrary email/IP an
        // attacker submits. States are created only after an actual failed password attempt.
        AttemptState accountState = attempts.get(accountKey(normalizedEmail, remoteAddress));
        AttemptState addressState = attempts.get(addressKey(remoteAddress));
        long accountDelay = accountState == null ? 0 : accountState.retryAfterMillis(now, WINDOW_MILLIS);
        long addressDelay = addressState == null ? 0 : addressState.retryAfterMillis(now, WINDOW_MILLIS);
        long delayMillis = Math.max(accountDelay, addressDelay);
        return delayMillis <= 0 ? 0 : Math.max(1, (delayMillis + 999) / 1000);
    }

    /** Records a failed credential check without exposing whether the supplied account exists. */
    public void recordFailure(String normalizedEmail, String remoteAddress) {
        long now = System.currentTimeMillis();
        stateFor(accountKey(normalizedEmail, remoteAddress)).recordFailure(now, WINDOW_MILLIS,
                ACCOUNT_MAX_FAILURES, BLOCK_MILLIS);
        stateFor(addressKey(remoteAddress)).recordFailure(now, WINDOW_MILLIS,
                ADDRESS_MAX_FAILURES, BLOCK_MILLIS);
        trimIfNeeded(now);
    }

    /** A verified password clears only the account/address pair; the wider address budget remains. */
    public void recordSuccess(String normalizedEmail, String remoteAddress) {
        attempts.remove(accountKey(normalizedEmail, remoteAddress));
    }

    private AttemptState stateFor(String key) {
        return attempts.computeIfAbsent(key, ignored -> new AttemptState());
    }

    private void trimIfNeeded(long now) {
        if (attempts.size() <= MAX_TRACKED_KEYS) {
            return;
        }
        attempts.entrySet().removeIf(entry -> entry.getValue().isExpired(now, WINDOW_MILLIS));
        if (attempts.size() <= MAX_TRACKED_KEYS) {
            return;
        }
        int surplus = attempts.size() - MAX_TRACKED_KEYS;
        for (String key : attempts.keySet()) {
            if (surplus-- <= 0) {
                break;
            }
            attempts.remove(key);
        }
    }

    private String accountKey(String email, String remoteAddress) {
        return "account:" + safeComponent(email) + "|" + safeComponent(remoteAddress);
    }

    private String addressKey(String remoteAddress) {
        return "address:" + safeComponent(remoteAddress);
    }

    private String safeComponent(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String trimmed = value.trim();
        return trimmed.length() > 320 ? trimmed.substring(0, 320) : trimmed;
    }

    private static final class AttemptState {
        private final ArrayDeque<Long> failures = new ArrayDeque<>();
        private long blockedUntil;

        synchronized long retryAfterMillis(long now, long windowMillis) {
            prune(now, windowMillis);
            return Math.max(0, blockedUntil - now);
        }

        synchronized void recordFailure(long now, long windowMillis, int maximumFailures, long blockMillis) {
            prune(now, windowMillis);
            if (blockedUntil > now) {
                return;
            }
            failures.addLast(now);
            if (failures.size() >= maximumFailures) {
                blockedUntil = now + blockMillis;
                failures.clear();
            }
        }

        synchronized boolean isExpired(long now, long windowMillis) {
            prune(now, windowMillis);
            return failures.isEmpty() && blockedUntil <= now;
        }

        private void prune(long now, long windowMillis) {
            while (!failures.isEmpty() && failures.peekFirst() <= now - windowMillis) {
                failures.removeFirst();
            }
            if (blockedUntil <= now) {
                blockedUntil = 0;
            }
        }
    }
}
