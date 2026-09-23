package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.ApplicationStatusHistoryDAO;
import vn.edu.eaut.recruitflow.dao.InterviewDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.dao.CompanyDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.ApplicationStatusHistory;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.MatchResult;
import vn.edu.eaut.recruitflow.model.Offer;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Owns application transitions and guarantees a history row for every change. */
public class ApplicationService {
    private final ApplicationDAO applicationDAO;
    private final ApplicationStatusHistoryDAO historyDAO;
    private final JobDAO jobDAO;
    private final ResumeDAO resumeDAO;
    private final UserDAO userDAO;
    private final InterviewDAO interviewDAO;
    private final OfferDAO offerDAO;
    private final MatchingService matchingService;
    private final NotificationService notificationService;
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final JobCapacityPolicy capacityPolicy = new JobCapacityPolicy();

    public ApplicationService() {
        this(new ApplicationDAO(), new ApplicationStatusHistoryDAO(), new JobDAO(), new ResumeDAO(), new UserDAO(),
                new InterviewDAO(), new OfferDAO(), new MatchingService(), new NotificationService());
    }

    ApplicationService(ApplicationDAO applicationDAO, ApplicationStatusHistoryDAO historyDAO, JobDAO jobDAO,
                       ResumeDAO resumeDAO, UserDAO userDAO, InterviewDAO interviewDAO, OfferDAO offerDAO,
                       MatchingService matchingService, NotificationService notificationService) {
        this.applicationDAO = applicationDAO;
        this.historyDAO = historyDAO;
        this.jobDAO = jobDAO;
        this.resumeDAO = resumeDAO;
        this.userDAO = userDAO;
        this.interviewDAO = interviewDAO;
        this.offerDAO = offerDAO;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    /** Implements BR01–BR04 and the required apply transaction. */
    public Application apply(int candidateId, int jobId, Integer requestedResumeId) throws BusinessException {
        return apply(candidateId, jobId, requestedResumeId, null);
    }

    /** Applies with an explicitly selected CV and an optional recruiter-facing introduction. */
    public Application apply(int candidateId, int jobId, Integer requestedResumeId, String coverLetter) throws BusinessException {
        String normalizedCoverLetter = cleanCoverLetter(coverLetter);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (!jobDAO.lockById(connection, jobId)) {
                    throw new BusinessException("Không tìm thấy tin tuyển dụng.");
                }
                Job job = jobDAO.findById(connection, jobId);
                if (job == null) {
                    throw new BusinessException("Không tìm thấy tin tuyển dụng.");
                }
                if (!JobStatus.PUBLISHED.name().equals(job.getStatus())) {
                    throw new BusinessException(job.isAutoClosed() ? "Vị trí này đã tuyển đủ số lượng." : "Chỉ có thể ứng tuyển tin đang được đăng tuyển.");
                }
                if (job.getDeadline() == null || job.getDeadline().toLocalDate().isBefore(LocalDate.now())) {
                    throw new BusinessException("Tin tuyển dụng đã hết hạn nộp hồ sơ.");
                }
                if (applicationDAO.existsByCandidateAndJob(connection, candidateId, jobId)) {
                    throw new BusinessException("Bạn đã ứng tuyển công việc này rồi.");
                }
                if (applicationDAO.countActiveByJobId(connection, jobId) >= job.getNumberOfPositions()) {
                    throw new BusinessException("Vị trí này đã tuyển đủ số lượng.");
                }

                Resume resume = requestedResumeId == null || requestedResumeId <= 0
                        ? resumeDAO.findDefaultByCandidateId(candidateId) : resumeDAO.findById(connection, requestedResumeId);
                if (resume == null || resume.getCandidateId() != candidateId) {
                    throw new BusinessException("Bạn cần chọn một CV thuộc tài khoản của mình trước khi ứng tuyển.");
                }

                MatchResult match = matchingService.calculate(resume, job.getSkills());
                Application application = new Application();
                application.setJobId(jobId);
                application.setCandidateId(candidateId);
                application.setResumeId(resume.getId());
                application.setStatus(ApplicationStatus.SUBMITTED.name());
                application.setMatchScore(match.getMatchScore());
                application.setCoverLetter(normalizedCoverLetter);
                applicationDAO.create(connection, application);
                synchronizeJobCapacity(connection, job);
                insertHistory(connection, application.getId(), null, ApplicationStatus.SUBMITTED, candidateId,
                        "Ứng viên nộp hồ sơ.");
                notificationService.create(connection, candidateId, "Đã gửi đơn ứng tuyển",
                        "Đơn ứng tuyển cho vị trí " + job.getTitle() + " đã được ghi nhận.");
                for (int hrUserId : companyDAO.findActiveHrUserIds(job.getCompanyId())) {
                    notificationService.create(connection, hrUserId, "Có đơn ứng tuyển mới",
                            "Có ứng viên mới ứng tuyển vị trí " + job.getTitle() + " của " + job.getCompanyName() + ".");
                }
                connection.commit();
                return application;
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể gửi đơn ứng tuyển. Vui lòng thử lại.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để gửi đơn ứng tuyển.", exception);
        }
    }

