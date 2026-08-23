package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

/** Database representation of a short-lived, single-use password-reset OTP. */
public class PasswordResetOtp {
    private long id;
    private int userId;
    private String otpHash;
    private Timestamp expiresAt;
    private int attemptCount;
    private Timestamp consumedAt;
    private Timestamp createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public Timestamp getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Timestamp consumedAt) { this.consumedAt = consumedAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
