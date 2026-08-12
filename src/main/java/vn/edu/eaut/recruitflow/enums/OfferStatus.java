package vn.edu.eaut.recruitflow.enums;

public enum OfferStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    DECLINED,
    EXPIRED;

    public static OfferStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
