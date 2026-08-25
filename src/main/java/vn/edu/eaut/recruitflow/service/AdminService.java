package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.AuditLogDAO;
import vn.edu.eaut.recruitflow.dao.CandidateProfileDAO;
import vn.edu.eaut.recruitflow.dao.DepartmentDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.RecruiterProfileDAO;
import vn.edu.eaut.recruitflow.dao.RoleDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.AdminDashboardStats;
import vn.edu.eaut.recruitflow.model.AuditLog;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.RecruiterProfile;
import vn.edu.eaut.recruitflow.model.Role;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    private final RecruiterProfileDAO recruiterProfileDAO;

    public AdminService() {
        this(new UserDAO(), new DepartmentDAO(), new JobDAO(), new ApplicationDAO(), new AuditLogDAO(), new AuditLogService(),
                new RoleDAO(), new CandidateProfileDAO(), new RecruiterProfileDAO());
    }

    AdminService(UserDAO userDAO, DepartmentDAO departmentDAO, JobDAO jobDAO, ApplicationDAO applicationDAO,
                 AuditLogDAO auditLogDAO, AuditLogService auditLogService, RoleDAO roleDAO,
                 CandidateProfileDAO candidateProfileDAO) {
        this(userDAO, departmentDAO, jobDAO, applicationDAO, auditLogDAO, auditLogService, roleDAO, candidateProfileDAO,
                new RecruiterProfileDAO());
    }

    AdminService(UserDAO userDAO, DepartmentDAO departmentDAO, JobDAO jobDAO, ApplicationDAO applicationDAO,
                 AuditLogDAO auditLogDAO, AuditLogService auditLogService, RoleDAO roleDAO,
                 CandidateProfileDAO candidateProfileDAO, RecruiterProfileDAO recruiterProfileDAO) {
        this.userDAO = userDAO;
        this.departmentDAO = departmentDAO;
        this.jobDAO = jobDAO;
        this.applicationDAO = applicationDAO;
        this.auditLogDAO = auditLogDAO;
        this.auditLogService = auditLogService;
        this.roleDAO = roleDAO;
        this.candidateProfileDAO = candidateProfileDAO;
        this.recruiterProfileDAO = recruiterProfileDAO;
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
            stats.setCandidates(userDAO.countByRole(RoleName.CANDIDATE.name()));
            stats.setEmployers(userDAO.countByRole(RoleName.HR.name()));
            stats.setActiveJobs(jobDAO.countActiveJobs());
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

    /** Lets an Admin see the organization data needed before activating a pending HR account. */
    public Map<Integer, RecruiterProfile> getRecruiterProfiles(List<User> users, int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            List<Integer> recruiterIds = users == null ? List.of() : users.stream()
                    .filter(user -> RoleName.HR.name().equals(user.getRoleName()))
                    .map(User::getId)
                    .toList();
            return recruiterProfileDAO.findByUserIds(recruiterIds);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông tin xác minh nhà tuyển dụng.", exception);
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
                if (roleName.name().equals(target.getRoleName())) {
                    connection.commit();
                    return;
                }
                boolean isNewRecruiterAssignment = roleName == RoleName.HR
                        && !RoleName.HR.name().equals(target.getRoleName());
                if (isNewRecruiterAssignment && recruiterProfileDAO.findByUserId(connection, userId) == null) {
                    throw new BusinessException("Không thể chuyển trực tiếp tài khoản này sang HR. Người dùng cần đăng ký loại Nhà tuyển dụng và cung cấp thông tin tổ chức để Admin xét duyệt.");
                }
                if (!userDAO.updateRole(connection, userId, roleId)) {
                    throw new BusinessException("Không thể cập nhật vai trò người dùng.");
                }
                // A manually reassigned recruiter still enters the same review gate as a
                // self-registered recruiter. Activation remains a separate explicit approval.
                if (isNewRecruiterAssignment && UserStatus.ACTIVE.name().equals(target.getStatus())
                        && !userDAO.updateStatus(connection, userId, UserStatus.INACTIVE.name())) {
                    throw new BusinessException("Không thể đưa tài khoản Nhà tuyển dụng vào trạng thái chờ duyệt.");
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
                audit.setDetails("Changed role to " + roleName.name()
                        + (isNewRecruiterAssignment ? "; recruiter approval required before activation" : ""));
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
            List<AuditLog> logs = auditLogDAO.search(keyword, entityName, fromDate, page, pageSize);
            long total = auditLogDAO.count(keyword, entityName, fromDate);
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
