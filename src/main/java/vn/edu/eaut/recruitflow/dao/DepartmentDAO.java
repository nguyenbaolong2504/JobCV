package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO extends DaoSupport {
    public List<Department> findAll() throws SQLException {
        String sql = "SELECT id, name, description, created_at FROM departments ORDER BY name";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            List<Department> departments = new ArrayList<>();
            while (resultSet.next()) {
                departments.add(map(resultSet));
            }
            return departments;
        }
    }

    public Department findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Department findById(Connection connection, int id) throws SQLException {
        String sql = "SELECT id, name, description, created_at FROM departments WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Department findByName(String name) throws SQLException {
        String sql = "SELECT id, name, description, created_at FROM departments WHERE name = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public int insert(Department department) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, department);
        }
    }

    public int insert(Connection connection, Department department) throws SQLException {
        String sql = "INSERT INTO departments (name, description) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, department.getName());
            statement.setString(2, department.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    department.setId(keys.getInt(1));
                    return department.getId();
                }
            }
        }
        throw new SQLException("Creating department did not return a generated id.");
    }

    public boolean update(Department department) throws SQLException {
        String sql = "UPDATE departments SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, department.getName());
            statement.setString(2, department.getDescription());
            statement.setInt(3, department.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public long count() throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM departments");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private Department map(ResultSet resultSet) throws SQLException {
        Department department = new Department();
        department.setId(resultSet.getInt("id"));
        department.setName(resultSet.getString("name"));
        department.setDescription(resultSet.getString("description"));
        department.setCreatedAt(resultSet.getTimestamp("created_at"));
        return department;
    }
}
