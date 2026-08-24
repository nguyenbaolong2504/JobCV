package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

import java.sql.Timestamp;

/** Minimal verification data collected for a self-registered recruiter account. */
public class RecruiterProfile {
    private int id;
    private int userId;
    private String organizationName;
    private String jobTitle;
    private String workPhone;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getOrganizationName() { return repairLegacyMojibake(organizationName); }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getJobTitle() { return repairLegacyMojibake(jobTitle); }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getWorkPhone() { return workPhone; }
    public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
