package vn.edu.eaut.recruitflow.enums;

public enum UserStatus {
    ACTIVE,
    LOCKED,
    INACTIVE;

    public static UserStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
