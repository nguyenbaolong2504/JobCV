package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.JobAlert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Candidate-scoped CRUD for saved job searches. */
public class JobAlertDAO extends DaoSupport {
    private static final String SELECT_ALERT = "SELECT a.id, a.candidate_id, a.name, a.keyword, a.department_id, "
            + "d.name AS department_name, a.location, a.employment_type, a.frequency, a.is_active, "
            + "a.last_notified_at, a.created_at, a.updated_at FROM job_alerts a "
            + "LEFT JOIN departments d ON d.id = a.department_id ";

    public List<JobAlert> findByCandidateId(int candidateId) throws SQLException {
        String sql = SELECT_ALERT + "WHERE a.candidate_id = ? ORDER BY a.is_active DESC, a.updated_at DESC, a.id DESC";
        List<JobAlert> alerts = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    alerts.add(map(resultSet));
                }
            }
        }
        return alerts;
    }

    public int insert(JobAlert alert) throws SQLException {
        String sql = "INSERT INTO job_alerts (candidate_id, name, keyword, department_id, location, "
                + "employment_type, frequency, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, alert.getCandidateId());
            statement.setString(2, alert.getName());
            statement.setString(3, alert.getKeyword());
            setNullableInt(statement, 4, alert.getDepartmentId());
            statement.setString(5, alert.getLocation());
            if (alert.getEmploymentType() == null) statement.setNull(6, Types.VARCHAR);
            else statement.setString(6, alert.getEmploymentType());
            statement.setString(7, alert.getFrequency());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    alert.setId(keys.getInt(1));
                    return alert.getId();
                }
            }
        }
        throw new SQLException("Creating job alert did not return a generated id.");
    }

    public boolean updateActive(int candidateId, int alertId, boolean active) throws SQLException {
        String sql = "UPDATE job_alerts SET is_active = ? WHERE id = ? AND candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, alertId);
            statement.setInt(3, candidateId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int candidateId, int alertId) throws SQLException {
        String sql = "DELETE FROM job_alerts WHERE id = ? AND candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, alertId);
            statement.setInt(2, candidateId);
            return statement.executeUpdate() == 1;
        }
    }

    public long countByCandidateId(int candidateId, boolean activeOnly) throws SQLException {
        String sql = "SELECT COUNT(*) FROM job_alerts WHERE candidate_id = ?" + (activeOnly ? " AND is_active = TRUE" : "");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private JobAlert map(ResultSet resultSet) throws SQLException {
        JobAlert alert = new JobAlert();
        alert.setId(resultSet.getInt("id"));
        alert.setCandidateId(resultSet.getInt("candidate_id"));
        alert.setName(resultSet.getString("name"));
        alert.setKeyword(resultSet.getString("keyword"));
        alert.setDepartmentId(getNullableInt(resultSet, "department_id"));
        alert.setDepartmentName(resultSet.getString("department_name"));
        alert.setLocation(resultSet.getString("location"));
        alert.setEmploymentType(resultSet.getString("employment_type"));
        alert.setFrequency(resultSet.getString("frequency"));
        alert.setActive(resultSet.getBoolean("is_active"));
        alert.setLastNotifiedAt(resultSet.getTimestamp("last_notified_at"));
        alert.setCreatedAt(resultSet.getTimestamp("created_at"));
        alert.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return alert;
    }
}
