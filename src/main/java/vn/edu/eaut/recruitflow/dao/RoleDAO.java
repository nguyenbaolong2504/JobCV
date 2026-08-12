package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO extends DaoSupport {
    public Role findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Role findById(Connection connection, int id) throws SQLException {
        String sql = "SELECT id, role_name, description FROM roles WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Role findByName(String roleName) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByName(connection, roleName);
        }
    }

    public Role findByName(Connection connection, String roleName) throws SQLException {
        String sql = "SELECT id, role_name, description FROM roles WHERE role_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Role> findAll() throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, role_name, description FROM roles ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            List<Role> roles = new ArrayList<>();
            while (resultSet.next()) {
                roles.add(map(resultSet));
            }
            return roles;
        }
    }

    public int insert(Role role) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, role);
        }
    }

    public int insert(Connection connection, Role role) throws SQLException {
        String sql = "INSERT INTO roles (role_name, description) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, role.getRoleName());
            statement.setString(2, role.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    role.setId(keys.getInt(1));
                    return role.getId();
                }
            }
        }
        throw new SQLException("Creating role did not return a generated id.");
    }

    public boolean update(Role role) throws SQLException {
        String sql = "UPDATE roles SET role_name = ?, description = ? WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.getRoleName());
            statement.setString(2, role.getDescription());
            statement.setInt(3, role.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM roles WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private Role map(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setId(resultSet.getInt("id"));
        role.setRoleName(resultSet.getString("role_name"));
        role.setDescription(resultSet.getString("description"));
        return role;
    }
}
