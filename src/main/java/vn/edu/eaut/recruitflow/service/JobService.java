package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.model.JobSkill;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Job CRUD and publication rules. Public methods never expose unpublished jobs. */
public class JobService {
    private final JobDAO jobDAO;
    private final UserDAO userDAO;
    private final JobCategoryService jobCategoryService;
    private final ApplicationDAO applicationDAO;

    public JobService() {
        this(new JobDAO(), new UserDAO(), new JobCategoryService(), new ApplicationDAO());
    }

    JobService(JobDAO jobDAO, UserDAO userDAO) {
        this(jobDAO, userDAO, new JobCategoryService(), new ApplicationDAO());
    }

    JobService(JobDAO jobDAO, UserDAO userDAO, JobCategoryService jobCategoryService) {
        this(jobDAO, userDAO, jobCategoryService, new ApplicationDAO());
    }

    JobService(JobDAO jobDAO, UserDAO userDAO, JobCategoryService jobCategoryService,
               ApplicationDAO applicationDAO) {
        this.jobDAO = jobDAO;
        this.userDAO = userDAO;
        this.jobCategoryService = jobCategoryService;
        this.applicationDAO = applicationDAO;
    }

    public List<Job> getAllJobs() throws BusinessException {
        try {
            return jobDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách tin tuyển dụng.", exception);
        }
    }

    public Job getJobById(int id) throws BusinessException {
        try {
            Job job = jobDAO.findById(id);
            if (job == null) {
                throw new BusinessException("Không tìm thấy tin tuyển dụng.");
            }
            return job;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải tin tuyển dụng.", exception);
        }
    }

    public Job getPublishedJobById(int id) throws BusinessException {
        try {
            Job job = jobDAO.findOpenPublishedById(id);
            if (job == null) {
                throw new BusinessException("Tin tuyển dụng này không còn hiển thị.");
            }
            return job;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải tin tuyển dụng.", exception);
        }
    }

    public List<JobSkill> getSkills(int jobId) throws BusinessException {
        return getJobById(jobId).getSkills();
    }

    public List<Job> getFeaturedPublishedJobs(int limit) throws BusinessException {
        try {
            return jobDAO.findPublishedJobs(1, Math.max(1, Math.min(limit, 20)));
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải tin tuyển dụng nổi bật.", exception);
        }
    }

    public PageResult<Job> searchPublishedJobs(JobSearchCriteria criteria, int page, int pageSize, String sort)
            throws BusinessException {
        try {
            List<Job> jobs = jobDAO.searchPublished(criteria, publicSort(sort), page, pageSize);
            long total = jobDAO.countPublished(criteria);
            return new PageResult<>(jobs, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tìm kiếm tin tuyển dụng.", exception);
        }
    }

    /** Compatibility entry point for existing integrations using the original four filters. */
    public PageResult<Job> searchPublishedJobs(String keyword, Integer departmentId, String location,
                                                String employmentType, int page, int pageSize, String sort)
            throws BusinessException {
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setDepartmentId(departmentId);
        criteria.setLocation(location);
        criteria.setEmploymentType(employmentType);
        return searchPublishedJobs(criteria, page, pageSize, sort);
    }

    public PageResult<Job> searchForHr(String keyword, Integer departmentId, String location, String employmentType,
                                       String status, String sort, int page, int pageSize) throws BusinessException {
        try {
            List<Job> jobs = jobDAO.search(keyword, departmentId, location, employmentType, status, sort, page, pageSize);
            long total = jobDAO.count(keyword, departmentId, location, employmentType, status);
            return new PageResult<>(jobs, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách tin tuyển dụng.", exception);
        }
    }

    public PageResult<Job> searchForHr(String keyword, Integer departmentId, String location, EmploymentType employmentType,
                                       JobStatus status, String sort, int page, int pageSize) throws BusinessException {
        return searchForHr(keyword, departmentId, location,
                employmentType == null ? null : employmentType.name(),
                status == null ? null : status.name(), sort, page, pageSize);
    }

    public void createJob(Job job, String skillsText, int actorId) throws BusinessException {
        validateHrActor(actorId);
        validateJob(job);
        job.setCreatedBy(actorId);
        job.setStatus(JobStatus.DRAFT.name());
        List<JobSkill> skills = parseSkills(skillsText);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                jobDAO.insert(connection, job);
                jobDAO.replaceSkills(connection, job.getId(), skills);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể tạo tin tuyển dụng.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để tạo tin tuyển dụng.", exception);
        }
    }

    public void updateJob(Job submitted, String skillsText, int actorId) throws BusinessException {
        validateHrActor(actorId);
        if (submitted == null || submitted.getId() <= 0) {
            throw new BusinessException("Tin tuyển dụng không hợp lệ.");
        }
        Job existing = getJobById(submitted.getId());
        submitted.setCreatedBy(existing.getCreatedBy());
        submitted.setStatus(existing.getStatus());
        validateJob(submitted);
        List<JobSkill> skills = parseSkills(skillsText);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (!jobDAO.lockById(connection, submitted.getId())) {
                    throw new BusinessException("Không tìm thấy tin tuyển dụng.");
                }
                long hiredCount = applicationDAO.countByJobAndStatus(connection, submitted.getId(),
                        ApplicationStatus.HIRED.name());
                if (submitted.getNumberOfPositions() < hiredCount) {
                    throw new BusinessException("Số lượng tuyển không thể nhỏ hơn " + hiredCount
                            + " ứng viên đã nhận việc.");
                }
                if (!jobDAO.update(connection, submitted)) {
                    throw new BusinessException("Không thể cập nhật tin tuyển dụng.");
                }
                jobDAO.replaceSkills(connection, submitted.getId(), skills);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật tin tuyển dụng.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để cập nhật tin tuyển dụng.", exception);
        }
    }

