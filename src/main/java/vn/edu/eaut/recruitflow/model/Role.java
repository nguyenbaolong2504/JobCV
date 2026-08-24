package vn.edu.eaut.recruitflow.model;

import static vn.edu.eaut.recruitflow.util.VietnameseTextUtil.repairLegacyMojibake;

public class Role {
    private int id;
    private String roleName;
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return repairLegacyMojibake(description); }
    public void setDescription(String description) { this.description = description; }
}
