package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Permission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** JDBC persistence for the application RBAC permission matrix. */
public class PermissionDAO extends DaoSupport {
    public List<Permission> findAll() throws SQLException {
        String sql = "SELECT id, permission_code, module, display_name, description FROM permissions "
                + "ORDER BY module, permission_code";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Permission> permissions = new ArrayList<>();
            while (resultSet.next()) {
                permissions.add(map(resultSet));
            }
            return permissions;
        }
    }

    public Set<String> findCodesByRoleId(int roleId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findCodesByRoleId(connection, roleId);
        }
    }

    public Set<String> findCodesByRoleId(Connection connection, int roleId) throws SQLException {
        String sql = "SELECT p.permission_code FROM role_permissions rp "
                + "JOIN permissions p ON p.id = rp.permission_id WHERE rp.role_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<String> codes = new LinkedHashSet<>();
                while (resultSet.next()) {
                    codes.add(resultSet.getString("permission_code"));
                }
                return codes;
            }
        }
    }

    public boolean roleHasPermission(int roleId, String permissionCode) throws SQLException {
        String sql = "SELECT 1 FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id "
                + "WHERE rp.role_id = ? AND p.permission_code = ? LIMIT 1";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            statement.setString(2, permissionCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void replaceRolePermissions(Connection connection, int roleId, Collection<String> permissionCodes)
            throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM role_permissions WHERE role_id = ?")) {
            delete.setInt(1, roleId);
            delete.executeUpdate();
        }
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO role_permissions (role_id, permission_id) "
                + "SELECT ?, id FROM permissions WHERE permission_code = ?";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            for (String permissionCode : permissionCodes) {
                insert.setInt(1, roleId);
                insert.setString(2, permissionCode);
                if (insert.executeUpdate() != 1) {
                    throw new SQLException("Permission không tồn tại: " + permissionCode);
                }
            }
        }
    }

    private Permission map(ResultSet resultSet) throws SQLException {
        Permission permission = new Permission();
        permission.setId(resultSet.getInt("id"));
        permission.setPermissionCode(resultSet.getString("permission_code"));
        permission.setModule(resultSet.getString("module"));
        permission.setDisplayName(resultSet.getString("display_name"));
        permission.setDescription(resultSet.getString("description"));
        return permission;
    }
}
