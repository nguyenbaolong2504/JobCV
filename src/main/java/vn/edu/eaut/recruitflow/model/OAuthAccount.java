package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

/** Links a local account to an immutable identity at an OAuth provider. */
public class OAuthAccount {
    private long id;
    private int userId;
    private String provider;
    private String providerSubject;
    private Timestamp createdAt;
    private Timestamp lastLoginAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderSubject() { return providerSubject; }
    public void setProviderSubject(String providerSubject) { this.providerSubject = providerSubject; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Timestamp lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