    public Application apply(int candidateId, int jobId, int resumeId) throws BusinessException {
        return apply(candidateId, jobId, Integer.valueOf(resumeId));
    }

    public boolean hasApplied(int candidateId, int jobId) throws BusinessException {
        try {
            return applicationDAO.findByCandidateAndJob(candidateId, jobId) != null;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kiểm tra trạng thái ứng tuyển.", exception);
        }
    }

    public Application getForCandidate(int applicationId, int candidateId) throws BusinessException {
        Application application = loadApplication(applicationId);
        if (application.getCandidateId() != candidateId) {
            throw new BusinessException("Bạn không có quyền xem đơn ứng tuyển này.");
        }
        return application;
    }

    public Application getForHr(int applicationId, int actorId) throws BusinessException {
        User actor = requireHrActor(actorId);
        Application application = loadApplication(applicationId);
        requireOwnership(application, actor);
        return application;
    }

    public Application getForInterviewer(int applicationId, int interviewerId) throws BusinessException {
        Application application = loadApplication(applicationId);
        try {
            boolean assigned = interviewDAO.findByApplication(applicationId).stream()
                    .anyMatch(interview -> interview.getInterviewerId() == interviewerId);
            if (!assigned) {
                throw new BusinessException("Bạn không có quyền xem hồ sơ ứng tuyển này.");
            }
            return application;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền xem hồ sơ ứng tuyển.", exception);
        }
    }

    private Application loadApplication(int applicationId) throws BusinessException {
        try {
            Application application = applicationDAO.findById(applicationId);
            if (application == null) {
                throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
            }
            return application;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải đơn ứng tuyển.", exception);
        }
    }

