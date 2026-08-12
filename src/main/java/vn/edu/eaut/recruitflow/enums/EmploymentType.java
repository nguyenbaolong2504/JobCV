package vn.edu.eaut.recruitflow.enums;

public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    INTERNSHIP,
    CONTRACT,
    REMOTE;

    public static EmploymentType fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
