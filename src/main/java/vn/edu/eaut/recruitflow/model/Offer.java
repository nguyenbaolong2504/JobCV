package vn.edu.eaut.recruitflow.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Offer {
    private int id;
    private int applicationId;
    private BigDecimal salary;
    private Date startDate;
    private int probationMonths;
    private String location;
    private Date expiryDate;
    private String status;
    private String note;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int candidateId;
    private String candidateName;
    private String jobTitle;
    private String departmentName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public int getProbationMonths() { return probationMonths; }
    public void setProbationMonths(int probationMonths) { this.probationMonths = probationMonths; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