    public void changeStatus(int jobId, String targetStatus, int actorId) throws BusinessException {
        validateHrActor(actorId);
        Job job = getJobById(jobId);
        JobStatus target;
        try {
            target = JobStatus.fromValue(targetStatus);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Trạng thái tin tuyển dụng không hợp lệ.");
        }
        JobStatus current = job.getJobStatus();
        if (!canChangeStatus(current, target)) {
            throw new BusinessException("Không thể chuyển từ " + current + " sang " + target + ".");
        }
        if (target == JobStatus.PUBLISHED && (job.getDeadline() == null
                || job.getDeadline().toLocalDate().isBefore(LocalDate.now()))) {
            throw new BusinessException("Không thể đăng tin đã quá hạn nộp hồ sơ. Vui lòng cập nhật hạn nộp trước.");
        }
        try {
            if (!jobDAO.updateStatus(jobId, target.name())) {
                throw new BusinessException("Không thể cập nhật trạng thái tin tuyển dụng.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật trạng thái tin tuyển dụng.", exception);
        }
    }

    public List<JobSkill> parseSkills(String skillsText) throws BusinessException {
        List<JobSkill> result = new ArrayList<>();
        if (skillsText == null || skillsText.isBlank()) {
            return result;
        }
        Set<String> seen = new HashSet<>();
        String[] entries = skillsText.split("[\\r\\n,;]+");
        for (String raw : entries) {
            String line = raw.trim();
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("[:|]");
            String name = parts[0].trim();
            if (name.isBlank() || name.length() > 100) {
                throw new BusinessException("Tên kỹ năng không hợp lệ.");
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new BusinessException("Không được nhập trùng kỹ năng: " + name + ".");
            }
            int weight = 1;
            boolean required = false;
            if (parts.length >= 2 && !parts[1].trim().isBlank()) {
                try {
                    weight = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException exception) {
                    throw new BusinessException("Trọng số của kỹ năng " + name + " phải là số nguyên.");
                }
            }
            if (weight < 1 || weight > 10) {
                throw new BusinessException("Trọng số kỹ năng phải từ 1 đến 10.");
            }
            if (parts.length >= 3) {
                String flag = parts[2].trim().toLowerCase(Locale.ROOT);
                required = "true".equals(flag) || "required".equals(flag) || "bắt buộc".equals(flag) || "bat buoc".equals(flag);
            }
            JobSkill skill = new JobSkill();
            skill.setSkillName(name);
            skill.setWeight(weight);
            skill.setRequired(required);
            result.add(skill);
        }
        return result;
    }

    private void validateHrActor(int actorId) throws BusinessException {
        try {
            User actor = userDAO.findById(actorId);
            if (actor == null || !("HR".equals(actor.getRoleName()) || "ADMIN".equals(actor.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép quản lý tin tuyển dụng.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }

    private void validateJob(Job job) throws BusinessException {
        if (job == null || blank(job.getJobCode()) || job.getJobCode().length() > 50) {
            throw new BusinessException("Mã tin tuyển dụng là bắt buộc và không quá 50 ký tự.");
        }
        if (blank(job.getTitle()) || job.getTitle().length() > 255) {
            throw new BusinessException("Tiêu đề công việc là bắt buộc và không quá 255 ký tự.");
        }
        if (job.getDepartmentId() <= 0) {
            throw new BusinessException("Vui lòng chọn phòng ban.");
        }
        jobCategoryService.validateSelectableCategory(job.getCategoryId());
        if (blank(job.getLocation()) || job.getLocation().length() > 255) {
            throw new BusinessException("Địa điểm làm việc là bắt buộc và không quá 255 ký tự.");
        }
        try {
            EmploymentType.fromValue(job.getEmploymentType());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Loại hình làm việc không hợp lệ.");
        }
        if (job.getNumberOfPositions() <= 0) {
            throw new BusinessException("Số lượng tuyển phải lớn hơn 0.");
        }
        if (job.getSalaryMin() == null || job.getSalaryMax() == null || job.getSalaryMin().compareTo(BigDecimal.ZERO) < 0
                || job.getSalaryMax().compareTo(job.getSalaryMin()) < 0) {
            throw new BusinessException("Khoảng lương không hợp lệ.");
        }
        if (job.getExperienceRequired() < 0) {
            throw new BusinessException("Số năm kinh nghiệm không được âm.");
        }
        if (blank(job.getDescription())) {
            throw new BusinessException("Mô tả công việc là bắt buộc.");
        }
        if (blank(job.getRequirements())) {
            throw new BusinessException("Yêu cầu công việc là bắt buộc.");
        }
        if (job.getDeadline() == null || job.getDeadline().toLocalDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Hạn nộp phải từ hôm nay trở đi.");
        }
    }

    private boolean canChangeStatus(JobStatus current, JobStatus target) {
        if (current == null || target == null || current == target) {
            return false;
        }
        return switch (current) {
            case DRAFT -> target == JobStatus.PUBLISHED || target == JobStatus.ARCHIVED;
            case PUBLISHED -> target == JobStatus.CLOSED || target == JobStatus.ARCHIVED;
            case CLOSED -> target == JobStatus.PUBLISHED || target == JobStatus.ARCHIVED;
            case ARCHIVED -> false;
        };
    }

    private String publicSort(String sort) {
        if (sort == null) {
            return "newest";
        }
        return switch (sort) {
            case "deadline" -> "deadline_asc";
            case "salary" -> "salary_desc";
            case "experience" -> "experience_asc";
            default -> sort;
        };
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
