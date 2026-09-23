package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Job;

import java.time.LocalDate;

/** Pure policy for automatic capacity-driven job status changes. */
final class JobCapacityPolicy {
    record Decision(String status, boolean autoClosed) {}

    Decision evaluate(Job job, long activeApplications, LocalDate today) {
        if (job == null || job.getNumberOfPositions() < 1) return null;
        if (activeApplications >= job.getNumberOfPositions()
                && JobStatus.PUBLISHED.name().equals(job.getStatus())) {
            return new Decision(JobStatus.CLOSED.name(), true);
        }
        if (activeApplications < job.getNumberOfPositions() && job.isAutoClosed()
                && JobStatus.CLOSED.name().equals(job.getStatus()) && job.getDeadline() != null
                && !job.getDeadline().toLocalDate().isBefore(today)) {
            return new Decision(JobStatus.PUBLISHED.name(), false);
        }
        return null;
    }
}
