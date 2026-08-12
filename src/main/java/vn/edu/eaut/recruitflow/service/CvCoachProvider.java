package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.model.CvReviewResult;

/** Provider boundary: a local rules engine can be replaced by an approved AI provider later. */
public interface CvCoachProvider {
    CvReviewResult review(String resumeText, String reviewGoal, String targetRole) throws AiProviderException;

    String getName();
}
