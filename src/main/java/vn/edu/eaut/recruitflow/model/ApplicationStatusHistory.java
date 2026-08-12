package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

public class ApplicationStatusHistory {
    private int id;
    private int applicationId;
    private String oldStatus;
    private String newStatus;
    private Integer changedBy;
    private String changedByName;
    private String remarks;
    private Timestamp changedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    /** View-friendly alias used by the application timeline. */
    public String getStatus() { return newStatus; }
    public Integer getChangedBy() { return changedBy; }
    public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }
    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    /** View-friendly alias used by the application timeline. */
    public String getNote() { return remarks; }
    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }
}
