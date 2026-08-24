package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

public class JobSkill {
    private int id;
    private int jobId;
    private String skillName;
    private int weight;
    private boolean required;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public String getSkillName() { return repairLegacyMojibake(skillName); }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public boolean isRequired() { return required; }
    public boolean getIsRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
