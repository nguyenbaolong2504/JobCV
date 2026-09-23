package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.model.Job;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JobCapacityPolicyTest {
    private final JobCapacityPolicy policy = new JobCapacityPolicy();
    private final LocalDate today = LocalDate.of(2026, 8, 29);

    @Test
    void closesPublishedJobWhenCapacityIsReached() {
        JobCapacityPolicy.Decision decision = policy.evaluate(job("PUBLISHED", false, today.plusDays(1)), 2, today);
        assertNotNull(decision);
        assertEquals("CLOSED", decision.status());
        assertTrue(decision.autoClosed());
    }

    @Test
    void reopensOnlyAutoClosedUnexpiredJobWhenSlotIsReleased() {
        JobCapacityPolicy.Decision decision = policy.evaluate(job("CLOSED", true, today), 1, today);
        assertNotNull(decision);
        assertEquals("PUBLISHED", decision.status());
        assertFalse(decision.autoClosed());
    }

    @Test
    void doesNotReopenManualClosure() {
        assertNull(policy.evaluate(job("CLOSED", false, today.plusDays(1)), 1, today));
    }

    @Test
    void doesNotReopenExpiredJob() {
        assertNull(policy.evaluate(job("CLOSED", true, today.minusDays(1)), 1, today));
    }

    private Job job(String status, boolean autoClosed, LocalDate deadline) {
        Job job = new Job();
        job.setNumberOfPositions(2);
        job.setStatus(status);
        job.setAutoClosed(autoClosed);
        job.setDeadline(Date.valueOf(deadline));
        return job;
    }
}
