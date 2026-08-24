package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DaoSupport {
    private static final String SELECT_USER = "SELECT u.id, u.email, u.password_hash, u.full_name, u.role_id, u.status, u.session_version, u.created_at, u.updated_at, r.role_name "
            + "FROM users u JOIN roles r ON r.id = u.role_id ";

    public User findByEmail(String email) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByEmail(connection, email);
        }
    }

    public User findByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_USER + "WHERE u.email = ?")) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public User findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_USER + "WHERE u.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<User> list(String keyword, String status, String roleName, int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_USER + "WHERE 1 = 1");
        List<String> values = new ArrayList<>();
        appendFilters(sql, values, keyword, status, roleName);
        sql.append(" ORDER BY u.created_at DESC, u.id DESC LIMIT ? OFFSET ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindFilters(statement, values);
            int position = values.size() + 1;
            statement.setInt(position++, pageSize(pageSize));
            statement.setInt(position, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(map(resultSet));
                }
                return users;
            }
        }
    }

    public long count(String keyword, String status, String roleName) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u JOIN roles r ON r.id = u.role_id WHERE 1 = 1");
        List<String> values = new ArrayList<>();
        appendFilters(sql, values, keyword, status, roleName);
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindFilters(statement, values);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countByStatus(String status) throws SQLException {
        return count(null, status, null);
    }

    public long countAll() throws SQLException {
        return count(null, null, null);
    }

    public int insert(User user) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, user);
        }
    }

    public int insert(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, full_name, role_id, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setInt(4, user.getRoleId());
            statement.setString(5, user.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                    return user.getId();
                }
            }
        }
        throw new SQLException("Creating user did not return a generated id.");
    }

    public boolean update(User user) throws SQLException {
        try (Connection connection = openConnection()) {
            return update(connection, user);
        }
    }

    public boolean update(Connection connection, User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, full_name = ?, role_id = ?, status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getFullName());
            statement.setInt(3, user.getRoleId());
            statement.setString(4, user.getStatus());
            statement.setInt(5, user.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updatePassword(int userId, String passwordHash) throws SQLException {
        try (Connection connection = openConnection()) {
            return updatePassword(connection, userId, passwordHash);
        }
    }

    /** Serializes sensitive actions for one account, such as accepting an offer. */
    public boolean lockById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean updatePassword(Connection connection, int userId, String passwordHash) throws SQLException {
        // Keep the password update and global session revocation in one atomic row update.
        // AuthenticationFilter compares this version with the value stored at login.
        String sql = "UPDATE users SET password_hash = ?, session_version = session_version + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(int userId, String status) throws SQLException {
        try (Connection connection = openConnection()) {
            return updateStatus(connection, userId, status);
        }
    }

    public boolean updateStatus(Connection connection, int userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateRole(Connection connection, int userId, int roleId) throws SQLException {
        String sql = "UPDATE users SET role_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    private void appendFilters(StringBuilder sql, List<String> values, String keyword, String status, String roleName) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ?)");
            String value = '%' + keyword.trim().toLowerCase() + '%';
            values.add(value);
            values.add(value);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND u.status = ?");
            values.add(status.trim().toUpperCase());
        }
        if (roleName != null && !roleName.isBlank()) {
            sql.append(" AND r.role_name = ?");
            values.add(roleName.trim().toUpperCase());
        }
    }

    private void bindFilters(PreparedStatement statement, List<String> values) throws SQLException {
        for (int index = 0; index < values.size(); index++) {
            statement.setString(index + 1, values.get(index));
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("full_name"));
        user.setRoleId(resultSet.getInt("role_id"));
        user.setRoleName(resultSet.getString("role_name"));
        user.setStatus(resultSet.getString("status"));
        user.setSessionVersion(resultSet.getInt("session_version"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return user;
    }
}
