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
    public String getDisplayName() {
        if (name == null) return "";
        return switch (name) {
            case "Information Technology" -> "Công nghệ thông tin";
            case "Human Resources" -> "Nhân sự";
            case "Customer Service" -> "Chăm sóc khách hàng";
            case "Finance" -> "Tài chính";
            case "Sales" -> "Kinh doanh";
            case "Design" -> "Thiết kế";
            case "Operations" -> "Vận hành";
            case "Construction" -> "Xây dựng";
            default -> name;
        };
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
