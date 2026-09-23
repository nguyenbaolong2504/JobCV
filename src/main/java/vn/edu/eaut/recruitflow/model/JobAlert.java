package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

/** Saved candidate search criteria used to revisit matching published jobs. */
public class JobAlert {
    private int id;
    private int candidateId;
    private String name;
    private String keyword;
    private Integer departmentId;
    private String departmentName;
    private String location;
    private String employmentType;
    private String frequency;
    private boolean active;
    private Timestamp lastNotifiedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private long matchingJobs;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getLastNotifiedAt() { return lastNotifiedAt; }
    public void setLastNotifiedAt(Timestamp lastNotifiedAt) { this.lastNotifiedAt = lastNotifiedAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public long getMatchingJobs() { return matchingJobs; }
    public void setMatchingJobs(long matchingJobs) { this.matchingJobs = Math.max(0, matchingJobs); }
}
