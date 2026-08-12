package vn.edu.eaut.recruitflow.model;

import java.util.Collections;
import java.util.List;

/**
 * Safe, presentation-ready output of the CV Coach.  This intentionally contains only
 * suggestions; it never mutates the stored resume or invents candidate experience.
 */
public final class CvReviewResult {
    private final String resumeName;
    private final int score;
    private final String summary;
    private final List<String> strengths;
    private final List<String> improvements;
    private final List<String> missingSections;
    private final List<String> keywordSuggestions;
    private final List<String> suggestedBullets;
    private final String rewrittenSummary;
    private final String provider;
    private final String notice;
    private final String disclaimer;

    public CvReviewResult(String resumeName, int score, String summary, List<String> strengths, List<String> improvements,
                          List<String> missingSections, List<String> keywordSuggestions,
                          List<String> suggestedBullets, String rewrittenSummary, String provider, String notice,
                          String disclaimer) {
        this.resumeName = safeText(resumeName);
        this.score = Math.max(0, Math.min(100, score));
        this.summary = safeText(summary);
        this.strengths = immutable(strengths);
        this.improvements = immutable(improvements);
        this.missingSections = immutable(missingSections);
        this.keywordSuggestions = immutable(keywordSuggestions);
        this.suggestedBullets = immutable(suggestedBullets);
        this.rewrittenSummary = safeText(rewrittenSummary);
        this.provider = safeText(provider);
        this.notice = safeText(notice);
        this.disclaimer = safeText(disclaimer);
    }

    public CvReviewResult withSource(String provider, String notice) {
        return new CvReviewResult(resumeName, score, summary, strengths, improvements, missingSections,
                keywordSuggestions, suggestedBullets, rewrittenSummary, provider, notice, disclaimer);
    }

    public CvReviewResult withResumeName(String resumeName) {
        return new CvReviewResult(resumeName, score, summary, strengths, improvements, missingSections,
                keywordSuggestions, suggestedBullets, rewrittenSummary, provider, notice, disclaimer);
    }

    public String getResumeName() { return resumeName; }
    public int getScore() { return score; }
    /** JSON/UI contract name for the overall quality score. */
    public int getOverallScore() { return score; }
    /** Alias useful for JSPs that prefer a descriptive property name. */
    public int getQualityScore() { return score; }
    public String getSummary() { return summary; }
    public List<String> getStrengths() { return strengths; }
    public List<String> getImprovements() { return improvements; }
    public List<String> getMissingSections() { return missingSections; }
    public List<String> getKeywordSuggestions() { return keywordSuggestions; }
    public List<String> getSuggestedBullets() { return suggestedBullets; }
    public String getRewrittenSummary() { return rewrittenSummary; }
    public String getProvider() { return provider; }
    public String getNotice() { return notice; }
    public String getDisclaimer() { return disclaimer; }
    public boolean isExternalProvider() { return "openai-compatible".equals(provider); }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static List<String> immutable(List<String> values) {
        return values == null ? Collections.emptyList() : List.copyOf(values);
    }
}
