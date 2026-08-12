package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

public class Department {
    private int id;
    private String name;
    private String description;
    private Timestamp createdAt;

    public Department() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
