package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

public class OnboardingTask {
    private int id;
    private int onboardingId;
    private String taskName;
    private String description;
    private boolean required;
    private String status;
    private Timestamp completedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOnboardingId() { return onboardingId; }
    public void setOnboardingId(int onboardingId) { this.onboardingId = onboardingId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    /** UI-friendly alias for taskName. */
    public String getTitle() { return taskName; }
    public void setTitle(String title) { this.taskName = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRequired() { return required; }
    public boolean getIsRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
