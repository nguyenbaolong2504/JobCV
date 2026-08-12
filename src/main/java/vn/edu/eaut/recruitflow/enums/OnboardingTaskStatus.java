package vn.edu.eaut.recruitflow.enums;

public enum OnboardingTaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    public static OnboardingTaskStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
