package vn.edu.eaut.recruitflow.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/** A published job and the CV matching explanation used in recommendations. */
public class JobMatchResult {
    private final Job job;
    private final BigDecimal matchScore;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;

    public JobMatchResult(Job job, MatchResult match) {
        this.job = job;
        this.matchScore = match == null ? BigDecimal.ZERO : match.getMatchScore();
        this.matchedSkills = match == null ? Collections.emptyList() : match.getMatchedSkills();
        this.missingSkills = match == null ? Collections.emptyList() : match.getMissingSkills();
    }

    public Job getJob() { return job; }
    public BigDecimal getMatchScore() { return matchScore; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
}
