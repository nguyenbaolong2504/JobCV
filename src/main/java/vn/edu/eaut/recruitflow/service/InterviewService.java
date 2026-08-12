package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.InterviewDAO;
import vn.edu.eaut.recruitflow.dao.InterviewFeedbackDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.InterviewStatus;
import vn.edu.eaut.recruitflow.enums.InterviewType;
import vn.edu.eaut.recruitflow.enums.Recommendation;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.model.InterviewFeedback;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Scheduling and interviewer feedback, with application updates in the same transaction. */
public class InterviewService {
    private final InterviewDAO interviewDAO;
    private final InterviewFeedbackDAO feedbackDAO;
    private final ApplicationDAO applicationDAO;
    private final UserDAO userDAO;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;

    public InterviewService() {
        this(new InterviewDAO(), new InterviewFeedbackDAO(), new ApplicationDAO(), new UserDAO(),
                new ApplicationService(), new NotificationService());
    }

    InterviewService(InterviewDAO interviewDAO, InterviewFeedbackDAO feedbackDAO, ApplicationDAO applicationDAO,
                     UserDAO userDAO, ApplicationService applicationService, NotificationService notificationService) {
        this.interviewDAO = interviewDAO;
        this.feedbackDAO = feedbackDAO;
        this.applicationDAO = applicationDAO;
        this.userDAO = userDAO;
        this.applicationService = applicationService;
        this.notificationService = notificationService;
    }

