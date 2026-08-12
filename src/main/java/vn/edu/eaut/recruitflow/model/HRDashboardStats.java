package vn.edu.eaut.recruitflow.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class HRDashboardStats {
    private long activeJobs;
    private long totalApplications;
    private long screeningCandidates;
    private long upcomingInterviews;
    private long offersSent;
    private long hiredCandidates;
    private Map<String, Long> applicationsByMonth = new LinkedHashMap<>();
    private Map<String, Long> applicationStatus = new LinkedHashMap<>();
    private Map<String, Long> recruitmentFunnel = new LinkedHashMap<>();

    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }
    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }
    public long getScreeningCandidates() { return screeningCandidates; }
    public void setScreeningCandidates(long screeningCandidates) { this.screeningCandidates = screeningCandidates; }
    public long getUpcomingInterviews() { return upcomingInterviews; }
    public void setUpcomingInterviews(long upcomingInterviews) { this.upcomingInterviews = upcomingInterviews; }
    public long getOffersSent() { return offersSent; }
    public void setOffersSent(long offersSent) { this.offersSent = offersSent; }
    public long getHiredCandidates() { return hiredCandidates; }
    public void setHiredCandidates(long hiredCandidates) { this.hiredCandidates = hiredCandidates; }
    public Map<String, Long> getApplicationsByMonth() { return applicationsByMonth; }
    public void setApplicationsByMonth(Map<String, Long> applicationsByMonth) { this.applicationsByMonth = applicationsByMonth == null ? new LinkedHashMap<>() : new LinkedHashMap<>(applicationsByMonth); }
    public Map<String, Long> getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(Map<String, Long> applicationStatus) { this.applicationStatus = applicationStatus == null ? new LinkedHashMap<>() : new LinkedHashMap<>(applicationStatus); }
    public Map<String, Long> getRecruitmentFunnel() { return recruitmentFunnel; }
    public void setRecruitmentFunnel(Map<String, Long> recruitmentFunnel) { this.recruitmentFunnel = recruitmentFunnel == null ? new LinkedHashMap<>() : new LinkedHashMap<>(recruitmentFunnel); }
}
