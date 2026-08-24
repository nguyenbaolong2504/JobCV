package vn.edu.eaut.recruitflow.util;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.enums.PermissionCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionPolicyTest {
    @Test
    void protectsEveryResumeCreationAndInterviewResumeEndpoint() {
        assertEquals(PermissionCode.CANDIDATE_RESUMES_MANAGE,
                PermissionPolicy.requiredForPath("/candidate/cv-builder"));
        assertEquals(PermissionCode.CANDIDATE_RESUMES_MANAGE,
                PermissionPolicy.requiredForPath("/candidate/cv-builder/create"));
        assertEquals(PermissionCode.INTERVIEWER_INTERVIEWS_VIEW,
                PermissionPolicy.requiredForPath("/interviewer/resumes/download"));
    }
}
