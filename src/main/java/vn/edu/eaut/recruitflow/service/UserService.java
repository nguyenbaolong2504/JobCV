package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.CandidateProfileDAO;
import vn.edu.eaut.recruitflow.dao.RecruiterProfileDAO;
import vn.edu.eaut.recruitflow.dao.RoleDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.RecruiterProfile;
import vn.edu.eaut.recruitflow.model.Role;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.AuthSession;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Authentication, candidate registration, and admin user-management rules. */
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final CandidateProfileDAO candidateProfileDAO;
    private final RecruiterProfileDAO recruiterProfileDAO;

    public UserService() {
        this(new UserDAO(), new RoleDAO(), new CandidateProfileDAO(), new RecruiterProfileDAO());
    }

    UserService(UserDAO userDAO, RoleDAO roleDAO, CandidateProfileDAO candidateProfileDAO) {
        this(userDAO, roleDAO, candidateProfileDAO, new RecruiterProfileDAO());
    }

    UserService(UserDAO userDAO, RoleDAO roleDAO, CandidateProfileDAO candidateProfileDAO,
                RecruiterProfileDAO recruiterProfileDAO) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.candidateProfileDAO = candidateProfileDAO;
        this.recruiterProfileDAO = recruiterProfileDAO;
    }

    /** Returns null for invalid credentials; database failures remain user-safe. */
    public User authenticate(String email, String password) throws BusinessException {
        if (email == null || password == null) {
            return null;
        }
        try {
            User user = userDAO.findByEmail(AuthValidation.email(email));
            if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
                return null;
            }
            return PasswordUtil.matches(password, user.getPasswordHash()) ? user : null;
        } catch (SQLException exception) {
            // Keep the browser response generic, but retain the actionable cause in the server log.
            // Never log credentials or the submitted password here.
            LOGGER.log(Level.WARNING,
                    "Authentication database query failed. Check JDBC URL, DB credentials, and users/roles schema.",
                    exception);
            throw new BusinessException("Không thể xác thực tài khoản lúc này.", exception);
        }
    }

    /** Backward-compatible name used by older controllers. */
    public User login(String email, String password) {
        try {
            return authenticate(email, password);
        } catch (BusinessException ignored) {
            return null;
        }
    }

    public void registerCandidate(String email, String password, String fullName) throws BusinessException {
        registerAccount(email, password, fullName, "CANDIDATE", null, null, null);
    }

    public void registerAccount(String email, String password, String fullName, String accountType) throws BusinessException {
        registerAccount(email, password, fullName, accountType, null, null, null);
    }

    /**
     * Candidate accounts are active immediately. Recruiter registrations collect verifiable work
     * details and remain INACTIVE until an Admin explicitly activates the account.
     */
    public void registerAccount(String email, String password, String fullName, String accountType,
                                String organizationName, String jobTitle, String workPhone) throws BusinessException {
        String normalizedEmail = AuthValidation.email(email);
        String validatedPassword = AuthValidation.newPassword(password);
        String validatedFullName = AuthValidation.fullName(fullName);
        String roleName = accountType == null ? "" : accountType.trim().toUpperCase(Locale.ROOT);
        if (!"CANDIDATE".equals(roleName) && !"HR".equals(roleName)) {
            throw new BusinessException("Loại tài khoản không hợp lệ.");
        }
        String validatedOrganization = null;
        String validatedJobTitle = null;
        String validatedWorkPhone = null;
        if ("HR".equals(roleName)) {
            validatedOrganization = AuthValidation.organizationName(organizationName);
            validatedJobTitle = AuthValidation.jobTitle(jobTitle);
            validatedWorkPhone = AuthValidation.workPhone(workPhone);
        }

        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (userDAO.findByEmail(connection, normalizedEmail) != null) {
                    throw new BusinessException("Email này đã được đăng ký.");
                }
                Role selectedRole = roleDAO.findByName(connection, roleName);
                if (selectedRole == null) {
                    throw new BusinessException("Vai trò " + roleName + " chưa được cấu hình trong cơ sở dữ liệu.");
                }

                User user = new User();
                user.setEmail(normalizedEmail);
                user.setPasswordHash(PasswordUtil.hash(validatedPassword));
                user.setFullName(validatedFullName);
                user.setRoleId(selectedRole.getId());
                user.setStatus("CANDIDATE".equals(roleName) ? UserStatus.ACTIVE.name() : UserStatus.INACTIVE.name());
                userDAO.insert(connection, user);

                if ("CANDIDATE".equals(roleName)) {
                    CandidateProfile profile = new CandidateProfile();
                    profile.setUserId(user.getId());
                    profile.setExperienceYears(0);
                    candidateProfileDAO.insert(connection, profile);
                } else {
                    RecruiterProfile profile = new RecruiterProfile();
                    profile.setUserId(user.getId());
                    profile.setOrganizationName(validatedOrganization);
                    profile.setJobTitle(validatedJobTitle);
                    profile.setWorkPhone(validatedWorkPhone);
                    recruiterProfileDAO.insert(connection, profile);
                }

                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể đăng ký tài khoản. Vui lòng thử lại.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để đăng ký.", exception);
        }
    }

    /** Compatibility adapter for older code; new controllers use the throwing variant. */
    public boolean registerCandidateSafely(String email, String password, String fullName) {
        try {
            registerCandidate(email, password, fullName);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    public User getById(int userId) throws BusinessException {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new BusinessException("Không tìm thấy người dùng.");
            }
            return user;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông tin người dùng.", exception);
        }
    }

    /** Re-checks account status and role on protected requests so locked accounts lose access immediately. */
    public User validateAuthenticatedSession(int userId, String expectedRole) throws BusinessException {
        if (userId <= 0 || !AuthSession.isSupportedRole(expectedRole)) {
            return null;
        }
        try {
            User user = userDAO.findById(userId);
            if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())
                    || !AuthSession.normalizeRole(expectedRole).equals(AuthSession.normalizeRole(user.getRoleName()))) {
                return null;
            }
            return user;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực phiên đăng nhập lúc này.", exception);
        }
    }

    public PageResult<User> searchUsers(String keyword, String status, String roleName, int page, int pageSize) throws BusinessException {
        try {
            List<User> users = userDAO.list(keyword, status, roleName, page, pageSize);
            long total = userDAO.count(keyword, status, roleName);
            return new PageResult<>(users, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách người dùng.", exception);
        }
    }

    public void changeStatus(int actorId, int targetUserId, String targetStatus) throws BusinessException {
        if (actorId == targetUserId) {
            throw new BusinessException("Không thể tự khóa hoặc thay đổi trạng thái tài khoản của chính bạn.");
        }
        UserStatus status;
        try {
            status = UserStatus.fromValue(targetStatus);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Trạng thái người dùng không hợp lệ.");
        }
        try {
            User target = userDAO.findById(targetUserId);
            if (target == null) {
                throw new BusinessException("Không tìm thấy người dùng.");
            }
            if (!userDAO.updateStatus(targetUserId, status.name())) {
                throw new BusinessException("Không thể cập nhật trạng thái người dùng.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật trạng thái người dùng.", exception);
        }
    }

}
