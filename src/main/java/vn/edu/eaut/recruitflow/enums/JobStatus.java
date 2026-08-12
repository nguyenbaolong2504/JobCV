package vn.edu.eaut.recruitflow.enums;

public enum JobStatus {
    DRAFT,
    PUBLISHED,
    CLOSED,
    ARCHIVED;

    public static JobStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
