package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.dao.CompanyDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Recruitment funnel report, optionally limited to applications submitted in a selected period. */
public class ReportService {
    private final ApplicationDAO applicationDAO;
    private final OfferDAO offerDAO;
    private final UserDAO userDAO;
    private final CompanyDAO companyDAO = new CompanyDAO();

    public ReportService() {
        this(new ApplicationDAO(), new OfferDAO(), new UserDAO());
    }

    ReportService(ApplicationDAO applicationDAO, OfferDAO offerDAO, UserDAO userDAO) {
        this.applicationDAO = applicationDAO;
        this.offerDAO = offerDAO;
        this.userDAO = userDAO;
    }

    public Map<String, Object> getRecruitmentReport(LocalDate fromDate, LocalDate toDate) throws BusinessException {
        return buildRecruitmentReport(fromDate, toDate, null);
    }

    /** Returns only the current recruiter's data; administrators retain the global view. */
    public Map<String, Object> getRecruitmentReport(LocalDate fromDate, LocalDate toDate, int actorId)
            throws BusinessException {
        try {
            User actor = userDAO.findById(actorId);
            if (actor == null || !("HR".equals(actor.getRoleName()) || "ADMIN".equals(actor.getRoleName()))) {
                throw new BusinessException("Bạn không có quyền xem báo cáo tuyển dụng.");
            }
            Integer ownerId = "ADMIN".equals(actor.getRoleName()) ? null : companyDAO.findCompanyIdByUserId(actorId);
            if (!"ADMIN".equals(actor.getRoleName()) && ownerId == null) throw new BusinessException("HR chưa được liên kết với công ty.");
            return buildRecruitmentReport(fromDate, toDate, ownerId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền xem báo cáo.", exception);
        }
    }

    private Map<String, Object> buildRecruitmentReport(LocalDate fromDate, LocalDate toDate, Integer ownerId)
            throws BusinessException {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException("Từ ngày không được sau đến ngày.");
        }
        try {
            Map<String, Long> byStatus = applicationDAO.countGroupedByStatus(fromDate, toDate, ownerId);
            long applied = applicationDAO.countAll(fromDate, toDate, ownerId);
            long screening = byStatus.getOrDefault(ApplicationStatus.SCREENING.name(), 0L);
            long interviewed = byStatus.getOrDefault(ApplicationStatus.INTERVIEWED.name(), 0L)
                    + byStatus.getOrDefault(ApplicationStatus.INTERVIEW_SCHEDULED.name(), 0L);
            long offered = byStatus.getOrDefault(ApplicationStatus.OFFERED.name(), 0L);
            long hired = byStatus.getOrDefault(ApplicationStatus.HIRED.name(), 0L);
            long shortlisted = byStatus.getOrDefault(ApplicationStatus.SHORTLISTED.name(), 0L);
            long submitted = byStatus.getOrDefault(ApplicationStatus.SUBMITTED.name(), 0L);
            long interviewScheduled = byStatus.getOrDefault(ApplicationStatus.INTERVIEW_SCHEDULED.name(), 0L);
            long interviewedOnly = byStatus.getOrDefault(ApplicationStatus.INTERVIEWED.name(), 0L);
            long rejected = byStatus.getOrDefault(ApplicationStatus.REJECTED.name(), 0L);
            long withdrawn = byStatus.getOrDefault(ApplicationStatus.WITHDRAWN.name(), 0L);
            long uniqueCandidates = applicationDAO.countDistinctCandidates(fromDate, toDate, ownerId);
            long jobsReceivingApplications = applicationDAO.countDistinctJobs(fromDate, toDate, ownerId);
            BigDecimal averageApplicationsPerJob = jobsReceivingApplications == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(applied * 1.0d / jobsReceivingApplications).setScale(1, RoundingMode.HALF_UP);
            Map<String, Long> applicationsByMonth = applicationDAO.countByMonth(fromDate, toDate, ownerId);
            Map.Entry<String, Long> peakMonth = applicationsByMonth.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).orElse(null);
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalApplications", applied);
            report.put("applied", applied);
            report.put("submitted", submitted);
            report.put("screening", screening);
            report.put("shortlisted", shortlisted);
            report.put("interviewScheduled", interviewScheduled);
            report.put("interviewedOnly", interviewedOnly);
            report.put("interview", interviewed);
            report.put("interviewed", interviewed);
            report.put("offered", offered);
            report.put("hired", hired);
            report.put("rejected", rejected);
            report.put("withdrawn", withdrawn);
            report.put("activePipeline", Math.max(0L, applied - rejected - withdrawn - hired));
            report.put("offersSent", offerDAO.countByStatus(OfferStatus.SENT.name(), ownerId));
            report.put("shortlistRate", rate(shortlisted, applied));
            report.put("screeningRate", rate(screening, applied));
            report.put("interviewRate", rate(interviewed, applied));
            report.put("offerRate", rate(offered, applied));
            report.put("hireRate", rate(hired, applied));
            report.put("rejectionRate", rate(rejected, applied));
            report.put("withdrawalRate", rate(withdrawn, applied));
            report.put("uniqueCandidates", uniqueCandidates);
            report.put("jobsReceivingApplications", jobsReceivingApplications);
            report.put("averageApplicationsPerJob", averageApplicationsPerJob);
            report.put("averageMatchScore", applicationDAO.averageMatchScore(fromDate, toDate, ownerId));
            report.put("applicationsByMonth", applicationsByMonth);
            report.put("applicationsByDepartment", applicationDAO.countByDepartment(fromDate, toDate, ownerId));
            report.put("applicationsByLocation", applicationDAO.countByLocation(fromDate, toDate, ownerId));
            report.put("topJobs", applicationDAO.findTopJobs(fromDate, toDate, ownerId, 5));
            report.put("peakMonth", peakMonth == null ? "—" : peakMonth.getKey());
            report.put("peakMonthApplications", peakMonth == null ? 0L : peakMonth.getValue());
            report.put("fromDate", fromDate);
            report.put("toDate", toDate);
            return report;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tạo báo cáo tuyển dụng.", exception);
        }
    }

    private BigDecimal rate(long numerator, long denominator) {
        return denominator == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(numerator * 100.0d / denominator).setScale(2, RoundingMode.HALF_UP);
    }
}
