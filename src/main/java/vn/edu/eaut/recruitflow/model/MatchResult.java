package vn.edu.eaut.recruitflow.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class MatchResult {
    private final BigDecimal matchScore;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;

    public MatchResult(BigDecimal matchScore, List<String> matchedSkills, List<String> missingSkills) {
        this.matchScore = matchScore == null ? BigDecimal.ZERO : matchScore;
        this.matchedSkills = matchedSkills == null ? Collections.emptyList() : List.copyOf(matchedSkills);
        this.missingSkills = missingSkills == null ? Collections.emptyList() : List.copyOf(missingSkills);
    }

    public BigDecimal getMatchScore() { return matchScore; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
}
