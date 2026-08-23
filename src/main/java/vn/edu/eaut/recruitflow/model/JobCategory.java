package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * A curated career taxonomy used to help job seekers browse published jobs.
 *
 * <p>The product intentionally supports two levels: a top-level career group
 * (for example, "Công nghệ thông tin") and optional specialist categories
 * below it (for example, "Phát triển phần mềm").  Jobs are associated with a
 * leaf category only, so selecting a group can reliably include all of its
 * specialisations.</p>
 */
public class JobCategory {
    private int id;
    private Integer parentId;
    private String parentName;
    private String name;
    private String description;
    private int displayOrder;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<JobCategory> children = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<JobCategory> getChildren() { return children; }
    public void setChildren(List<JobCategory> children) {
        this.children = children == null ? new ArrayList<>() : new ArrayList<>(children);
    }

    public boolean isRoot() { return parentId == null; }
}
