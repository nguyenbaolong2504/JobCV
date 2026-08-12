package vn.edu.eaut.recruitflow.model;

public class CandidateDashboardStats {
    private long totalApplications;
    private long upcomingInterviews;
    private long offers;
    private int profileCompletion;

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }
    public long getUpcomingInterviews() { return upcomingInterviews; }
    public void setUpcomingInterviews(long upcomingInterviews) { this.upcomingInterviews = upcomingInterviews; }
    public long getOffers() { return offers; }
    public void setOffers(long offers) { this.offers = offers; }
    public int getProfileCompletion() { return profileCompletion; }
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = Math.max(0, Math.min(100, profileCompletion)); }
}
