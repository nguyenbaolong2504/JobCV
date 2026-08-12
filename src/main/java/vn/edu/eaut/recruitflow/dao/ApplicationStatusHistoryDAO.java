package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.ApplicationStatusHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ApplicationStatusHistoryDAO extends DaoSupport {
    private static final String SELECT_HISTORY = "SELECT h.id, h.application_id, h.old_status, h.new_status, h.changed_by, h.remarks, h.changed_at, "
            + "u.full_name AS changed_by_name FROM application_status_history h LEFT JOIN users u ON u.id = h.changed_by ";

    public List<ApplicationStatusHistory> findByApplicationId(int applicationId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByApplicationId(connection, applicationId);
        }
    }

    public List<ApplicationStatusHistory> findByApplication(int applicationId) throws SQLException {
        return findByApplicationId(applicationId);
    }

    public List<ApplicationStatusHistory> findByApplicationId(Connection connection, int applicationId) throws SQLException {
        String sql = SELECT_HISTORY + "WHERE h.application_id = ? ORDER BY h.changed_at ASC, h.id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ApplicationStatusHistory> history = new ArrayList<>();
                while (resultSet.next()) {
                    history.add(map(resultSet));
                }
                return history;
            }
        }
    }

    public int insert(ApplicationStatusHistory history) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, history);
        }
    }

    public int insert(Connection connection, ApplicationStatusHistory history) throws SQLException {
        String sql = "INSERT INTO application_status_history (application_id, old_status, new_status, changed_by, remarks) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, history.getApplicationId());
            statement.setString(2, history.getOldStatus());
            statement.setString(3, history.getNewStatus());
            if (history.getChangedBy() == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, history.getChangedBy());
            }
            statement.setString(5, history.getRemarks());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    history.setId(keys.getInt(1));
                    return history.getId();
                }
            }
        }
        throw new SQLException("Creating application status history did not return a generated id.");
    }

    private ApplicationStatusHistory map(ResultSet resultSet) throws SQLException {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setId(resultSet.getInt("id"));
        history.setApplicationId(resultSet.getInt("application_id"));
        history.setOldStatus(resultSet.getString("old_status"));
        history.setNewStatus(resultSet.getString("new_status"));
        history.setChangedBy(getNullableInt(resultSet, "changed_by"));
        history.setChangedByName(resultSet.getString("changed_by_name"));
        history.setRemarks(resultSet.getString("remarks"));
        history.setChangedAt(resultSet.getTimestamp("changed_at"));
        return history;
    }
}
