package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.OnboardingDAO;
import vn.edu.eaut.recruitflow.dao.OnboardingTaskDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.OnboardingStatus;
import vn.edu.eaut.recruitflow.enums.OnboardingTaskStatus;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.Onboarding;
import vn.edu.eaut.recruitflow.model.OnboardingTask;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Onboarding creation, checklist management and progress calculation. */
public class OnboardingService {
    private static final List<String> DEFAULT_REQUIRED_TASKS = List.of(
            "Bổ sung CCCD", "Bổ sung thông tin ngân hàng", "Ký hợp đồng", "Nhận laptop",
            "Nhận tài khoản công ty", "Đọc quy định", "Orientation", "Gặp quản lý");

    private final OnboardingDAO onboardingDAO;
    private final OnboardingTaskDAO taskDAO;
    private final UserDAO userDAO;

    public OnboardingService() {
        this(new OnboardingDAO(), new OnboardingTaskDAO(), new UserDAO());
    }

    OnboardingService(OnboardingDAO onboardingDAO, OnboardingTaskDAO taskDAO, UserDAO userDAO) {
        this.onboardingDAO = onboardingDAO;
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
    }

    /** Called only from the accepted-offer transaction. */
    Onboarding createForHired(Connection connection, Application application) throws SQLException, BusinessException {
        Onboarding existing = onboardingDAO.findByApplicationId(connection, application.getId());
        if (existing != null) {
            return existing;
        }
        Onboarding onboarding = new Onboarding();
        onboarding.setApplicationId(application.getId());
        onboarding.setStatus(OnboardingStatus.NOT_STARTED.name());
        onboarding.setProgress(BigDecimal.ZERO);
        onboardingDAO.insert(connection, onboarding);
        for (String taskName : DEFAULT_REQUIRED_TASKS) {
            OnboardingTask task = new OnboardingTask();
            task.setOnboardingId(onboarding.getId());
            task.setTaskName(taskName);
            task.setRequired(true);
            task.setStatus(OnboardingTaskStatus.TODO.name());
            taskDAO.insert(connection, task);
        }
        return onboarding;
    }

    public Onboarding getForCandidate(int candidateId) throws BusinessException {
        try {
            List<Onboarding> onboardings = onboardingDAO.findByCandidateId(candidateId);
            return onboardings.isEmpty() ? null : onboardings.get(0);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải onboarding.", exception);
        }
    }

    public Onboarding getForCandidate(int onboardingId, int candidateId) throws BusinessException {
        try {
            Onboarding onboarding = onboardingDAO.findById(onboardingId);
            if (onboarding == null || !onboardingDAO.isOwnedByCandidate(onboardingId, candidateId)) {
                throw new BusinessException("Bạn không có quyền xem onboarding này.");
            }
            return onboarding;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải onboarding.", exception);
        }
    }

    public List<OnboardingTask> getTasks(int onboardingId) throws BusinessException {
        try {
            return taskDAO.findByOnboardingId(onboardingId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách công việc onboarding.", exception);
        }
    }

    public List<OnboardingTask> getTasksForCandidate(int onboardingId, int candidateId) throws BusinessException {
        getForCandidate(onboardingId, candidateId);
        return getTasks(onboardingId);
    }

    public List<Onboarding> findForHr() throws BusinessException {
        try {
            return onboardingDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách onboarding.", exception);
        }
    }

    public void completeTask(int candidateId, int taskId) throws BusinessException {
        updateTaskStatus(candidateId, taskId, OnboardingTaskStatus.DONE.name());
    }

    public void updateTaskStatus(int candidateId, int taskId, String targetStatus) throws BusinessException {
        OnboardingTaskStatus status = parseTaskStatus(targetStatus);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                OnboardingTask task = taskDAO.findById(connection, taskId);
                if (task == null || !onboardingDAO.isOwnedByCandidate(task.getOnboardingId(), candidateId)) {
                    throw new BusinessException("Bạn không có quyền cập nhật công việc onboarding này.");
                }
                if (!taskDAO.updateStatus(connection, taskId, status.name())) {
                    throw new BusinessException("Không thể cập nhật công việc onboarding.");
                }
                List<OnboardingTask> tasks = taskDAO.findByOnboardingId(connection, task.getOnboardingId());
                BigDecimal progress = calculateProgress(tasks, taskId, status);
                String onboardingStatus = progress.compareTo(BigDecimal.valueOf(100)) == 0
                        ? OnboardingStatus.COMPLETED.name()
                        : progress.compareTo(BigDecimal.ZERO) > 0 ? OnboardingStatus.IN_PROGRESS.name() : OnboardingStatus.NOT_STARTED.name();
                onboardingDAO.updateProgressAndStatus(connection, task.getOnboardingId(), progress, onboardingStatus);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật onboarding.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để cập nhật onboarding.", exception);
        }
    }

    public void addTask(int onboardingId, String taskName, boolean required, int actorId) throws BusinessException {
        validateHrActor(actorId);
        String name = taskName == null ? "" : taskName.trim();
        if (name.isBlank() || name.length() > 255) {
            throw new BusinessException("Tên công việc onboarding phải có từ 1 đến 255 ký tự.");
        }
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Onboarding onboarding = onboardingDAO.findById(connection, onboardingId);
                if (onboarding == null) {
                    throw new BusinessException("Không tìm thấy onboarding.");
                }
                OnboardingTask task = new OnboardingTask();
                task.setOnboardingId(onboardingId);
                task.setTaskName(name);
                task.setRequired(required);
                task.setStatus(OnboardingTaskStatus.TODO.name());
                taskDAO.insert(connection, task);

                List<OnboardingTask> tasks = taskDAO.findByOnboardingId(connection, onboardingId);
                BigDecimal progress = calculateProgress(tasks, -1, OnboardingTaskStatus.TODO);
                String status = progress.compareTo(BigDecimal.valueOf(100)) == 0
                        ? OnboardingStatus.COMPLETED.name()
                        : progress.compareTo(BigDecimal.ZERO) > 0 ? OnboardingStatus.IN_PROGRESS.name() : OnboardingStatus.NOT_STARTED.name();
                onboardingDAO.updateProgressAndStatus(connection, onboardingId, progress, status);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể thêm công việc onboarding.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể thêm công việc onboarding.", exception);
        }
    }

    private BigDecimal calculateProgress(List<OnboardingTask> tasks, int changedTaskId, OnboardingTaskStatus changedStatus) {
        long totalRequired = tasks.stream().filter(OnboardingTask::isRequired).count();
        if (totalRequired == 0) {
            return BigDecimal.valueOf(100);
        }
        long doneRequired = tasks.stream()
                .filter(OnboardingTask::isRequired)
                .filter(task -> task.getId() == changedTaskId ? changedStatus == OnboardingTaskStatus.DONE
                        : OnboardingTaskStatus.DONE.name().equals(task.getStatus()))
                .count();
        return BigDecimal.valueOf(doneRequired * 100.0d / totalRequired).setScale(2, RoundingMode.HALF_UP);
    }

    private OnboardingTaskStatus parseTaskStatus(String value) throws BusinessException {
        try {
            return OnboardingTaskStatus.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Trạng thái công việc onboarding không hợp lệ.");
        }
    }

    private void validateHrActor(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !("HR".equals(user.getRoleName()) || "ADMIN".equals(user.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép quản lý onboarding.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }
}
