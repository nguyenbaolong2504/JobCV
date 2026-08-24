package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.InterviewDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
import vn.edu.eaut.recruitflow.model.CandidateDashboardStats;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.HRDashboardStats;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {
    private final ApplicationDAO applicationDAO;
    private final InterviewDAO interviewDAO;
    private final OfferDAO offerDAO;
    private final JobDAO jobDAO;
    private final CandidateProfileService profileService;
    private final UserDAO userDAO;

    public DashboardService() {
        this(new ApplicationDAO(), new InterviewDAO(), new OfferDAO(), new JobDAO(),
                new CandidateProfileService(), new UserDAO());
    }

    DashboardService(ApplicationDAO applicationDAO, InterviewDAO interviewDAO, OfferDAO offerDAO, JobDAO jobDAO,
                     CandidateProfileService profileService, UserDAO userDAO) {
        this.applicationDAO = applicationDAO;
        this.interviewDAO = interviewDAO;
        this.offerDAO = offerDAO;
        this.jobDAO = jobDAO;
        this.profileService = profileService;
        this.userDAO = userDAO;
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
        return getHrDashboardStats(null);
    }

    public HRDashboardStats getHrDashboardStats(int actorId) throws BusinessException {
        try {
            User actor = userDAO.findById(actorId);
            if (actor == null || !("HR".equals(actor.getRoleName()) || "ADMIN".equals(actor.getRoleName()))) {
                throw new BusinessException("Bạn không có quyền xem báo cáo tuyển dụng.");
            }
            return getHrDashboardStats("ADMIN".equals(actor.getRoleName()) ? null : actorId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền xem dashboard.", exception);
        }
    }

    private HRDashboardStats getHrDashboardStats(Integer ownerId) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            HRDashboardStats stats = new HRDashboardStats();
            stats.setActiveJobs(jobDAO.count(null, null, null, null, "PUBLISHED", ownerId));
            stats.setTotalApplications(applicationDAO.count(null, null, null, null, ownerId));
            stats.setUpcomingInterviews(interviewDAO.countUpcoming(ownerId));
            stats.setOffersSent(offerDAO.countByStatus(OfferStatus.SENT.name(), ownerId));
            stats.setDraftOffers(offerDAO.countByStatus(OfferStatus.DRAFT.name(), ownerId));
            stats.setAcceptedOffers(offerDAO.countByStatus(OfferStatus.ACCEPTED.name(), ownerId));
            long declinedOffers = offerDAO.countByStatus(OfferStatus.DECLINED.name(), ownerId);
            Map<String, Long> statusCounts = applicationDAO.countGroupedByStatus(ownerId);
            stats.setNewApplications(statusCounts.getOrDefault(ApplicationStatus.SUBMITTED.name(), 0L));
            stats.setScreeningCandidates(statusCounts.getOrDefault(ApplicationStatus.SCREENING.name(), 0L));
            stats.setShortlistedCandidates(statusCounts.getOrDefault(ApplicationStatus.SHORTLISTED.name(), 0L));
            stats.setInterviewedCandidates(statusCounts.getOrDefault(ApplicationStatus.INTERVIEWED.name(), 0L));
            stats.setHiredCandidates(statusCounts.getOrDefault(ApplicationStatus.HIRED.name(), 0L));
            stats.setHiringRate(percentage(stats.getHiredCandidates(), stats.getTotalApplications()));
            stats.setOfferAcceptanceRate(percentage(stats.getAcceptedOffers(), stats.getAcceptedOffers() + declinedOffers));
            stats.setApplicationStatus(statusCounts);
            stats.setApplicationsByMonth(fillRecentMonths(applicationDAO.countByMonth(6, ownerId), 6));
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

    private int percentage(long part, long total) {
        return total <= 0 ? 0 : (int) Math.round(part * 100.0 / total);
    }

    private Map<String, Long> fillRecentMonths(Map<String, Long> rawValues, int numberOfMonths) {
        Map<String, Long> result = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth firstMonth = YearMonth.now().minusMonths(numberOfMonths - 1L);
        for (int index = 0; index < numberOfMonths; index++) {
            String key = firstMonth.plusMonths(index).format(formatter);
            result.put(key, rawValues.getOrDefault(key, 0L));
        }
        return result;
    }
}
