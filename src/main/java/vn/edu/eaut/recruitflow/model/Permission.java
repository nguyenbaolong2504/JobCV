package vn.edu.eaut.recruitflow.model;

/** Persisted metadata for one granular role permission. */
public class Permission {
    private int id;
    private String permissionCode;
    private String module;
    private String displayName;
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
