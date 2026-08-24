package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Onboarding {
    private int id;
    private int applicationId;
    private String status;
    private BigDecimal progress;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int candidateId;
    private String candidateName;
    private String jobTitle;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) { this.progress = progress; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }
    public String getCandidateName() { return repairLegacyMojibake(candidateName); }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getJobTitle() { return repairLegacyMojibake(jobTitle); }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
}
