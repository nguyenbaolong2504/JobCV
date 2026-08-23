package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.AuditLogDAO;
import vn.edu.eaut.recruitflow.dao.JobCategoryDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.model.AuditLog;
import vn.edu.eaut.recruitflow.model.JobCategory;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Admin-controlled job and career categories.
 *
 * <p>Categories deliberately have only two levels. This keeps the seeker menu
 * understandable and guarantees a job category filter means one career group
 * plus its direct specialisations, rather than an unbounded tree that is hard
 * to administer and display safely.</p>
 */
public class JobCategoryService {
    private static final int MAX_DISPLAY_ORDER = 10_000;
    private final JobCategoryDAO jobCategoryDAO;
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public JobCategoryService() {
        this(new JobCategoryDAO(), new UserDAO(), new AuditLogDAO());
    }

    JobCategoryService(JobCategoryDAO jobCategoryDAO, UserDAO userDAO, AuditLogDAO auditLogDAO) {
        this.jobCategoryDAO = jobCategoryDAO;
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<JobCategory> getAllCategories() throws BusinessException {
        try {
            return jobCategoryDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh mục nghề nghiệp.", exception);
        }
    }

    public List<JobCategory> getPublicHierarchy() throws BusinessException {
        try {
            return jobCategoryDAO.findActiveHierarchy();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh mục nghề nghiệp.", exception);
        }
    }

    public List<JobCategory> getActiveLeafCategories() throws BusinessException {
        try {
            return jobCategoryDAO.findActiveLeafCategories();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh mục nghề nghiệp.", exception);
        }
    }

    public JobCategory getById(int id) throws BusinessException {
        if (id <= 0) {
            throw new BusinessException("Danh mục không hợp lệ.");
        }
        try {
            JobCategory category = jobCategoryDAO.findById(id);
            if (category == null) {
                throw new BusinessException("Không tìm thấy danh mục.");
            }
            return category;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh mục nghề nghiệp.", exception);
        }
    }

    public int create(String name, Integer parentId, String description, int displayOrder, boolean active, int actorId)
            throws BusinessException {
        JobCategory category = buildCategory(0, name, parentId, description, displayOrder, active);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                requireAdmin(connection, actorId);
                validateParent(connection, category.getParentId(), category.isActive(), category.isActive());
                ensureUniqueName(connection, category, 0);
                int id = jobCategoryDAO.insert(connection, category);
                writeAudit(connection, actorId, "JOB_CATEGORY_CREATED", id,
                        "Created " + category.getName() + parentDescriptor(category.getParentId()));
                connection.commit();
                return id;
            } catch (BusinessException | SQLException exception) {
                rollback(connection);
                if (exception instanceof BusinessException businessException) {
                    throw businessException;
                }
                throw new BusinessException("Không thể tạo danh mục nghề nghiệp.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để tạo danh mục.", exception);
        }
    }

