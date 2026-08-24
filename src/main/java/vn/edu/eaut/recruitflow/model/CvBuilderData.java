package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

/**
 * Candidate-supplied content used to generate a DOCX CV from one of the built-in templates.
 * This is deliberately separate from {@link CandidateProfile}: creating a CV must not silently
 * overwrite the candidate's profile data.
 */
public class CvBuilderData {
    private String template;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String targetRole;
    private String summary;
    private String skills;
    private String experience;
    private String education;
    private String projects;
    private String certifications;
    private boolean makeDefault;

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getFullName() { return repairLegacyMojibake(fullName); }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return repairLegacyMojibake(location); }
    public void setLocation(String location) { this.location = location; }
    public String getTargetRole() { return repairLegacyMojibake(targetRole); }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getSummary() { return repairLegacyMojibake(summary); }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSkills() { return repairLegacyMojibake(skills); }
    public void setSkills(String skills) { this.skills = skills; }
    public String getExperience() { return repairLegacyMojibake(experience); }
    public void setExperience(String experience) { this.experience = experience; }
    public String getEducation() { return repairLegacyMojibake(education); }
    public void setEducation(String education) { this.education = education; }
    public String getProjects() { return repairLegacyMojibake(projects); }
    public void setProjects(String projects) { this.projects = projects; }
    public String getCertifications() { return repairLegacyMojibake(certifications); }
    public void setCertifications(String certifications) { this.certifications = certifications; }
    public boolean isMakeDefault() { return makeDefault; }
    public void setMakeDefault(boolean makeDefault) { this.makeDefault = makeDefault; }
}
