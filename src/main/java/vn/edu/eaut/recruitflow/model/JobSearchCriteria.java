package vn.edu.eaut.recruitflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filters supplied by a job seeker.  Salary bounds intentionally use range
 * overlap: a job is relevant when its advertised range can satisfy at least
 * part of the seeker's requested range.
 */
public class JobSearchCriteria {
    private String keyword;
    private String title;
    private Integer departmentId;
    private Integer categoryId;
    private String location;
    private String employmentType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Integer experienceMin;
    private Integer experienceMax;
    private LocalDate deadlineFrom;
    private LocalDate deadlineTo;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public Integer getExperienceMin() { return experienceMin; }
    public void setExperienceMin(Integer experienceMin) { this.experienceMin = experienceMin; }
    public Integer getExperienceMax() { return experienceMax; }
    public void setExperienceMax(Integer experienceMax) { this.experienceMax = experienceMax; }
    public LocalDate getDeadlineFrom() { return deadlineFrom; }
    public void setDeadlineFrom(LocalDate deadlineFrom) { this.deadlineFrom = deadlineFrom; }
    public LocalDate getDeadlineTo() { return deadlineTo; }
    public void setDeadlineTo(LocalDate deadlineTo) { this.deadlineTo = deadlineTo; }
}
