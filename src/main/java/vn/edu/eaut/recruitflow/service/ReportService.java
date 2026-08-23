package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
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

    public ReportService() {
        this(new ApplicationDAO(), new OfferDAO());
    }

    ReportService(ApplicationDAO applicationDAO, OfferDAO offerDAO) {
        this.applicationDAO = applicationDAO;
        this.offerDAO = offerDAO;
    }

    public Map<String, Object> getRecruitmentReport(LocalDate fromDate, LocalDate toDate) throws BusinessException {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException("Từ ngày không được sau đến ngày.");
        }
        try {
            Map<String, Long> byStatus = applicationDAO.countGroupedByStatus(fromDate, toDate);
            long applied = applicationDAO.countAll(fromDate, toDate);
            long screening = byStatus.getOrDefault(ApplicationStatus.SCREENING.name(), 0L);
            long interviewed = byStatus.getOrDefault(ApplicationStatus.INTERVIEWED.name(), 0L)
                    + byStatus.getOrDefault(ApplicationStatus.INTERVIEW_SCHEDULED.name(), 0L);
            long offered = byStatus.getOrDefault(ApplicationStatus.OFFERED.name(), 0L);
            long hired = byStatus.getOrDefault(ApplicationStatus.HIRED.name(), 0L);
            long shortlisted = byStatus.getOrDefault(ApplicationStatus.SHORTLISTED.name(), 0L);
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalApplications", applied);
            report.put("applied", applied);
            report.put("submitted", byStatus.getOrDefault(ApplicationStatus.SUBMITTED.name(), 0L));
            report.put("shortlisted", shortlisted);
            report.put("screening", screening);
            report.put("interview", interviewed);
            report.put("interviewed", interviewed);
            report.put("offered", offered);
            report.put("hired", hired);
            // The report is an application-submission cohort. Keep this metric on that same
            // cohort rather than counting every SENT offer in the entire database.
            report.put("offersSent", offerDAO.countIssuedByApplicationDateRange(fromDate, toDate));
            report.put("shortlistRate", rate(shortlisted, applied));
            report.put("screeningRate", rate(screening, applied));
            report.put("interviewRate", rate(interviewed, applied));
            report.put("offerRate", rate(offered, applied));
            report.put("hireRate", rate(hired, applied));
            report.put("applicationsByMonth", applicationDAO.countByMonth(fromDate, toDate));
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
