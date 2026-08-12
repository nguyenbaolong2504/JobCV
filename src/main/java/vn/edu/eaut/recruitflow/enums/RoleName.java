package vn.edu.eaut.recruitflow.enums;

public enum RoleName {
    ADMIN,
    HR,
    INTERVIEWER,
    CANDIDATE;

    public static RoleName fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
