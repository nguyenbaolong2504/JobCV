package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.AuditLogDAO;
import vn.edu.eaut.recruitflow.dao.CandidateProfileDAO;
import vn.edu.eaut.recruitflow.dao.DepartmentDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.RoleDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.AdminDashboardStats;
import vn.edu.eaut.recruitflow.model.AuditLog;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.Role;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Admin-only user state and audit log operations. */
public class AdminService {
    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;
    private final JobDAO jobDAO;
    private final ApplicationDAO applicationDAO;
    private final AuditLogDAO auditLogDAO;
    private final AuditLogService auditLogService;
    private final RoleDAO roleDAO;
    private final CandidateProfileDAO candidateProfileDAO;

    public AdminService() {
        this(new UserDAO(), new DepartmentDAO(), new JobDAO(), new ApplicationDAO(), new AuditLogDAO(), new AuditLogService(),
                new RoleDAO(), new CandidateProfileDAO());
    }

    AdminService(UserDAO userDAO, DepartmentDAO departmentDAO, JobDAO jobDAO, ApplicationDAO applicationDAO,
                 AuditLogDAO auditLogDAO, AuditLogService auditLogService, RoleDAO roleDAO,
                 CandidateProfileDAO candidateProfileDAO) {
        this.userDAO = userDAO;
        this.departmentDAO = departmentDAO;
        this.jobDAO = jobDAO;
        this.applicationDAO = applicationDAO;
        this.auditLogDAO = auditLogDAO;
        this.auditLogService = auditLogService;
        this.roleDAO = roleDAO;
        this.candidateProfileDAO = candidateProfileDAO;
    }

    public AdminDashboardStats getDashboardStats() throws BusinessException {
        try {
            AdminDashboardStats stats = new AdminDashboardStats();
            stats.setTotalUsers(userDAO.countAll());
            stats.setActiveUsers(userDAO.countByStatus(UserStatus.ACTIVE.name()));
            stats.setLockedUsers(userDAO.countByStatus(UserStatus.LOCKED.name()));
            stats.setTotalDepartments(departmentDAO.count());
            stats.setTotalJobs(jobDAO.countAll());
            stats.setTotalApplications(applicationDAO.countAll());
            return stats;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải Admin dashboard.", exception);
        }
    }

    public PageResult<User> searchUsers(String keyword, RoleName role, UserStatus status, int page, int pageSize)
            throws BusinessException {
        try {
            List<User> users = userDAO.list(keyword, status == null ? null : status.name(), role == null ? null : role.name(), page, pageSize);
            long total = userDAO.count(keyword, status == null ? null : status.name(), role == null ? null : role.name());
            return new PageResult<>(users, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách người dùng.", exception);
        }
    }

    /** Roles are fixed system roles; management here controls safe assignment to users. */
    public List<Role> getRoles(int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            return roleDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách vai trò.", exception);
        }
    }

    public void updateUserRole(int userId, int roleId, int actorId) throws BusinessException {
        requireAdmin(actorId);
        if (actorId == userId) {
            throw new BusinessException("Không thể thay đổi vai trò của chính bạn.");
        }
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User target = userDAO.findById(connection, userId);
                Role role = roleDAO.findById(connection, roleId);
                if (target == null || role == null) {
                    throw new BusinessException("Không tìm thấy người dùng hoặc vai trò.");
                }
                RoleName roleName;
                try {
                    roleName = RoleName.fromValue(role.getRoleName());
                } catch (IllegalArgumentException exception) {
                    throw new BusinessException("Vai trò hệ thống không hợp lệ.");
                }
                if (roleName == null) {
                    throw new BusinessException("Vai trò hệ thống không hợp lệ.");
                }
                if (!userDAO.updateRole(connection, userId, roleId)) {
                    throw new BusinessException("Không thể cập nhật vai trò người dùng.");
                }
                if (roleName == RoleName.CANDIDATE && candidateProfileDAO.findByUserId(connection, userId) == null) {
                    CandidateProfile profile = new CandidateProfile();
                    profile.setUserId(userId);
                    profile.setExperienceYears(0);
                    candidateProfileDAO.insert(connection, profile);
                }
                AuditLog audit = new AuditLog();
                audit.setUserId(actorId);
                audit.setAction("USER_ROLE_CHANGED");
                audit.setEntityName("users");
                audit.setEntityId(userId);
                audit.setDetails("Changed role to " + roleName.name());
                auditLogDAO.insert(connection, audit);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật vai trò người dùng.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật vai trò người dùng.", exception);
        }
    }

    public void updateUserStatus(int userId, UserStatus status, int actorId) throws BusinessException {
        requireAdmin(actorId);
        if (actorId == userId) {
            throw new BusinessException("Không thể thay đổi trạng thái tài khoản của chính bạn.");
        }
        try {
            User target = userDAO.findById(userId);
            if (target == null) throw new BusinessException("Không tìm thấy người dùng.");
            if (!userDAO.updateStatus(userId, status.name())) {
                throw new BusinessException("Không thể cập nhật trạng thái người dùng.");
            }
            auditLogService.record(actorId, "USER_STATUS_CHANGED", "users", userId,
                    "Changed status to " + status.name(), null);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật trạng thái người dùng.", exception);
        }
    }

    public PageResult<AuditLog> searchAuditLogs(String keyword, String entityName, LocalDate fromDate, int page, int pageSize)
            throws BusinessException {
        try {
            // DAO action filter is intentionally exact; keyword therefore narrows by action when supplied.
            String action = keyword == null || keyword.isBlank() ? null : keyword.trim();
            List<AuditLog> logs = auditLogDAO.search(null, action, entityName, page, pageSize);
            if (fromDate != null) {
                logs = logs.stream().filter(log -> log.getCreatedAt() != null
                        && !log.getCreatedAt().toLocalDateTime().toLocalDate().isBefore(fromDate)).toList();
            }
            long total = auditLogDAO.count(null, action, entityName);
            return new PageResult<>(logs, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải nhật ký hệ thống.", exception);
        }
    }

    private void requireAdmin(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !RoleName.ADMIN.name().equals(user.getRoleName())) {
                throw new BusinessException("Chỉ Admin được phép thực hiện thao tác này.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền Admin.", exception);
        }
    }
}
