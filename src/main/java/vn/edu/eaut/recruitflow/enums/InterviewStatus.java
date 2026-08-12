package vn.edu.eaut.recruitflow.enums;

public enum InterviewStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    RESCHEDULED;

    public static InterviewStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
