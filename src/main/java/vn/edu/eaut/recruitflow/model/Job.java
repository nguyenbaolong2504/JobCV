package vn.edu.eaut.recruitflow.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.enums.JobStatus;

public class Job {
    private int id;
    private String jobCode;
    private String title;
    private int departmentId;
    private String departmentName;
    private String location;
    private String employmentType;
    private int numberOfPositions;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
    private String requirements;
    private int experienceRequired;
    private Date deadline;
    private String status;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<JobSkill> skills = new ArrayList<>();

    public Job() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getJobCode() { return jobCode; }
    public void setJobCode(String jobCode) { this.jobCode = jobCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public EmploymentType getEmploymentTypeEnum() { return EmploymentType.fromValue(employmentType); }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType == null ? null : employmentType.name(); }
    public int getNumberOfPositions() { return numberOfPositions; }
    public void setNumberOfPositions(int numberOfPositions) { this.numberOfPositions = numberOfPositions; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public int getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(int experienceRequired) { this.experienceRequired = experienceRequired; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public JobStatus getJobStatus() { return JobStatus.fromValue(status); }
    public void setStatus(JobStatus status) { this.status = status == null ? null : status.name(); }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public List<JobSkill> getSkills() { return skills; }
    public void setSkills(List<JobSkill> skills) { this.skills = skills == null ? new ArrayList<>() : new ArrayList<>(skills); }
}
