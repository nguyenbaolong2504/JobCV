package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

import java.sql.Timestamp;

/** Minimal verification data collected for a self-registered recruiter account. */
public class RecruiterProfile {
    private int id;
    private int userId;
    private String organizationName;
    private String jobTitle;
    private String workPhone;
    private String industry;
    private String companySize;
    private String address;
    private String website;
    private String description;
    private String logoPath;
    private String coverPath;
    private boolean verified;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getOrganizationName() { return repairLegacyMojibake(organizationName); }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getJobTitle() { return repairLegacyMojibake(jobTitle); }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getWorkPhone() { return workPhone; }
    public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }
    public String getIndustry() { return repairLegacyMojibake(industry); }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCompanySize() { return repairLegacyMojibake(companySize); }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public String getAddress() { return repairLegacyMojibake(address); }
    public void setAddress(String address) { this.address = address; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDescription() { return repairLegacyMojibake(description); }
    public void setDescription(String description) { this.description = description; }
    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
