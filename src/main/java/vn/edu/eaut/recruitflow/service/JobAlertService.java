package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.JobAlertDAO;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.model.JobAlert;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Manages candidate search alerts and calculates their current matching-job counts. */
public class JobAlertService {
    private static final int MAX_ALERTS_PER_CANDIDATE = 10;
    private static final Set<String> FREQUENCIES = Set.of("DAILY", "WEEKLY");

    private final JobAlertDAO jobAlertDAO;
    private final JobService jobService;
    private final DepartmentService departmentService;

    public JobAlertService() {
        this(new JobAlertDAO(), new JobService(), new DepartmentService());
    }

    JobAlertService(JobAlertDAO jobAlertDAO, JobService jobService, DepartmentService departmentService) {
        this.jobAlertDAO = jobAlertDAO;
        this.jobService = jobService;
        this.departmentService = departmentService;
    }

    public List<JobAlert> getAlerts(int candidateId) throws BusinessException {
        requireCandidate(candidateId);
        try {
            List<JobAlert> alerts = jobAlertDAO.findByCandidateId(candidateId);
            for (JobAlert alert : alerts) {
                long matching = jobService.searchPublishedJobs(alert.getKeyword(), alert.getDepartmentId(),
                        alert.getLocation(), alert.getEmploymentType(), 1, 1, "newest").getTotalItems();
                alert.setMatchingJobs(matching);
            }
            return alerts;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông báo việc làm.", exception);
        }
    }

    public int create(int candidateId, String name, String keyword, Integer departmentId, String location,
                      String employmentType, String frequency) throws BusinessException {
        requireCandidate(candidateId);
        JobAlert alert = new JobAlert();
        alert.setCandidateId(candidateId);
        alert.setName(requiredText(name, "Tên thông báo", 80));
        alert.setKeyword(optionalText(keyword, "Từ khóa", 150));
        alert.setLocation(optionalText(location, "Địa điểm", 100));
        alert.setDepartmentId(departmentId);
        alert.setEmploymentType(optionalEmploymentType(employmentType));
        alert.setFrequency(normalizeFrequency(frequency));

        if (alert.getKeyword() == null && alert.getDepartmentId() == null
                && alert.getLocation() == null && alert.getEmploymentType() == null) {
            throw new BusinessException("Hãy chọn ít nhất một tiêu chí tìm việc.");
        }
        if (departmentId != null) {
            departmentService.getById(departmentId);
        }

        try {
            if (jobAlertDAO.countByCandidateId(candidateId, false) >= MAX_ALERTS_PER_CANDIDATE) {
                throw new BusinessException("Mỗi ứng viên được tạo tối đa 10 thông báo việc làm.");
            }
            return jobAlertDAO.insert(alert);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tạo thông báo việc làm.", exception);
        }
    }

    public void setActive(int candidateId, int alertId, boolean active) throws BusinessException {
        requireIds(candidateId, alertId);
        try {
            if (!jobAlertDAO.updateActive(candidateId, alertId, active)) {
                throw new BusinessException("Không tìm thấy thông báo việc làm.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật thông báo việc làm.", exception);
        }
    }

    public void delete(int candidateId, int alertId) throws BusinessException {
        requireIds(candidateId, alertId);
        try {
            if (!jobAlertDAO.delete(candidateId, alertId)) {
                throw new BusinessException("Không tìm thấy thông báo việc làm.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xóa thông báo việc làm.", exception);
        }
    }

    public long countActive(int candidateId) throws BusinessException {
        requireCandidate(candidateId);
        try {
            return jobAlertDAO.countByCandidateId(candidateId, true);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đếm thông báo việc làm.", exception);
        }
    }

    private String requiredText(String value, String label, int maxLength) throws BusinessException {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new BusinessException(label + " là bắt buộc và không quá " + maxLength + " ký tự.");
        }
        return normalized;
    }

    private String optionalText(String value, String label, int maxLength) throws BusinessException {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(label + " không được vượt quá " + maxLength + " ký tự.");
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private String optionalEmploymentType(String value) throws BusinessException {
        if (value == null || value.isBlank()) return null;
        try {
            return EmploymentType.fromValue(value).name();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Loại hình làm việc không hợp lệ.");
        }
    }

    private String normalizeFrequency(String value) throws BusinessException {
        String normalized = value == null ? "DAILY" : value.trim().toUpperCase(Locale.ROOT);
        if (!FREQUENCIES.contains(normalized)) {
            throw new BusinessException("Tần suất thông báo không hợp lệ.");
        }
        return normalized;
    }

    private void requireIds(int candidateId, int alertId) throws BusinessException {
        requireCandidate(candidateId);
        if (alertId <= 0) throw new BusinessException("Thông báo việc làm không hợp lệ.");
    }

    private void requireCandidate(int candidateId) throws BusinessException {
        if (candidateId <= 0) throw new BusinessException("Tài khoản ứng viên không hợp lệ.");
    }
}
