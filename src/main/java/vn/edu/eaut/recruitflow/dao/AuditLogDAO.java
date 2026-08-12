package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO extends DaoSupport {
    private static final String SELECT_LOG = "SELECT l.id, l.user_id, l.action, l.entity_name, l.entity_id, l.details, l.ip_address, l.created_at, "
            + "u.full_name AS user_name FROM audit_logs l LEFT JOIN users u ON u.id = l.user_id ";

    public int insert(AuditLog log) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, log);
        }
    }

    public int insert(Connection connection, AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_name, entity_id, details, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, log.getUserId());
            }
            statement.setString(2, log.getAction());
            statement.setString(3, log.getEntityName());
            if (log.getEntityId() == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, log.getEntityId());
            }
            statement.setString(5, log.getDetails());
            statement.setString(6, log.getIpAddress());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    log.setId(keys.getInt(1));
                    return log.getId();
                }
            }
        }
        throw new SQLException("Creating audit log did not return a generated id.");
    }

    public List<AuditLog> search(Integer userId, String action, String entityName, int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_LOG + "WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, userId, action, entityName);
        sql.append(" ORDER BY l.created_at DESC, l.id DESC LIMIT ? OFFSET ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            int index = parameters.size() + 1;
            statement.setInt(index++, pageSize(pageSize));
            statement.setInt(index, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AuditLog> logs = new ArrayList<>();
                while (resultSet.next()) {
                    logs.add(map(resultSet));
                }
                return logs;
            }
        }
    }

    public long count(Integer userId, String action, String entityName) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs l WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, userId, action, entityName);
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> parameters, Integer userId, String action, String entityName) {
        if (userId != null && userId > 0) {
            sql.append(" AND l.user_id = ?");
            parameters.add(userId);
        }
        if (action != null && !action.isBlank()) {
            sql.append(" AND l.action = ?");
            parameters.add(action.trim());
        }
        if (entityName != null && !entityName.isBlank()) {
            sql.append(" AND l.entity_name = ?");
            parameters.add(entityName.trim());
        }
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            if (value instanceof Integer integer) {
                statement.setInt(index + 1, integer);
            } else {
                statement.setString(index + 1, (String) value);
            }
        }
    }

    private AuditLog map(ResultSet resultSet) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(resultSet.getInt("id"));
        log.setUserId(getNullableInt(resultSet, "user_id"));
        log.setAction(resultSet.getString("action"));
        log.setEntityName(resultSet.getString("entity_name"));
        log.setEntityId(getNullableInt(resultSet, "entity_id"));
        log.setDetails(resultSet.getString("details"));
        log.setIpAddress(resultSet.getString("ip_address"));
        log.setCreatedAt(resultSet.getTimestamp("created_at"));
        log.setUserName(resultSet.getString("user_name"));
        return log;
    }
}
