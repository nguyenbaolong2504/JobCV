package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.InterviewDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
import vn.edu.eaut.recruitflow.model.CandidateDashboardStats;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.HRDashboardStats;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {
    private final ApplicationDAO applicationDAO;
    private final InterviewDAO interviewDAO;
    private final OfferDAO offerDAO;
    private final JobDAO jobDAO;
    private final CandidateProfileService profileService;

    public DashboardService() {
        this(new ApplicationDAO(), new InterviewDAO(), new OfferDAO(), new JobDAO(), new CandidateProfileService());
    }

    DashboardService(ApplicationDAO applicationDAO, InterviewDAO interviewDAO, OfferDAO offerDAO, JobDAO jobDAO,
                     CandidateProfileService profileService) {
        this.applicationDAO = applicationDAO;
        this.interviewDAO = interviewDAO;
        this.offerDAO = offerDAO;
        this.jobDAO = jobDAO;
        this.profileService = profileService;
    }

    public CandidateDashboardStats getCandidateDashboardStats(int candidateId) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            CandidateDashboardStats stats = new CandidateDashboardStats();
            stats.setTotalApplications(applicationDAO.countByCandidateId(candidateId));
            stats.setUpcomingInterviews(interviewDAO.countUpcomingByCandidateId(candidateId));
            // The dashboard wording is "offers awaiting a response"; historical accepted,
            // declined, and expired offers remain visible in the list but must not trigger this alert.
            stats.setOffers(offerDAO.countPendingByCandidateId(candidateId));
            CandidateProfile profile = profileService.getProfile(candidateId);
            stats.setProfileCompletion(profileService.completion(profile));
            return stats;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thống kê ứng viên.", exception);
        }
    }

    public HRDashboardStats getHrDashboardStats() throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            HRDashboardStats stats = new HRDashboardStats();
            stats.setActiveJobs(jobDAO.countActiveJobs());
            stats.setTotalApplications(applicationDAO.countAll());
            stats.setScreeningCandidates(applicationDAO.countByStatus(ApplicationStatus.SCREENING.name()));
            stats.setUpcomingInterviews(interviewDAO.countUpcoming());
            stats.setOffersSent(offerDAO.countByStatus(OfferStatus.SENT.name()));
            stats.setHiredCandidates(applicationDAO.countByStatus(ApplicationStatus.HIRED.name()));
            Map<String, Long> statusCounts = applicationDAO.countGroupedByStatus();
            stats.setApplicationStatus(statusCounts);
            stats.setApplicationsByMonth(applicationDAO.countByMonth(6));
            Map<String, Long> funnel = new LinkedHashMap<>();
            // "Đã nộp" is a current pipeline state, not the aggregate total.
            // Keep totalApplications for the summary card and exclude withdrawn/rejected records here.
            funnel.put("SUBMITTED", statusCounts.getOrDefault(ApplicationStatus.SUBMITTED.name(), 0L));
            funnel.put("SCREENING", statusCounts.getOrDefault(ApplicationStatus.SCREENING.name(), 0L));
            funnel.put("INTERVIEW", statusCounts.getOrDefault(ApplicationStatus.INTERVIEW_SCHEDULED.name(), 0L)
                    + statusCounts.getOrDefault(ApplicationStatus.INTERVIEWED.name(), 0L));
            funnel.put("OFFERED", statusCounts.getOrDefault(ApplicationStatus.OFFERED.name(), 0L));
            funnel.put("HIRED", statusCounts.getOrDefault(ApplicationStatus.HIRED.name(), 0L));
            stats.setRecruitmentFunnel(funnel);
            return stats;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải HR dashboard.", exception);
        }
    }
}
