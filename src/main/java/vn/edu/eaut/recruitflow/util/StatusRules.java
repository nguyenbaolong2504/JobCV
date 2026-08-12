package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.enums.ApplicationStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/** Central immutable application workflow; controllers may never bypass it. */
public final class StatusRules {
    private static final Map<ApplicationStatus, EnumSet<ApplicationStatus>> APPLICATION_TRANSITIONS = new EnumMap<>(ApplicationStatus.class);

    static {
        APPLICATION_TRANSITIONS.put(ApplicationStatus.SUBMITTED, EnumSet.of(ApplicationStatus.SCREENING, ApplicationStatus.WITHDRAWN));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.SCREENING, EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.SHORTLISTED, EnumSet.of(ApplicationStatus.INTERVIEW_SCHEDULED, ApplicationStatus.REJECTED));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.INTERVIEW_SCHEDULED, EnumSet.of(ApplicationStatus.INTERVIEWED, ApplicationStatus.REJECTED));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.INTERVIEWED, EnumSet.of(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.OFFERED, EnumSet.of(ApplicationStatus.HIRED, ApplicationStatus.REJECTED));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.HIRED, EnumSet.noneOf(ApplicationStatus.class));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class));
        APPLICATION_TRANSITIONS.put(ApplicationStatus.WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));
    }

    private StatusRules() {
    }

    public static boolean canTransition(ApplicationStatus current, ApplicationStatus target) {
        return current != null && target != null && APPLICATION_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(ApplicationStatus.class)).contains(target);
    }
}
