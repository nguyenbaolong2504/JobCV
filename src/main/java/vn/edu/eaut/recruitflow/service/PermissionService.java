package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.AuditLogDAO;
import vn.edu.eaut.recruitflow.dao.PermissionDAO;
import vn.edu.eaut.recruitflow.dao.RoleDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.PermissionCode;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.model.AuditLog;
import vn.edu.eaut.recruitflow.model.Permission;
import vn.edu.eaut.recruitflow.model.Role;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.PermissionPolicy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Role-based permission matrix with a fail-safe, backward-compatible fallback before migration. */
public class PermissionService {
    private static final Set<PermissionCode> REQUIRED_ADMIN_PERMISSIONS = EnumSet.of(
            PermissionCode.ADMIN_DASHBOARD_VIEW,
            PermissionCode.ADMIN_USERS_MANAGE,
            PermissionCode.ADMIN_PERMISSIONS_MANAGE);

    private final PermissionDAO permissionDAO;
    private final RoleDAO roleDAO;
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public PermissionService() {
        this(new PermissionDAO(), new RoleDAO(), new UserDAO(), new AuditLogDAO());
    }

    PermissionService(PermissionDAO permissionDAO, RoleDAO roleDAO, UserDAO userDAO, AuditLogDAO auditLogDAO) {
        this.permissionDAO = permissionDAO;
        this.roleDAO = roleDAO;
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public boolean hasPermission(int userId, String roleName, PermissionCode permission) throws SQLException {
        if (permission == null) return true;
        try {
            User user = userDAO.findById(userId);
            if (user == null || user.getRoleName() == null || !user.getRoleName().equalsIgnoreCase(roleName)) {
                return false;
            }
            Role role = roleDAO.findByName(user.getRoleName());
            return role != null && permissionDAO.roleHasPermission(role.getId(), permission.name());
        } catch (SQLException exception) {
            if (isPermissionSchemaUnavailable(exception)) {
                return fallbackHasPermission(roleName, permission);
            }
            throw exception;
        }
    }

    public List<Permission> getPermissions(int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            return permissionDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException(migrationMessage(exception, "Không thể tải cấu hình phân quyền."), exception);
        }
    }

    public List<Role> getRoles(int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            return roleDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải các vai trò hệ thống.", exception);
        }
    }

    public Set<String> getRolePermissionCodes(int roleId, int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            return permissionDAO.findCodesByRoleId(roleId);
        } catch (SQLException exception) {
            throw new BusinessException(migrationMessage(exception, "Không thể tải quyền của vai trò."), exception);
        }
    }

    public void updateRolePermissions(int roleId, Collection<String> requestedCodes, int actorId) throws BusinessException {
        requireAdmin(actorId);
        Set<String> normalizedCodes = normalizeCodes(requestedCodes);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Role role = roleDAO.findById(connection, roleId);
                if (role == null) {
                    throw new BusinessException("Không tìm thấy vai trò cần cập nhật.");
                }
                RoleName roleName = RoleName.fromValue(role.getRoleName());
                validateCodesForRole(roleName, normalizedCodes);
                if (roleName == RoleName.ADMIN && !normalizedCodes.containsAll(REQUIRED_ADMIN_PERMISSIONS.stream()
                        .map(Enum::name).collect(java.util.stream.Collectors.toSet()))) {
                    throw new BusinessException("Vai trò ADMIN phải giữ quyền quản trị cốt lõi để tránh khóa hệ thống.");
                }
                permissionDAO.replaceRolePermissions(connection, roleId, normalizedCodes);
                AuditLog audit = new AuditLog();
                audit.setUserId(actorId);
                audit.setAction("ROLE_PERMISSIONS_UPDATED");
                audit.setEntityName("roles");
                audit.setEntityId(roleId);
                audit.setDetails("Updated " + normalizedCodes.size() + " permissions for " + roleName.name());
                auditLogDAO.insert(connection, audit);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException(migrationMessage(exception, "Không thể cập nhật phân quyền."), exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException(migrationMessage(exception, "Không thể cập nhật phân quyền."), exception);
        }
    }

    private Set<String> normalizeCodes(Collection<String> requestedCodes) throws BusinessException {
        Set<String> normalized = new LinkedHashSet<>();
        if (requestedCodes == null) return normalized;
        for (String value : requestedCodes) {
            try {
                normalized.add(PermissionCode.fromValue(value).name());
            } catch (IllegalArgumentException exception) {
                throw new BusinessException("Permission không hợp lệ.");
            }
        }
        return normalized;
    }

    /**
     * Workspace access remains role-scoped in {@code AuthorizationFilter}; therefore granting
     * an HR action to a Candidate would look successful in the UI yet can never be used. Reject
     * such a configuration instead of storing a misleading matrix.
     */
    private void validateCodesForRole(RoleName roleName, Set<String> permissionCodes) throws BusinessException {
        if (roleName == RoleName.ADMIN) {
            return;
        }
        String prefix = switch (roleName) {
            case HR -> "HR_";
            case INTERVIEWER -> "INTERVIEWER_";
            case CANDIDATE -> "CANDIDATE_";
            case ADMIN -> "";
        };
        for (String code : permissionCodes) {
            if (!code.startsWith(prefix)) {
                throw new BusinessException("Không thể cấp quyền " + code + " cho vai trò " + roleName + ".");
            }
        }
    }

    private void requireAdmin(int actorId) throws BusinessException {
        try {
            User actor = userDAO.findById(actorId);
            if (actor == null || !RoleName.ADMIN.name().equals(actor.getRoleName())) {
                throw new BusinessException("Chỉ Admin được phép cấu hình phân quyền.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền Admin.", exception);
        }
    }

    private boolean fallbackHasPermission(String roleName, PermissionCode permission) {
        try {
            return PermissionPolicy.defaultPermissions(RoleName.fromValue(roleName)).contains(permission);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isPermissionSchemaUnavailable(SQLException exception) {
        return "42S02".equals(exception.getSQLState()) || exception.getErrorCode() == 1146;
    }

    private String migrationMessage(SQLException exception, String fallback) {
        return isPermissionSchemaUnavailable(exception)
                ? "Thiếu migration phân quyền. Hãy chạy migration RBAC trước khi cấu hình quyền."
                : fallback;
    }
}
