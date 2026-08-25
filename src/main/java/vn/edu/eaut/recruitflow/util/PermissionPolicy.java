package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.enums.PermissionCode;
import vn.edu.eaut.recruitflow.enums.RoleName;

import java.util.EnumSet;
import java.util.Set;

/** Central URL-to-action mapping and safe defaults while an additive RBAC migration is pending. */
public final class PermissionPolicy {
    private PermissionPolicy() {
    }

    public static PermissionCode requiredForPath(String path) {
        if (path == null) return null;
        if (path.startsWith("/admin/dashboard")) return PermissionCode.ADMIN_DASHBOARD_VIEW;
        if (path.startsWith("/admin/jobs") || path.startsWith("/admin/applications") || path.startsWith("/admin/reports")) return PermissionCode.ADMIN_DASHBOARD_VIEW;
        if (path.startsWith("/admin/companies")) return PermissionCode.ADMIN_USERS_MANAGE;
        if (path.startsWith("/admin/users")) return PermissionCode.ADMIN_USERS_MANAGE;
        if (path.startsWith("/admin/roles") || path.startsWith("/admin/permissions")) return PermissionCode.ADMIN_PERMISSIONS_MANAGE;
        if (path.startsWith("/admin/departments")) return PermissionCode.ADMIN_DEPARTMENTS_MANAGE;
        if (path.startsWith("/admin/categories") || path.startsWith("/admin/job-categories")) {
            return PermissionCode.ADMIN_CATEGORIES_MANAGE;
        }
        if (path.startsWith("/admin/audit-logs")) return PermissionCode.ADMIN_AUDIT_VIEW;

        if (path.startsWith("/hr/dashboard")) return PermissionCode.HR_DASHBOARD_VIEW;
        if (path.startsWith("/hr/company")) return PermissionCode.HR_DASHBOARD_VIEW;
        if (path.startsWith("/hr/jobs")) return PermissionCode.HR_JOBS_MANAGE;
        if (path.startsWith("/hr/applications") || path.startsWith("/hr/resumes")) return PermissionCode.HR_APPLICATIONS_MANAGE;
        if (path.startsWith("/hr/interviews")) return PermissionCode.HR_INTERVIEWS_MANAGE;
        if (path.startsWith("/hr/offers")) return PermissionCode.HR_OFFERS_MANAGE;
        if (path.startsWith("/hr/onboarding")) return PermissionCode.HR_ONBOARDING_MANAGE;
        if (path.startsWith("/hr/reports")) return PermissionCode.HR_REPORTS_VIEW;
        if (path.startsWith("/hr/notifications")) return PermissionCode.HR_NOTIFICATIONS_VIEW;

        if (path.startsWith("/interviewer/dashboard")) return PermissionCode.INTERVIEWER_DASHBOARD_VIEW;
        if (path.startsWith("/interviewer/interviews")) return path.contains("feedback")
                ? PermissionCode.INTERVIEWER_FEEDBACK_SUBMIT : PermissionCode.INTERVIEWER_INTERVIEWS_VIEW;
        // Resume downloads are available only from an interview assignment and must obey the
        // same permission as viewing that assignment. Without this mapping, an interviewer
        // whose interview-view permission was revoked could still fetch candidate CV files.
        if (path.startsWith("/interviewer/resumes")) return PermissionCode.INTERVIEWER_INTERVIEWS_VIEW;
        if (path.startsWith("/interviewer/notifications")) return PermissionCode.INTERVIEWER_NOTIFICATIONS_VIEW;

        if (path.startsWith("/candidate/profile")) return PermissionCode.CANDIDATE_PROFILE_MANAGE;
        // The CV builder creates a stored resume, so it belongs to the resume-management
        // capability rather than remaining reachable after that capability is revoked.
        if (path.startsWith("/candidate/cv-builder")) return PermissionCode.CANDIDATE_RESUMES_MANAGE;
        if (path.startsWith("/candidate/resumes")) return PermissionCode.CANDIDATE_RESUMES_MANAGE;
        if (path.startsWith("/candidate/jobs")) return PermissionCode.CANDIDATE_JOBS_VIEW;
        if (path.startsWith("/candidate/applications")) return PermissionCode.CANDIDATE_APPLICATIONS_MANAGE;
        if (path.startsWith("/candidate/interviews")) return PermissionCode.CANDIDATE_INTERVIEWS_VIEW;
        if (path.startsWith("/candidate/offers")) return PermissionCode.CANDIDATE_OFFERS_RESPOND;
        if (path.startsWith("/candidate/onboarding")) return PermissionCode.CANDIDATE_ONBOARDING_MANAGE;
        if (path.startsWith("/candidate/notifications")) return PermissionCode.CANDIDATE_NOTIFICATIONS_VIEW;
        if (path.startsWith("/candidate/dashboard")) return PermissionCode.CANDIDATE_APPLICATIONS_MANAGE;
        return null;
    }

    public static Set<PermissionCode> defaultPermissions(RoleName role) {
        if (role == null) return Set.of();
        return switch (role) {
            case ADMIN -> EnumSet.allOf(PermissionCode.class);
            case HR -> EnumSet.of(PermissionCode.HR_DASHBOARD_VIEW, PermissionCode.HR_JOBS_MANAGE,
                    PermissionCode.HR_APPLICATIONS_MANAGE, PermissionCode.HR_INTERVIEWS_MANAGE,
                    PermissionCode.HR_OFFERS_MANAGE, PermissionCode.HR_ONBOARDING_MANAGE,
                    PermissionCode.HR_REPORTS_VIEW, PermissionCode.HR_NOTIFICATIONS_VIEW);
            case INTERVIEWER -> EnumSet.of(PermissionCode.INTERVIEWER_DASHBOARD_VIEW,
                    PermissionCode.INTERVIEWER_INTERVIEWS_VIEW, PermissionCode.INTERVIEWER_FEEDBACK_SUBMIT,
                    PermissionCode.INTERVIEWER_NOTIFICATIONS_VIEW);
            case CANDIDATE -> EnumSet.of(PermissionCode.CANDIDATE_JOBS_VIEW, PermissionCode.CANDIDATE_PROFILE_MANAGE,
                    PermissionCode.CANDIDATE_RESUMES_MANAGE, PermissionCode.CANDIDATE_APPLICATIONS_MANAGE,
                    PermissionCode.CANDIDATE_INTERVIEWS_VIEW, PermissionCode.CANDIDATE_OFFERS_RESPOND,
                    PermissionCode.CANDIDATE_ONBOARDING_MANAGE, PermissionCode.CANDIDATE_NOTIFICATIONS_VIEW);
        };
    }
}
