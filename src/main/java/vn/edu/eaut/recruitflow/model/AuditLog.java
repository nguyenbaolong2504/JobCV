package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

import java.sql.Timestamp;

public class AuditLog {
    private int id;
    private Integer userId;
    private String action;
    private String entityName;
    private Integer entityId;
    private String details;
    private String ipAddress;
    private Timestamp createdAt;
    private String userName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getAction() { return repairLegacyMojibake(action); }
    public void setAction(String action) { this.action = action; }
    public String getEntityName() { return repairLegacyMojibake(entityName); }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
    public String getDetails() { return repairLegacyMojibake(details); }
    public void setDetails(String details) { this.details = details; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getUserName() { return repairLegacyMojibake(userName); }
    public void setUserName(String userName) { this.userName = userName; }
}
