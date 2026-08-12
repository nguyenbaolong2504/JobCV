package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.DepartmentDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.model.Department;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.List;

public class DepartmentService {
    private final DepartmentDAO departmentDAO;
    private final UserDAO userDAO;

    public DepartmentService() {
        this(new DepartmentDAO(), new UserDAO());
    }

    DepartmentService(DepartmentDAO departmentDAO, UserDAO userDAO) {
        this.departmentDAO = departmentDAO;
        this.userDAO = userDAO;
    }

    public List<Department> getAllDepartments() throws BusinessException {
        try {
            return departmentDAO.findAll();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách phòng ban.", exception);
        }
    }

    public Department getById(int id) throws BusinessException {
        try {
            Department department = departmentDAO.findById(id);
            if (department == null) {
                throw new BusinessException("Không tìm thấy phòng ban.");
            }
            return department;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải phòng ban.", exception);
        }
    }

    public void create(String name, String description) throws BusinessException {
        Department department = new Department();
        department.setName(validName(name));
        department.setDescription(cleanDescription(description));
        try {
            if (departmentDAO.findByName(department.getName()) != null) {
                throw new BusinessException("Tên phòng ban đã tồn tại.");
            }
            departmentDAO.insert(department);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tạo phòng ban.", exception);
        }
    }

    public void create(String name, String description, int actorId) throws BusinessException {
        requireAdmin(actorId);
        create(name, description);
    }

    public void update(int id, String name, String description) throws BusinessException {
        Department department = getById(id);
        String newName = validName(name);
        try {
            Department duplicate = departmentDAO.findByName(newName);
            if (duplicate != null && duplicate.getId() != id) {
                throw new BusinessException("Tên phòng ban đã tồn tại.");
            }
            department.setName(newName);
            department.setDescription(cleanDescription(description));
            if (!departmentDAO.update(department)) {
                throw new BusinessException("Không thể cập nhật phòng ban.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật phòng ban.", exception);
        }
    }

    public void update(int id, String name, String description, int actorId) throws BusinessException {
        requireAdmin(actorId);
        update(id, name, description);
    }

    public void delete(int id) throws BusinessException {
        try {
            if (!departmentDAO.delete(id)) {
                throw new BusinessException("Không tìm thấy phòng ban để xóa.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xóa phòng ban đang được sử dụng.", exception);
        }
    }

    public void delete(int id, int actorId) throws BusinessException {
        requireAdmin(actorId);
        delete(id);
    }

    private String validName(String name) throws BusinessException {
        String normalized = name == null ? "" : name.trim();
        if (normalized.length() < 2 || normalized.length() > 100) {
            throw new BusinessException("Tên phòng ban phải có từ 2 đến 100 ký tự.");
        }
        return normalized;
    }

    private String cleanDescription(String description) throws BusinessException {
        String value = description == null ? null : description.trim();
        if (value != null && value.length() > 2000) {
            throw new BusinessException("Mô tả phòng ban không được quá 2000 ký tự.");
        }
        return value == null || value.isBlank() ? null : value;
    }

    private void requireAdmin(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !RoleName.ADMIN.name().equals(user.getRoleName())) {
                throw new BusinessException("Chỉ Admin được phép quản lý phòng ban.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền Admin.", exception);
        }
    }
}