    public void create(Interview interview, int actorId) throws BusinessException {
        validateHrActor(actorId);
        validateInterview(interview);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application application = applicationDAO.findById(connection, interview.getApplicationId());
                if (application == null) {
                    throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
                }
                ApplicationStatus applicationStatus = ApplicationStatus.fromValue(application.getStatus());
                if (applicationStatus != ApplicationStatus.SHORTLISTED && applicationStatus != ApplicationStatus.INTERVIEW_SCHEDULED) {
                    throw new BusinessException("Chỉ có thể đặt lịch cho ứng viên đã được shortlist.");
                }
                validateInterviewer(connection, interview.getInterviewerId());
                if (interviewDAO.checkScheduleConflict(connection, interview.getInterviewerId(), interview.getInterviewDate(),
                        interview.getStartTime(), interview.getEndTime(), null)) {
                    throw new BusinessException("Interviewer đã có lịch phỏng vấn trùng thời gian này.");
                }
                interview.setStatus(InterviewStatus.SCHEDULED.name());
                interviewDAO.insert(connection, interview);
                if (applicationStatus == ApplicationStatus.SHORTLISTED) {
                    applicationService.transition(connection, application, ApplicationStatus.INTERVIEW_SCHEDULED, actorId,
                            "Đã đặt lịch phỏng vấn.");
                }
                notificationService.create(connection, application.getCandidateId(), "Lịch phỏng vấn mới",
                        "Bạn có lịch phỏng vấn cho vị trí " + application.getJobTitle() + " vào " + interview.getInterviewDate() + ".");
                notificationService.create(connection, interview.getInterviewerId(), "Bạn được phân công phỏng vấn",
                        "Bạn có lịch phỏng vấn ứng viên " + application.getCandidateName() + " vào " + interview.getInterviewDate() + ".");
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể tạo lịch phỏng vấn.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để đặt lịch phỏng vấn.", exception);
        }
    }

    public void update(Interview submitted, int actorId) throws BusinessException {
        validateHrActor(actorId);
        validateInterview(submitted);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Interview existing = interviewDAO.findById(connection, submitted.getId());
                if (existing == null) {
                    throw new BusinessException("Không tìm thấy lịch phỏng vấn.");
                }
                if (InterviewStatus.COMPLETED.name().equals(existing.getStatus()) || InterviewStatus.CANCELLED.name().equals(existing.getStatus())) {
                    throw new BusinessException("Không thể sửa lịch phỏng vấn đã hoàn tất hoặc bị hủy.");
                }
                submitted.setApplicationId(existing.getApplicationId());
                validateInterviewer(connection, submitted.getInterviewerId());
                if (interviewDAO.checkScheduleConflict(connection, submitted.getInterviewerId(), submitted.getInterviewDate(),
                        submitted.getStartTime(), submitted.getEndTime(), submitted.getId())) {
                    throw new BusinessException("Interviewer đã có lịch phỏng vấn trùng thời gian này.");
                }
                submitted.setStatus(InterviewStatus.RESCHEDULED.name());
                if (!interviewDAO.update(connection, submitted)) {
                    throw new BusinessException("Không thể cập nhật lịch phỏng vấn.");
                }
                Application application = applicationDAO.findById(connection, existing.getApplicationId());
                notificationService.create(connection, application.getCandidateId(), "Lịch phỏng vấn được thay đổi",
                        "Lịch phỏng vấn cho vị trí " + application.getJobTitle() + " đã được cập nhật.");
                notificationService.create(connection, submitted.getInterviewerId(), "Lịch phỏng vấn được thay đổi",
                        "Lịch phỏng vấn ứng viên " + application.getCandidateName() + " đã được cập nhật.");
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật lịch phỏng vấn.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để cập nhật lịch phỏng vấn.", exception);
        }
    }

    public void cancel(int interviewId, int actorId) throws BusinessException {
        validateHrActor(actorId);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Interview interview = interviewDAO.findById(connection, interviewId);
                if (interview == null) {
                    throw new BusinessException("Không tìm thấy lịch phỏng vấn.");
                }
                if (InterviewStatus.COMPLETED.name().equals(interview.getStatus())) {
                    throw new BusinessException("Không thể hủy lịch phỏng vấn đã hoàn tất.");
                }
                interviewDAO.cancel(connection, interviewId);
                Application application = applicationDAO.findById(connection, interview.getApplicationId());
                notificationService.create(connection, application.getCandidateId(), "Lịch phỏng vấn đã bị hủy",
                        "Lịch phỏng vấn cho vị trí " + application.getJobTitle() + " đã bị hủy. HR sẽ liên hệ lại nếu có lịch mới.");
                notificationService.create(connection, interview.getInterviewerId(), "Lịch phỏng vấn đã bị hủy",
                        "Lịch phỏng vấn ứng viên " + application.getCandidateName() + " đã bị hủy.");
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể hủy lịch phỏng vấn.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để hủy lịch phỏng vấn.", exception);
        }
    }

    public void submitFeedback(int interviewId, int interviewerId, InterviewFeedback feedback) throws BusinessException {
        validateFeedback(feedback);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Interview interview = interviewDAO.findById(connection, interviewId);
                if (interview == null || interview.getInterviewerId() != interviewerId) {
                    throw new BusinessException("Bạn chỉ có thể gửi đánh giá cho lịch phỏng vấn được phân công.");
                }
                if (!(InterviewStatus.SCHEDULED.name().equals(interview.getStatus()) || InterviewStatus.RESCHEDULED.name().equals(interview.getStatus()))) {
                    throw new BusinessException("Lịch phỏng vấn này không thể nhận đánh giá.");
                }
                if (feedbackDAO.findByInterviewId(connection, interviewId) != null) {
                    throw new BusinessException("Đánh giá cho lịch phỏng vấn này đã được gửi.");
                }
                feedback.setInterviewId(interviewId);
                feedback.setOverallScore(average(feedback));
                feedbackDAO.insert(connection, feedback);
                interviewDAO.updateStatus(connection, interviewId, InterviewStatus.COMPLETED.name());
                Application application = applicationDAO.findById(connection, interview.getApplicationId());
                applicationService.transition(connection, application, ApplicationStatus.INTERVIEWED, interviewerId,
                        "Interviewer đã gửi đánh giá: " + feedback.getRecommendation());
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể gửi đánh giá phỏng vấn.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để gửi đánh giá.", exception);
        }
    }

    public List<Interview> searchForHr(String keyword, Date date, String status) throws BusinessException {
        try {
            return interviewDAO.search(keyword, date, status);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách lịch phỏng vấn.", exception);
        }
    }

    public Interview getForHr(int interviewId) throws BusinessException {
        try {
            Interview interview = interviewDAO.findById(interviewId);
            if (interview == null) throw new BusinessException("Không tìm thấy lịch phỏng vấn.");
            return interview;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch phỏng vấn.", exception);
        }
    }

    public Interview getForInterviewer(int interviewId, int interviewerId) throws BusinessException {
        Interview interview = getForHr(interviewId);
        if (interview.getInterviewerId() != interviewerId) {
            throw new BusinessException("Bạn không có quyền xem lịch phỏng vấn này.");
        }
        return interview;
    }

    public InterviewFeedback getFeedback(int interviewId) throws BusinessException {
        try {
            return feedbackDAO.findByInterviewId(interviewId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải đánh giá phỏng vấn.", exception);
        }
    }

    public List<User> getInterviewers() throws BusinessException {
        try {
            return userDAO.list(null, "ACTIVE", "INTERVIEWER", 1, 100);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách interviewer.", exception);
        }
    }

    public List<Interview> findUpcomingForHr(int limit) throws BusinessException {
        try {
            List<Interview> interviews = interviewDAO.findUpcoming();
            return interviews.subList(0, Math.min(Math.max(0, limit), interviews.size()));
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch phỏng vấn sắp tới.", exception);
        }
    }

    public List<Interview> findUpcomingForInterviewer(int interviewerId) throws BusinessException {
        try {
            return interviewDAO.findUpcoming(interviewerId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch phỏng vấn sắp tới.", exception);
        }
    }

    public List<Interview> findForInterviewer(int interviewerId) throws BusinessException {
        try {
            return interviewDAO.findByInterviewer(interviewerId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch phỏng vấn.", exception);
        }
    }

    public List<Interview> findForCandidate(int candidateId) throws BusinessException {
        try {
            // Fetch through the candidate's applications so candidate ownership remains explicit.
            List<Interview> interviews = new ArrayList<>();
            for (Application application : applicationDAO.findByCandidateId(candidateId)) {
                interviews.addAll(interviewDAO.findByApplication(application.getId()));
            }
            interviews.sort(Comparator.comparing(Interview::getInterviewDate).reversed()
                    .thenComparing(Interview::getStartTime, Comparator.reverseOrder()));
            return interviews;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải lịch phỏng vấn.", exception);
        }
    }

    public List<Interview> findUpcomingForCandidate(int candidateId) throws BusinessException {
        return findForCandidate(candidateId).stream()
                .filter(interview -> interview.getInterviewDate() != null && !interview.getInterviewDate().toLocalDate().isBefore(LocalDate.now()))
                .filter(interview -> InterviewStatus.SCHEDULED.name().equals(interview.getStatus()) || InterviewStatus.RESCHEDULED.name().equals(interview.getStatus()))
                .sorted(Comparator.comparing(Interview::getInterviewDate).thenComparing(Interview::getStartTime))
                .toList();
    }

    private void validateInterview(Interview interview) throws BusinessException {
        if (interview == null || interview.getApplicationId() <= 0 || interview.getInterviewerId() <= 0
                || interview.getInterviewDate() == null || interview.getStartTime() == null || interview.getEndTime() == null) {
            throw new BusinessException("Thông tin lịch phỏng vấn chưa đầy đủ.");
        }
        try {
            InterviewType.fromValue(interview.getInterviewType());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Hình thức phỏng vấn không hợp lệ.");
        }
        if (!interview.getStartTime().before(interview.getEndTime())) {
            throw new BusinessException("Giờ bắt đầu phải trước giờ kết thúc.");
        }
        if (interview.getInterviewDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Không thể đặt lịch phỏng vấn trong quá khứ.");
        }
        if (InterviewType.ONLINE.name().equals(interview.getInterviewType()) && (interview.getMeetingUrl() == null || interview.getMeetingUrl().isBlank())) {
            throw new BusinessException("Phỏng vấn online cần có đường dẫn cuộc họp.");
        }
    }

    private void validateFeedback(InterviewFeedback feedback) throws BusinessException {
        if (feedback == null) {
            throw new BusinessException("Thông tin đánh giá không hợp lệ.");
        }
        validateScore(feedback.getTechnicalScore(), "Điểm kỹ thuật");
        validateScore(feedback.getCommunicationScore(), "Điểm giao tiếp");
        validateScore(feedback.getExperienceScore(), "Điểm kinh nghiệm");
        validateScore(feedback.getAttitudeScore(), "Điểm thái độ");
        try {
            Recommendation.fromValue(feedback.getRecommendation());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Khuyến nghị tuyển dụng không hợp lệ.");
        }
    }

    private void validateScore(BigDecimal score, String label) throws BusinessException {
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new BusinessException(label + " phải nằm trong khoảng 0 đến 10.");
        }
    }

    private BigDecimal average(InterviewFeedback feedback) {
        return feedback.getTechnicalScore().add(feedback.getCommunicationScore()).add(feedback.getExperienceScore())
                .add(feedback.getAttitudeScore()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
    }

    private void validateHrActor(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !("HR".equals(user.getRoleName()) || "ADMIN".equals(user.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép quản lý lịch phỏng vấn.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }

    private void validateInterviewer(Connection connection, int interviewerId) throws SQLException, BusinessException {
        User interviewer = userDAO.findById(connection, interviewerId);
        if (interviewer == null || !"INTERVIEWER".equals(interviewer.getRoleName()) || !"ACTIVE".equals(interviewer.getStatus())) {
            throw new BusinessException("Người được chọn không phải interviewer đang hoạt động.");
        }
    }
}
