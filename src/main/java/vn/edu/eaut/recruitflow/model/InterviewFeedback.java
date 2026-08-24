package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class InterviewFeedback {
    private int id;
    private int interviewId;
    private BigDecimal technicalScore;
    private BigDecimal communicationScore;
    private BigDecimal experienceScore;
    private BigDecimal attitudeScore;
    private BigDecimal overallScore;
    private String comment;
    private String recommendation;
    private Timestamp submittedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getInterviewId() { return interviewId; }
    public void setInterviewId(int interviewId) { this.interviewId = interviewId; }
    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
    public BigDecimal getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(BigDecimal communicationScore) { this.communicationScore = communicationScore; }
    public BigDecimal getExperienceScore() { return experienceScore; }
    public void setExperienceScore(BigDecimal experienceScore) { this.experienceScore = experienceScore; }
    public BigDecimal getAttitudeScore() { return attitudeScore; }
    public void setAttitudeScore(BigDecimal attitudeScore) { this.attitudeScore = attitudeScore; }
    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }
    public String getComment() { return repairLegacyMojibake(comment); }
    public void setComment(String comment) { this.comment = comment; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }
}
