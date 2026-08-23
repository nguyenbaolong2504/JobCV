-- RECRUITFLOW_APPROVED_DEV_MIGRATION
-- Granular RBAC migration for the selected RecruitFlow schema.
-- This migration creates permission metadata and default role grants without changing users,
-- password hashes, role assignments or existing permission choices.

CREATE TABLE IF NOT EXISTS permissions (
    id INT NOT NULL AUTO_INCREMENT,
    permission_code VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_code (permission_code),
    KEY idx_permissions_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (permission_code, module, display_name, description) VALUES
    ('ADMIN_DASHBOARD_VIEW', 'Quản trị', 'Xem tổng quan quản trị', 'Xem dashboard hệ thống'),
    ('ADMIN_USERS_MANAGE', 'Quản trị', 'Quản lý người dùng', 'Xem, khóa, kích hoạt và gán vai trò người dùng'),
    ('ADMIN_PERMISSIONS_MANAGE', 'Quản trị', 'Quản lý phân quyền', 'Cấu hình ma trận quyền theo vai trò'),
    ('ADMIN_DEPARTMENTS_MANAGE', 'Quản trị', 'Quản lý phòng ban', 'Tạo, sửa và lưu trữ phòng ban'),
    ('ADMIN_CATEGORIES_MANAGE', 'Quản trị', 'Quản lý danh mục', 'Quản lý danh mục nghề nghiệp và danh mục con'),
    ('ADMIN_AUDIT_VIEW', 'Quản trị', 'Xem nhật ký', 'Xem nhật ký audit hệ thống'),
    ('HR_DASHBOARD_VIEW', 'Tuyển dụng', 'Xem dashboard HR', 'Xem số liệu tuyển dụng'),
    ('HR_JOBS_MANAGE', 'Tuyển dụng', 'Quản lý tin tuyển', 'Tạo, sửa, publish, đóng và lưu trữ tin tuyển'),
    ('HR_APPLICATIONS_MANAGE', 'Tuyển dụng', 'Quản lý hồ sơ', 'Sàng lọc và quản lý hồ sơ ứng tuyển'),
    ('HR_INTERVIEWS_MANAGE', 'Tuyển dụng', 'Quản lý phỏng vấn', 'Lên lịch, đổi lịch và hủy phỏng vấn'),
    ('HR_OFFERS_MANAGE', 'Tuyển dụng', 'Quản lý offer', 'Tạo, gửi và quản lý offer'),
    ('HR_ONBOARDING_MANAGE', 'Tuyển dụng', 'Quản lý onboarding', 'Theo dõi và giao việc onboarding'),
    ('HR_REPORTS_VIEW', 'Tuyển dụng', 'Xem báo cáo', 'Xem và xuất báo cáo tuyển dụng'),
    ('HR_NOTIFICATIONS_VIEW', 'Tuyển dụng', 'Xem thông báo HR', 'Đọc và cập nhật thông báo HR'),
    ('INTERVIEWER_DASHBOARD_VIEW', 'Phỏng vấn', 'Xem dashboard Interviewer', 'Xem tổng quan các lịch được phân công'),
    ('INTERVIEWER_INTERVIEWS_VIEW', 'Phỏng vấn', 'Xem lịch được phân công', 'Xem lịch và hồ sơ ứng viên được phân công'),
    ('INTERVIEWER_FEEDBACK_SUBMIT', 'Phỏng vấn', 'Gửi đánh giá', 'Gửi feedback cho lịch phỏng vấn đã kết thúc'),
    ('INTERVIEWER_NOTIFICATIONS_VIEW', 'Phỏng vấn', 'Xem thông báo Interviewer', 'Đọc và cập nhật thông báo phỏng vấn'),
    ('CANDIDATE_JOBS_VIEW', 'Ứng viên', 'Tìm việc', 'Xem và tìm kiếm tin tuyển dụng'),
    ('CANDIDATE_PROFILE_MANAGE', 'Ứng viên', 'Quản lý hồ sơ cá nhân', 'Cập nhật hồ sơ ứng viên'),
    ('CANDIDATE_RESUMES_MANAGE', 'Ứng viên', 'Quản lý CV', 'Tạo, tải lên, tải xuống và chọn CV'),
    ('CANDIDATE_APPLICATIONS_MANAGE', 'Ứng viên', 'Quản lý ứng tuyển', 'Ứng tuyển, xem và rút đơn của bản thân'),
    ('CANDIDATE_INTERVIEWS_VIEW', 'Ứng viên', 'Xem lịch phỏng vấn', 'Xem lịch phỏng vấn của bản thân'),
    ('CANDIDATE_OFFERS_RESPOND', 'Ứng viên', 'Phản hồi offer', 'Xem và phản hồi offer của bản thân'),
    ('CANDIDATE_ONBOARDING_MANAGE', 'Ứng viên', 'Thực hiện onboarding', 'Cập nhật công việc onboarding của bản thân'),
    ('CANDIDATE_NOTIFICATIONS_VIEW', 'Ứng viên', 'Xem thông báo ứng viên', 'Đọc và cập nhật thông báo cá nhân')
ON DUPLICATE KEY UPDATE
    module = VALUES(module), display_name = VALUES(display_name), description = VALUES(description);

-- Grant the standard role matrix only once, at first RBAC installation. Future changes must be
-- managed from /admin/permissions and are never overwritten by a subsequent migration run.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE NOT EXISTS (SELECT 1 FROM role_permissions)
  AND (
       r.role_name = 'ADMIN'
       OR (r.role_name = 'HR' AND LEFT(p.permission_code, 3) = 'HR_')
       OR (r.role_name = 'INTERVIEWER' AND LEFT(p.permission_code, 12) = 'INTERVIEWER_')
       OR (r.role_name = 'CANDIDATE' AND LEFT(p.permission_code, 10) = 'CANDIDATE_')
  );

SELECT permission_code, module, display_name
FROM permissions
ORDER BY module, permission_code;
