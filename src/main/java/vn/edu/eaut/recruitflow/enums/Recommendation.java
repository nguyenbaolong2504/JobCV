package vn.edu.eaut.recruitflow.enums;

public enum Recommendation {
    STRONG_HIRE,
    HIRE,
    CONSIDER,
    NO_HIRE;

    public static Recommendation fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