    public void update(int id, String name, Integer parentId, String description, int displayOrder, boolean active,
                       int actorId) throws BusinessException {
        if (id <= 0) {
            throw new BusinessException("Danh mục không hợp lệ.");
        }
        JobCategory submitted = buildCategory(id, name, parentId, description, displayOrder, active);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                requireAdmin(connection, actorId);
                JobCategory existing = jobCategoryDAO.findById(connection, id);
                if (existing == null) {
                    throw new BusinessException("Không tìm thấy danh mục cần cập nhật.");
                }
                if (submitted.getParentId() != null && submitted.getParentId() == id) {
                    throw new BusinessException("Danh mục không thể là danh mục cha của chính nó.");
                }
                if (jobCategoryDAO.hasChildren(connection, id) && submitted.getParentId() != null) {
                    throw new BusinessException("Danh mục đang có danh mục con phải giữ ở cấp danh mục chính.");
                }
                if (!submitted.isActive() && jobCategoryDAO.hasActiveChildren(connection, id)) {
                    throw new BusinessException("Hãy ngừng hiển thị các danh mục con trước khi ngừng hiển thị danh mục chính.");
                }
                if (!submitted.isActive() && jobCategoryDAO.hasJobs(connection, id)) {
                    throw new BusinessException("Không thể ngừng hiển thị danh mục đang được gán cho tin tuyển dụng. Hãy chuyển các tin sang danh mục khác trước.");
                }
                boolean parentChanged = !Objects.equals(existing.getParentId(), submitted.getParentId());
                boolean activatingChild = !existing.isActive() && submitted.isActive();
                validateParent(connection, submitted.getParentId(), submitted.isActive(), parentChanged || activatingChild);
                ensureUniqueName(connection, submitted, id);
                if (!jobCategoryDAO.update(connection, submitted)) {
                    throw new BusinessException("Không thể cập nhật danh mục nghề nghiệp.");
                }
                writeAudit(connection, actorId, "JOB_CATEGORY_UPDATED", id,
                        "Updated " + submitted.getName() + parentDescriptor(submitted.getParentId()));
                connection.commit();
            } catch (BusinessException | SQLException exception) {
                rollback(connection);
                if (exception instanceof BusinessException businessException) {
                    throw businessException;
                }
                throw new BusinessException("Không thể cập nhật danh mục nghề nghiệp.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để cập nhật danh mục.", exception);
        }
    }

    public void delete(int id, int actorId) throws BusinessException {
        if (id <= 0) {
            throw new BusinessException("Danh mục không hợp lệ.");
        }
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                requireAdmin(connection, actorId);
                JobCategory existing = jobCategoryDAO.findById(connection, id);
                if (existing == null) {
                    throw new BusinessException("Không tìm thấy danh mục cần xóa.");
                }
                if (jobCategoryDAO.hasChildren(connection, id)) {
                    throw new BusinessException("Không thể xóa danh mục đang có danh mục con.");
                }
                if (jobCategoryDAO.hasJobs(connection, id)) {
                    throw new BusinessException("Không thể xóa danh mục đang được gán cho tin tuyển dụng.");
                }
                if (!jobCategoryDAO.delete(connection, id)) {
                    throw new BusinessException("Không thể xóa danh mục nghề nghiệp.");
                }
                writeAudit(connection, actorId, "JOB_CATEGORY_DELETED", id, "Deleted " + existing.getName());
                connection.commit();
            } catch (BusinessException | SQLException exception) {
                rollback(connection);
                if (exception instanceof BusinessException businessException) {
                    throw businessException;
                }
                throw new BusinessException("Không thể xóa danh mục nghề nghiệp.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để xóa danh mục.", exception);
        }
    }

    /** Used by JobService before a recruiter can attach a category to a job. */
    public void validateSelectableCategory(Integer categoryId) throws BusinessException {
        if (categoryId == null) {
            return;
        }
        try (Connection connection = DBUtil.getConnection()) {
            JobCategory category = jobCategoryDAO.findById(connection, categoryId);
            if (category == null || !category.isActive()) {
                throw new BusinessException("Danh mục nghề nghiệp không tồn tại hoặc đã ngừng hiển thị.");
            }
            if (category.getParentId() != null) {
                JobCategory parent = jobCategoryDAO.findById(connection, category.getParentId());
                if (parent == null || !parent.isActive()) {
                    throw new BusinessException("Danh mục cha đang ngừng hiển thị; vui lòng chọn danh mục khác.");
                }
            }
            if (jobCategoryDAO.hasActiveChildren(connection, categoryId)) {
                throw new BusinessException("Vui lòng chọn một danh mục con cụ thể cho tin tuyển dụng.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực danh mục nghề nghiệp.", exception);
        }
    }

    private JobCategory buildCategory(int id, String name, Integer parentId, String description, int displayOrder,
                                      boolean active) throws BusinessException {
        JobCategory category = new JobCategory();
        category.setId(id);
        category.setName(validName(name));
        category.setParentId(normalizeParentId(parentId));
        category.setDescription(cleanDescription(description));
        category.setDisplayOrder(validDisplayOrder(displayOrder));
        category.setActive(active);
        return category;
    }

    private void validateParent(Connection connection, Integer parentId, boolean childActive, boolean mustBeVacant)
            throws SQLException, BusinessException {
        if (parentId == null) {
            return;
        }
        JobCategory parent = jobCategoryDAO.findById(connection, parentId);
        if (parent == null) {
            throw new BusinessException("Không tìm thấy danh mục cha.");
        }
        if (parent.getParentId() != null) {
            throw new BusinessException("Chỉ hỗ trợ một cấp danh mục con. Hãy chọn danh mục chính làm danh mục cha.");
        }
        if (mustBeVacant && jobCategoryDAO.hasJobs(connection, parentId)) {
            throw new BusinessException("Danh mục chính đang được gán cho tin tuyển dụng nên không thể thêm danh mục con. Hãy chuyển các tin sang danh mục con trước.");
        }
        if (childActive && !parent.isActive()) {
            throw new BusinessException("Không thể hiển thị danh mục con dưới danh mục chính đang ngừng hiển thị.");
        }
    }

    private void ensureUniqueName(Connection connection, JobCategory submitted, int currentId)
            throws SQLException, BusinessException {
        JobCategory duplicate = jobCategoryDAO.findByNameUnderParent(connection, submitted.getName(), submitted.getParentId());
        if (duplicate != null && duplicate.getId() != currentId) {
            throw new BusinessException("Tên danh mục đã tồn tại trong cùng danh mục cha.");
        }
    }

    private void requireAdmin(Connection connection, int actorId) throws SQLException, BusinessException {
        User actor = userDAO.findById(connection, actorId);
        if (actor == null || !RoleName.ADMIN.name().equals(actor.getRoleName())) {
            throw new BusinessException("Chỉ Admin được phép quản lý danh mục nghề nghiệp.");
        }
    }

    private void writeAudit(Connection connection, int actorId, String action, int categoryId, String details)
            throws SQLException {
        AuditLog audit = new AuditLog();
        audit.setUserId(actorId);
        audit.setAction(action);
        audit.setEntityName("job_categories");
        audit.setEntityId(categoryId);
        audit.setDetails(details);
        auditLogDAO.insert(connection, audit);
    }

    private String validName(String name) throws BusinessException {
        String value = name == null ? "" : name.trim();
        if (value.length() < 2 || value.length() > 120) {
            throw new BusinessException("Tên danh mục phải có từ 2 đến 120 ký tự.");
        }
        return value;
    }

    private String cleanDescription(String description) throws BusinessException {
        String value = description == null ? "" : description.trim();
        if (value.length() > 500) {
            throw new BusinessException("Mô tả danh mục không được quá 500 ký tự.");
        }
        return value.isBlank() ? null : value;
    }

    private Integer normalizeParentId(Integer parentId) throws BusinessException {
        if (parentId == null || parentId == 0) {
            return null;
        }
        if (parentId < 0) {
            throw new BusinessException("Danh mục cha không hợp lệ.");
        }
        return parentId;
    }

    private int validDisplayOrder(int displayOrder) throws BusinessException {
        if (displayOrder < 0 || displayOrder > MAX_DISPLAY_ORDER) {
            throw new BusinessException("Thứ tự hiển thị phải từ 0 đến " + MAX_DISPLAY_ORDER + ".");
        }
        return displayOrder;
    }

    private String parentDescriptor(Integer parentId) {
        return parentId == null ? " as root category" : " under parent #" + parentId;
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Original database error is more useful to the caller.
        }
    }
}
