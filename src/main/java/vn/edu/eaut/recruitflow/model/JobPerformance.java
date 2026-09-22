package vn.edu.eaut.recruitflow.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Aggregated recruitment performance for one job in a selected reporting period. */
public class JobPerformance {
    private int jobId;
    private String jobCode;
    private String title;
    private int companyId;
    private String companyName;
    private long applications;
    private long shortlisted;
    private long interviews;
    private long offers;
    private long hires;
    private BigDecimal averageMatchScore = BigDecimal.ZERO;

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public String getJobCode() { return jobCode; }
    public void setJobCode(String jobCode) { this.jobCode = jobCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public long getApplications() { return applications; }
    public void setApplications(long applications) { this.applications = applications; }
    public long getShortlisted() { return shortlisted; }
    public void setShortlisted(long shortlisted) { this.shortlisted = shortlisted; }
    public long getInterviews() { return interviews; }
    public void setInterviews(long interviews) { this.interviews = interviews; }
    public long getOffers() { return offers; }
    public void setOffers(long offers) { this.offers = offers; }
    public long getHires() { return hires; }
    public void setHires(long hires) { this.hires = hires; }
    public BigDecimal getAverageMatchScore() { return averageMatchScore; }
    public void setAverageMatchScore(BigDecimal averageMatchScore) {
        this.averageMatchScore = averageMatchScore == null ? BigDecimal.ZERO
                : averageMatchScore.setScale(1, RoundingMode.HALF_UP);
    }
    public BigDecimal getHireRate() {
        return applications == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(hires * 100.0d / applications).setScale(1, RoundingMode.HALF_UP);
    }
}
