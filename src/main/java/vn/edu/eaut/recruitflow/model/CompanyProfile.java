package vn.edu.eaut.recruitflow.model;

import java.util.ArrayList;
import java.util.List;

/** Public employer profile backed by an organization's recruitment department. */
public class CompanyProfile {
    private int id;
    private String name;
    private String industry;
    private String logoFile;
    private String location;
    private String size;
    private String website;
    private String description;
    private boolean verified;
    private long openJobs;
    private List<String> highlights = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getLogoFile() { return logoFile; }
    public void setLogoFile(String logoFile) { this.logoFile = logoFile; }
    public boolean isUploadedLogo() { return logoFile != null && logoFile.startsWith("company_"); }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public long getOpenJobs() { return openJobs; }
    public void setOpenJobs(long openJobs) { this.openJobs = openJobs; }
    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights == null ? new ArrayList<>() : new ArrayList<>(highlights); }
}
