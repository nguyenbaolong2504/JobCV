package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Job;
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

    public JobService() {
        this(new JobDAO(), new UserDAO());
    }

    JobService(JobDAO jobDAO, UserDAO userDAO) {
        this.jobDAO = jobDAO;
        this.userDAO = userDAO;
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
        Job job = getJobById(id);
        if (!JobStatus.PUBLISHED.name().equals(job.getStatus())) {
            throw new BusinessException("Tin tuyển dụng này không còn hiển thị.");
        }
        return job;
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

    /** Returns a job only when the current staff member owns it or is an administrator. */
    public Job getJobForManagement(int id, int actorId) throws BusinessException {
        User actor = requireHrActor(actorId);
        Job job = getJobById(id);
        requireOwnership(job, actor);
        return job;
    }

    public long countPublishedJobs() throws BusinessException {
        try {
            return jobDAO.countActiveJobs();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể thống kê tin tuyển dụng đang mở.", exception);
        }
    }

    public PageResult<Job> searchPublishedJobs(String keyword, Integer departmentId, String location,
                                                String employmentType, int page, int pageSize, String sort)
            throws BusinessException {
        try {
            List<Job> jobs = jobDAO.search(keyword, departmentId, location, employmentType,
                    JobStatus.PUBLISHED.name(), publicSort(sort), page, pageSize);
            long total = jobDAO.count(keyword, departmentId, location, employmentType, JobStatus.PUBLISHED.name());
            return new PageResult<>(jobs, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tìm kiếm tin tuyển dụng.", exception);
        }
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

    public PageResult<Job> searchForHr(String keyword, Integer departmentId, String location, EmploymentType employmentType,
                                       JobStatus status, String sort, int page, int pageSize, int actorId)
            throws BusinessException {
        User actor = requireHrActor(actorId);
        Integer ownerId = "ADMIN".equals(actor.getRoleName()) ? null : actorId;
        try {
            List<Job> jobs = jobDAO.search(keyword, departmentId, location,
                    employmentType == null ? null : employmentType.name(),
                    status == null ? null : status.name(), ownerId, sort, page, pageSize);
            long total = jobDAO.count(keyword, departmentId, location,
                    employmentType == null ? null : employmentType.name(),
                    status == null ? null : status.name(), ownerId);
            return new PageResult<>(jobs, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách tin tuyển dụng.", exception);
        }
    }

    public void createJob(Job job, String skillsText, int actorId) throws BusinessException {
        requireHrActor(actorId);
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
        User actor = requireHrActor(actorId);
        if (submitted == null || submitted.getId() <= 0) {
            throw new BusinessException("Tin tuyển dụng không hợp lệ.");
        }
        Job existing = getJobById(submitted.getId());
        requireOwnership(existing, actor);
        submitted.setCreatedBy(existing.getCreatedBy());
        submitted.setStatus(existing.getStatus());
        validateJob(submitted);
        List<JobSkill> skills = parseSkills(skillsText);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
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
        User actor = requireHrActor(actorId);
        Job job = getJobById(jobId);
        requireOwnership(job, actor);
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

    private User requireHrActor(int actorId) throws BusinessException {
        try {
            User actor = userDAO.findById(actorId);
            if (actor == null || !("HR".equals(actor.getRoleName()) || "ADMIN".equals(actor.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép quản lý tin tuyển dụng.");
            }
            return actor;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }

    private void requireOwnership(Job job, User actor) throws BusinessException {
        if (!"ADMIN".equals(actor.getRoleName()) && job.getCreatedBy() != actor.getId()) {
            throw new BusinessException("Bạn chỉ được quản lý tin tuyển dụng do mình tạo.");
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
            default -> sort;
        };
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
