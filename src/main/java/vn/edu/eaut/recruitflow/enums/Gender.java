package vn.edu.eaut.recruitflow.enums;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    public static Gender fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
