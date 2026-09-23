package vn.edu.eaut.recruitflow.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobCapacityTest {
    @Test
    void remainingPositionsSubtractsEveryActiveApplication() {
        Job job = new Job();
        job.setNumberOfPositions(2);
        job.setActiveApplications(1);

        assertEquals(1, job.getRemainingPositions());
    }

    @Test
    void remainingPositionsNeverBecomesNegative() {
        Job job = new Job();
        job.setNumberOfPositions(2);
        job.setActiveApplications(4);

        assertEquals(0, job.getRemainingPositions());
    }
}
