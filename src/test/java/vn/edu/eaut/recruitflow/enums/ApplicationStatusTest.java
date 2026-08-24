package vn.edu.eaut.recruitflow.enums;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.util.StatusRules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationStatusTest {
    @Test
    void enumAndCentralRulesStayInSync() {
        for (ApplicationStatus current : ApplicationStatus.values()) {
            for (ApplicationStatus target : ApplicationStatus.values()) {
                assertEquals(current.canTransitionTo(target), StatusRules.canTransition(current, target),
                        () -> current + " -> " + target + " differs between rule implementations");
            }
        }
    }

    @Test
    void terminalStatesCannotTransition() {
        for (ApplicationStatus terminal : new ApplicationStatus[]{
                ApplicationStatus.HIRED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN}) {
            for (ApplicationStatus target : ApplicationStatus.values()) {
                assertFalse(terminal.canTransitionTo(target));
            }
        }
    }

    @Test
    void parsesCaseAndRejectsUnknownStatus() {
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED,
                ApplicationStatus.fromValue(" interview_scheduled "));
        assertTrue(ApplicationStatus.SUBMITTED.canTransitionTo(ApplicationStatus.SCREENING));
        assertThrows(IllegalArgumentException.class, () -> ApplicationStatus.fromValue("unknown"));
    }
}
