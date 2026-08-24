package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Interview {
    private int id;
    private int applicationId;
    private int interviewerId;
    private String interviewType;
    private Date interviewDate;
    private Time startTime;
    private Time endTime;
    private String location;
    private String meetingUrl;
    private String status;
    private String note;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String interviewerName;
    private String candidateName;
    private String jobTitle;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public int getInterviewerId() { return interviewerId; }
    public void setInterviewerId(int interviewerId) { this.interviewerId = interviewerId; }
    public String getInterviewType() { return interviewType; }
    public void setInterviewType(String interviewType) { this.interviewType = interviewType; }
    /** Short alias retained for JSP form and card bindings. */
    public String getType() { return interviewType; }
    public Date getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Date interviewDate) { this.interviewDate = interviewDate; }
    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }
    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
    public String getLocation() { return repairLegacyMojibake(location); }
    public void setLocation(String location) { this.location = location; }
    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return repairLegacyMojibake(note); }
    public void setNote(String note) { this.note = note; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getInterviewerName() { return repairLegacyMojibake(interviewerName); }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }
    public String getCandidateName() { return repairLegacyMojibake(candidateName); }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getJobTitle() { return repairLegacyMojibake(jobTitle); }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
}
