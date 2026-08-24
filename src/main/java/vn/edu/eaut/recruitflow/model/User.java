package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;
import vn.edu.eaut.recruitflow.enums.UserStatus;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

public class User {
    private int id;
    private String email;
    private String passwordHash;
    private String fullName;
    private int roleId;
    private String roleName;
    private String status;
    /**
     * Increments whenever credentials are changed. Authenticated browser sessions store the
     * value observed at sign-in so a password reset can invalidate every older session.
     */
    private int sessionVersion;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return repairLegacyMojibake(fullName); }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    
    public String getRoleName() { return repairLegacyMojibake(roleName); }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UserStatus getUserStatus() { return UserStatus.fromValue(status); }
    public void setStatus(UserStatus status) { this.status = status == null ? null : status.name(); }

    public int getSessionVersion() { return sessionVersion; }
    public void setSessionVersion(int sessionVersion) { this.sessionVersion = Math.max(0, sessionVersion); }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
