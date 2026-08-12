package vn.edu.eaut.recruitflow.enums;

public enum InterviewType {
    ONLINE,
    OFFLINE;

    public static InterviewType fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
