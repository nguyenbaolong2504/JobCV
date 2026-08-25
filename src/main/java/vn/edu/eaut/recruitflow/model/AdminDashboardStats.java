package vn.edu.eaut.recruitflow.model;

public class AdminDashboardStats {
    private long totalUsers;
    private long activeUsers;
    private long lockedUsers;
    private long totalDepartments;
    private long totalJobs;
    private long totalApplications;
    private long candidates;
    private long employers;
    private long activeJobs;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    public long getLockedUsers() { return lockedUsers; }
    public void setLockedUsers(long lockedUsers) { this.lockedUsers = lockedUsers; }
    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }
    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }
    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }
    public long getCandidates() { return candidates; }
    public void setCandidates(long candidates) { this.candidates = candidates; }
    public long getEmployers() { return employers; }
    public void setEmployers(long employers) { this.employers = employers; }
    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }
}
