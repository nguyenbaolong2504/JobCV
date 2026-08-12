package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.OnboardingTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OnboardingTaskDAO extends DaoSupport {
    public OnboardingTask findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public OnboardingTask findById(Connection connection, int id) throws SQLException {
        String sql = "SELECT id, onboarding_id, task_name, description, is_required, status, completed_at, created_at, updated_at FROM onboarding_tasks WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<OnboardingTask> findByOnboardingId(int onboardingId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByOnboardingId(connection, onboardingId);
        }
    }

    public List<OnboardingTask> findByOnboardingId(Connection connection, int onboardingId) throws SQLException {
        String sql = "SELECT id, onboarding_id, task_name, description, is_required, status, completed_at, created_at, updated_at "
                + "FROM onboarding_tasks WHERE onboarding_id = ? ORDER BY is_required DESC, id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, onboardingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OnboardingTask> tasks = new ArrayList<>();
                while (resultSet.next()) {
                    tasks.add(map(resultSet));
                }
                return tasks;
            }
        }
    }

    public int insert(OnboardingTask task) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, task);
        }
    }

    public int insert(Connection connection, OnboardingTask task) throws SQLException {
        String sql = "INSERT INTO onboarding_tasks (onboarding_id, task_name, description, is_required, status, completed_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, task.getOnboardingId());
            statement.setString(2, task.getTaskName());
            statement.setString(3, task.getDescription());
            statement.setBoolean(4, task.isRequired());
            statement.setString(5, task.getStatus());
            statement.setTimestamp(6, task.getCompletedAt());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                    return task.getId();
                }
            }
        }
        throw new SQLException("Creating onboarding task did not return a generated id.");
    }

    public boolean update(OnboardingTask task) throws SQLException {
        String sql = "UPDATE onboarding_tasks SET task_name = ?, description = ?, is_required = ? WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, task.getTaskName());
            statement.setString(2, task.getDescription());
            statement.setBoolean(3, task.isRequired());
            statement.setInt(4, task.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(int taskId, String status) throws SQLException {
        try (Connection connection = openConnection()) {
            return updateStatus(connection, taskId, status);
        }
    }

    public boolean updateStatus(Connection connection, int taskId, String status) throws SQLException {
        String sql = "UPDATE onboarding_tasks SET status = ?, completed_at = CASE WHEN ? = 'DONE' THEN CURRENT_TIMESTAMP ELSE NULL END WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, status);
            statement.setInt(3, taskId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int taskId) throws SQLException {
        String sql = "DELETE FROM onboarding_tasks WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            return statement.executeUpdate() == 1;
        }
    }

    public long countRequired(int onboardingId) throws SQLException {
        try (Connection connection = openConnection()) {
            return countRequired(connection, onboardingId);
        }
    }

    public long countRequired(Connection connection, int onboardingId) throws SQLException {
        return countByStatus(connection, onboardingId, null, true);
    }

    public long countRequiredDone(int onboardingId) throws SQLException {
        try (Connection connection = openConnection()) {
            return countRequiredDone(connection, onboardingId);
        }
    }

    public long countRequiredDone(Connection connection, int onboardingId) throws SQLException {
        return countByStatus(connection, onboardingId, "DONE", true);
    }

    public long countDone(int onboardingId) throws SQLException {
        try (Connection connection = openConnection()) {
            return countByStatus(connection, onboardingId, "DONE", false);
        }
    }

    private long countByStatus(Connection connection, int onboardingId, String status, boolean requiredOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM onboarding_tasks WHERE onboarding_id = ?");
        if (requiredOnly) {
            sql.append(" AND is_required = TRUE");
        }
        if (status != null) {
            sql.append(" AND status = ?");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, onboardingId);
            if (status != null) {
                statement.setString(2, status);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private OnboardingTask map(ResultSet resultSet) throws SQLException {
        OnboardingTask task = new OnboardingTask();
        task.setId(resultSet.getInt("id"));
        task.setOnboardingId(resultSet.getInt("onboarding_id"));
        task.setTaskName(resultSet.getString("task_name"));
        task.setDescription(resultSet.getString("description"));
        task.setRequired(resultSet.getBoolean("is_required"));
        task.setStatus(resultSet.getString("status"));
        task.setCompletedAt(resultSet.getTimestamp("completed_at"));
        task.setCreatedAt(resultSet.getTimestamp("created_at"));
        task.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return task;
    }
}
