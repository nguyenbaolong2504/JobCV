package vn.edu.eaut.recruitflow.enums;

import java.util.EnumSet;

/** The only valid states for a recruitment application. */
public enum ApplicationStatus {
    SUBMITTED,
    SCREENING,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    INTERVIEWED,
    OFFERED,
    HIRED,
    REJECTED,
    WITHDRAWN;

    public boolean canTransitionTo(ApplicationStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case SUBMITTED -> EnumSet.of(SCREENING, WITHDRAWN).contains(target);
            case SCREENING -> EnumSet.of(SHORTLISTED, REJECTED).contains(target);
            case SHORTLISTED -> EnumSet.of(INTERVIEW_SCHEDULED, REJECTED).contains(target);
            case INTERVIEW_SCHEDULED -> EnumSet.of(INTERVIEWED, REJECTED).contains(target);
            case INTERVIEWED -> EnumSet.of(OFFERED, REJECTED).contains(target);
            case OFFERED -> EnumSet.of(HIRED, REJECTED).contains(target);
            case HIRED, REJECTED, WITHDRAWN -> false;
        };
    }

    public static ApplicationStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
