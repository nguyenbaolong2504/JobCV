package vn.edu.eaut.recruitflow.enums;

public enum OnboardingStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    public static OnboardingStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