    public PageResult<Application> searchForCandidate(int candidateId, String keyword, ApplicationStatus status, int page, int pageSize)
            throws BusinessException {
        try {
            List<Application> all = applicationDAO.findByCandidateId(candidateId);
            String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
            List<Application> filtered = new ArrayList<>();
            for (Application application : all) {
                boolean matchesStatus = status == null || status.name().equals(application.getStatus());
                boolean matchesKeyword = normalizedKeyword.isBlank()
                        || (application.getJobTitle() != null && application.getJobTitle().toLowerCase().contains(normalizedKeyword))
                        || (application.getJobCode() != null && application.getJobCode().toLowerCase().contains(normalizedKeyword));
                if (matchesStatus && matchesKeyword) {
                    filtered.add(application);
                }
            }
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(100, pageSize));
            int from = Math.min((safePage - 1) * safeSize, filtered.size());
            int to = Math.min(from + safeSize, filtered.size());
            return new PageResult<>(filtered.subList(from, to), safePage, safeSize, filtered.size());
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách đơn ứng tuyển.", exception);
        }
    }

    public PageResult<Application> searchForCandidate(int candidateId, String keyword, int page, int pageSize)
            throws BusinessException {
        return searchForCandidate(candidateId, keyword, (ApplicationStatus) null, page, pageSize);
    }

    public PageResult<Application> searchForHr(String keyword, Integer jobId, String status, BigDecimal minMatchScore,
                                                int page, int pageSize) throws BusinessException {
        try {
            List<Application> applications = applicationDAO.search(keyword, jobId, status, minMatchScore, page, pageSize);
            long total = applicationDAO.count(keyword, jobId, status, minMatchScore);
            return new PageResult<>(applications, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tìm kiếm đơn ứng tuyển.", exception);
        }
    }

    public PageResult<Application> searchForHr(String keyword, Integer jobId, String status, BigDecimal minMatchScore,
                                                int page, int pageSize, int actorId) throws BusinessException {
        User actor = requireHrActor(actorId);
        Integer ownerId = "ADMIN".equals(actor.getRoleName()) ? null : companyIdFor(actorId);
        try {
            List<Application> applications = applicationDAO.search(
                    keyword, jobId, status, minMatchScore, ownerId, page, pageSize);
            long total = applicationDAO.count(keyword, jobId, status, minMatchScore, ownerId);
            return new PageResult<>(applications, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tìm kiếm đơn ứng tuyển.", exception);
        }
    }

    public List<ApplicationStatusHistory> getHistory(int applicationId) throws BusinessException {
        try {
            return historyDAO.findByApplicationId(applicationId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch sử đơn ứng tuyển.", exception);
        }
    }

    public List<ApplicationStatusHistory> getHistoryForCandidate(int candidateId, int applicationId) throws BusinessException {
        getForCandidate(applicationId, candidateId);
        return getHistory(applicationId);
    }

    public List<ApplicationStatusHistory> getHistoryForHr(int applicationId, int actorId) throws BusinessException {
        getForHr(applicationId, actorId);
        return getHistory(applicationId);
    }

    public List<Interview> getInterviews(int applicationId) throws BusinessException {
        try {
            return interviewDAO.findByApplication(applicationId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch phỏng vấn.", exception);
        }
    }

    public List<Interview> getInterviewsForCandidate(int candidateId, int applicationId) throws BusinessException {
        getForCandidate(applicationId, candidateId);
        return getInterviews(applicationId);
    }

    public List<Interview> getInterviewsForHr(int applicationId, int actorId) throws BusinessException {
        getForHr(applicationId, actorId);
        return getInterviews(applicationId);
    }

    public Offer getOffer(int applicationId) throws BusinessException {
        try {
            return offerDAO.findByApplicationId(applicationId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải offer.", exception);
        }
    }

    public Offer getOfferForCandidate(int candidateId, int applicationId) throws BusinessException {
        getForCandidate(applicationId, candidateId);
        return getOffer(applicationId);
    }

    public Offer getOfferForHr(int applicationId, int actorId) throws BusinessException {
        getForHr(applicationId, actorId);
        return getOffer(applicationId);
    }

    public List<Application> findShortlisted() throws BusinessException {
        return findByStatus(ApplicationStatus.SHORTLISTED.name());
    }

    public List<Application> findShortlisted(int actorId) throws BusinessException {
        return searchForHr(null, null, ApplicationStatus.SHORTLISTED.name(), null, 1, 100, actorId).getItems();
    }

    public List<Application> findInterviewed() throws BusinessException {
        return findByStatus(ApplicationStatus.INTERVIEWED.name());
    }

    public List<Application> findInterviewed(int actorId) throws BusinessException {
        return searchForHr(null, null, ApplicationStatus.INTERVIEWED.name(), null, 1, 100, actorId).getItems();
    }

    public List<Application> findByStatus(String status) throws BusinessException {
        try {
            return applicationDAO.findByStatus(status);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách ứng viên.", exception);
        }
    }

    public void transitionStatus(int applicationId, String targetStatus, String remarks, int actorId) throws BusinessException {
        getForHr(applicationId, actorId);
        ApplicationStatus target = parseStatus(targetStatus);
        if (target == ApplicationStatus.HIRED) {
            throw new BusinessException("Chỉ thao tác chấp nhận thư mời mới được chuyển ứng viên sang trạng thái đã tuyển.");
        }
        transition(applicationId, target, actorId, remarks, true);
    }

    public void withdraw(int applicationId, int candidateId, String remarks) throws BusinessException {
        Application application = getForCandidate(applicationId, candidateId);
        if (!ApplicationStatus.SUBMITTED.name().equals(application.getStatus())) {
            throw new BusinessException("Chỉ có thể rút đơn khi đơn đang ở trạng thái SUBMITTED.");
        }
        transition(applicationId, ApplicationStatus.WITHDRAWN, candidateId, remarks, false);
    }

    void transition(Connection connection, Application application, ApplicationStatus target, int actorId, String remarks)
            throws SQLException, BusinessException {
        ApplicationStatus current = parseStatus(application.getStatus());
        if (!current.canTransitionTo(target)) {
            throw new BusinessException("Không thể chuyển trạng thái đơn từ " + current + " sang " + target + ".");
        }
        if (!applicationDAO.updateStatus(connection, application.getId(), target.name())) {
            throw new BusinessException("Không thể cập nhật trạng thái đơn ứng tuyển.");
        }
        Job job = jobDAO.findById(connection, application.getJobId());
        if (job != null) synchronizeJobCapacity(connection, job);
        insertHistory(connection, application.getId(), current.name(), target, actorId, remarks);
        notificationService.create(connection, application.getCandidateId(), "Cập nhật đơn ứng tuyển",
                "Đơn ứng tuyển vị trí " + application.getJobTitle() + " đã chuyển sang " + target.name() + ".");
    }

    private void transition(int applicationId, ApplicationStatus target, int actorId, String remarks, boolean notify)
            throws BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application application = applicationDAO.findByIdForUpdate(connection, applicationId);
                if (application == null) {
                    throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
                }
                transition(connection, application, target, actorId, cleanRemarks(remarks));
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật trạng thái đơn ứng tuyển.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để cập nhật đơn ứng tuyển.", exception);
        }
    }

    private void insertHistory(Connection connection, int applicationId, String oldStatus, ApplicationStatus newStatus,
                               int actorId, String remarks) throws SQLException {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(applicationId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus.name());
        history.setChangedBy(actorId);
        history.setRemarks(cleanRemarks(remarks));
        historyDAO.insert(connection, history);
    }

    private User requireHrActor(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !("HR".equals(user.getRoleName()) || "ADMIN".equals(user.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép xử lý đơn ứng tuyển.");
            }
            return user;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }

    private void requireOwnership(Application application, User actor) throws BusinessException {
        if ("ADMIN".equals(actor.getRoleName())) {
            return;
        }
        try {
            Job job = jobDAO.findById(application.getJobId());
            if (job == null || job.getCompanyId() != companyIdFor(actor.getId())) {
                throw new BusinessException("Bạn chỉ được xử lý ứng viên thuộc công ty của mình.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền xử lý đơn ứng tuyển.", exception);
        }
    }

    private ApplicationStatus parseStatus(String value) throws BusinessException {
        try {
            return ApplicationStatus.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Trạng thái đơn ứng tuyển không hợp lệ.");
        }
    }

    private String cleanRemarks(String remarks) {
        if (remarks == null || remarks.isBlank()) {
            return null;
        }
        String cleaned = remarks.trim();
        return cleaned.length() <= 4000 ? cleaned : cleaned.substring(0, 4000);
    }

    private String cleanCoverLetter(String coverLetter) throws BusinessException {
        if (coverLetter == null || coverLetter.isBlank()) {
            return null;
        }
        String cleaned = coverLetter.trim().replace("\r\n", "\n");
        if (cleaned.length() < 20) {
            throw new BusinessException("Lời giới thiệu cần có ít nhất 20 ký tự.");
        }
        if (cleaned.length() > 2000) {
            throw new BusinessException("Lời giới thiệu không được vượt quá 2.000 ký tự.");
        }
        return cleaned;
    }

    private int companyIdFor(int userId) throws BusinessException {
        try {
            Integer companyId = companyDAO.findCompanyIdByUserId(userId);
            if (companyId == null) throw new BusinessException("Tài khoản HR chưa được liên kết với công ty.");
            return companyId;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực công ty của HR.", exception);
        }
    }

    private void synchronizeJobCapacity(Connection connection, Job job) throws SQLException {
        long activeApplications = applicationDAO.countActiveByJobId(connection, job.getId());
        JobCapacityPolicy.Decision decision = capacityPolicy.evaluate(job, activeApplications, LocalDate.now());
        if (decision != null) {
            jobDAO.updateCapacityStatus(connection, job.getId(), decision.status(), decision.autoClosed());
        }
    }
}
