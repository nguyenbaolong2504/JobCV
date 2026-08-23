package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.ApplicationStatusHistoryDAO;
import vn.edu.eaut.recruitflow.dao.InterviewDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
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
                    throw new BusinessException("Chỉ có thể ứng tuyển tin đang được đăng tuyển.");
                }
                if (job.getDeadline() == null || job.getDeadline().toLocalDate().isBefore(LocalDate.now())) {
                    throw new BusinessException("Tin tuyển dụng đã hết hạn nộp hồ sơ.");
                }
                if (applicationDAO.existsByCandidateAndJob(connection, candidateId, jobId)) {
                    throw new BusinessException("Bạn đã ứng tuyển công việc này rồi.");
                }

                Resume resume = requestedResumeId == null || requestedResumeId <= 0
                        ? resumeDAO.findDefaultByCandidateId(candidateId) : resumeDAO.findById(connection, requestedResumeId);
                if (resume == null || resume.getCandidateId() != candidateId) {
                    throw new BusinessException("Bạn cần chọn một CV thuộc tài khoản của mình trước khi ứng tuyển.");
                }
                if (!new ResumeService().isFileAvailable(resume)) {
                    throw new BusinessException("Tệp CV đã chọn không còn trên máy chủ. Vui lòng tải CV mới trước khi ứng tuyển.");
                }

                MatchResult match = matchingService.calculate(resume, job.getSkills());
                Application application = new Application();
                application.setJobId(jobId);
                application.setCandidateId(candidateId);
                application.setResumeId(resume.getId());
                application.setStatus(ApplicationStatus.SUBMITTED.name());
                application.setMatchScore(match.getMatchScore());
                applicationDAO.create(connection, application);
                insertHistory(connection, application.getId(), null, ApplicationStatus.SUBMITTED, candidateId,
                        "Ứng viên nộp hồ sơ.");
                notificationService.create(connection, candidateId, "Đã gửi đơn ứng tuyển",
                        "Đơn ứng tuyển cho vị trí " + job.getTitle() + " đã được ghi nhận.");
                notificationService.create(connection, job.getCreatedBy(), "Có đơn ứng tuyển mới",
                        "Có ứng viên mới ứng tuyển vị trí " + job.getTitle() + ".");
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

    public Application getForCandidate(int applicationId, int candidateId) throws BusinessException {
        Application application = getForHr(applicationId);
        if (application.getCandidateId() != candidateId) {
            throw new BusinessException("Bạn không có quyền xem đơn ứng tuyển này.");
        }
        return application;
    }

    public Application getForHr(int applicationId) throws BusinessException {
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

    public Offer getOffer(int applicationId) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            return offerDAO.findByApplicationId(applicationId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải offer.", exception);
        }
    }

    public Offer getOfferForCandidate(int candidateId, int applicationId) throws BusinessException {
        getForCandidate(applicationId, candidateId);
        Offer offer = getOffer(applicationId);
        return offer != null && OfferStatus.DRAFT.name().equals(offer.getStatus()) ? null : offer;
    }

    public List<Application> findShortlisted() throws BusinessException {
        return findByStatus(ApplicationStatus.SHORTLISTED.name());
    }

    public List<Application> findInterviewed() throws BusinessException {
        return findByStatus(ApplicationStatus.INTERVIEWED.name());
    }

    public List<Application> findByStatus(String status) throws BusinessException {
        try {
            return applicationDAO.findByStatus(status);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách ứng viên.", exception);
        }
    }

    public void transitionStatus(int applicationId, String targetStatus, String remarks, int actorId) throws BusinessException {
        validateHrActor(actorId);
        ApplicationStatus target = parseStatus(targetStatus);
        validateHrManagedTarget(target);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (!applicationDAO.lockById(connection, applicationId)) {
                    throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
                }
                Application application = applicationDAO.findById(connection, applicationId);
                if (application == null) {
                    throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
                }
                ApplicationStatus current = parseStatus(application.getStatus());
                if (current == ApplicationStatus.INTERVIEW_SCHEDULED && target == ApplicationStatus.SHORTLISTED) {
                    throw new BusinessException("Hãy hủy lịch phỏng vấn đang hiệu lực để đưa hồ sơ về danh sách chờ lên lịch.");
                }
                if (target == ApplicationStatus.REJECTED && current == ApplicationStatus.INTERVIEW_SCHEDULED
                        && interviewDAO.hasActiveInterviewForApplication(connection, applicationId, null)) {
                    throw new BusinessException("Hãy hủy lịch phỏng vấn đang hiệu lực trước khi từ chối hồ sơ.");
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

    public void withdraw(int applicationId, int candidateId, String remarks) throws BusinessException {
        Application application = getForCandidate(applicationId, candidateId);
        if (!ApplicationStatus.SUBMITTED.name().equals(application.getStatus())) {
            throw new BusinessException("Chỉ có thể rút đơn khi đơn đang ở trạng thái SUBMITTED.");
        }
        transition(applicationId, ApplicationStatus.WITHDRAWN, candidateId, remarks, false);
    }

    void transition(Connection connection, Application application, ApplicationStatus target, int actorId, String remarks)
            throws SQLException, BusinessException {
        if (application == null || !applicationDAO.lockById(connection, application.getId())) {
            throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
        }
        Application lockedApplication = applicationDAO.findById(connection, application.getId());
        if (lockedApplication == null) {
            throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
        }
        ApplicationStatus current = parseStatus(lockedApplication.getStatus());
        if (!current.canTransitionTo(target)) {
            throw new BusinessException("Không thể chuyển trạng thái đơn từ " + current + " sang " + target + ".");
        }
        if (!applicationDAO.updateStatusIfCurrent(connection, lockedApplication.getId(), target.name(), current.name())) {
            throw new BusinessException("Trạng thái đơn đã thay đổi, vui lòng tải lại trang và thử lại.");
        }
        insertHistory(connection, lockedApplication.getId(), current.name(), target, actorId, remarks);
        notificationService.create(connection, lockedApplication.getCandidateId(), "Cập nhật đơn ứng tuyển",
                "Đơn ứng tuyển vị trí " + lockedApplication.getJobTitle() + " đã chuyển sang " + target.name() + ".");
    }

    private void transition(int applicationId, ApplicationStatus target, int actorId, String remarks, boolean notify)
            throws BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application application = applicationDAO.findById(connection, applicationId);
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

    private void validateHrActor(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !("HR".equals(user.getRoleName()) || "ADMIN".equals(user.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép xử lý đơn ứng tuyển.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }

    private ApplicationStatus parseStatus(String value) throws BusinessException {
        try {
            ApplicationStatus status = ApplicationStatus.fromValue(value);
            if (status == null) {
                throw new IllegalArgumentException("missing status");
            }
            return status;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Trạng thái đơn ứng tuyển không hợp lệ.");
        }
    }

    /**
     * The generic HR endpoint handles review decisions only. Milestone states are owned by
     * their specialised workflows so a forged form cannot create an interview, offer, or hire.
     */
    private void validateHrManagedTarget(ApplicationStatus target) throws BusinessException {
        if (target == ApplicationStatus.SCREENING || target == ApplicationStatus.SHORTLISTED
                || target == ApplicationStatus.REJECTED) {
            return;
        }
        throw new BusinessException(switch (target) {
            case INTERVIEW_SCHEDULED -> "Lịch phỏng vấn chỉ được tạo từ chức năng lên lịch phỏng vấn.";
            case INTERVIEWED -> "Chỉ feedback của interviewer mới có thể hoàn tất bước phỏng vấn.";
            case OFFERED -> "Chỉ thao tác gửi offer mới được chuyển ứng viên sang OFFERED.";
            case HIRED -> "Chỉ ứng viên chấp nhận offer mới được chuyển sang HIRED.";
            case WITHDRAWN -> "Chỉ ứng viên mới có thể rút đơn ứng tuyển.";
            case SUBMITTED -> "Không thể đưa đơn ứng tuyển trở lại trạng thái SUBMITTED.";
            default -> "Trạng thái đơn ứng tuyển không được cập nhật từ màn hình xử lý hồ sơ.";
        });
    }

    private String cleanRemarks(String remarks) {
        if (remarks == null || remarks.isBlank()) {
            return null;
        }
        String cleaned = remarks.trim();
        return cleaned.length() <= 4000 ? cleaned : cleaned.substring(0, 4000);
    }
}
