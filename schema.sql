-- RecruitFlow development schema (MySQL 8.0.16+)
-- Do not run this file against production data without a migration plan.
CREATE DATABASE IF NOT EXISTS recruitflow
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE recruitflow;

CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(30) NOT NULL,
    description VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS departments (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_departments_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE', 'LOCKED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    -- Increased whenever a password changes, invalidating all browser sessions created before it.
    session_version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_role_status (role_id, status),
    KEY idx_users_status_created_at (status, created_at),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS candidate_profiles (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    date_of_birth DATE NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NULL,
    address VARCHAR(255) NULL,
    university VARCHAR(150) NULL,
    major VARCHAR(100) NULL,
    experience_years INT NOT NULL DEFAULT 0,
    skills TEXT NULL,
    summary TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_candidate_profiles_user_id (user_id),
    KEY idx_candidate_profiles_university_major (university, major),
    CONSTRAINT chk_candidate_profiles_experience_years CHECK (experience_years >= 0),
    CONSTRAINT fk_candidate_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Action-level permissions: role membership opens a workspace, while these grants decide
-- which modules the role can use. The records are managed by Admin after the initial seed.
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

-- Public career taxonomy. A category is either a top-level group or one direct child of a group.
-- parent_scope_id makes names unique even for root categories (where parent_id is NULL).
CREATE TABLE IF NOT EXISTS job_categories (
    id INT NOT NULL AUTO_INCREMENT,
    parent_id INT NULL,
    parent_scope_id INT GENERATED ALWAYS AS (COALESCE(parent_id, 0)) STORED,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_job_categories_parent_name (parent_scope_id, name),
    KEY idx_job_categories_parent_active_order (parent_id, is_active, display_order, name),
    CONSTRAINT chk_job_categories_display_order CHECK (display_order >= 0),
    CONSTRAINT fk_job_categories_parent FOREIGN KEY (parent_id) REFERENCES job_categories (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Recruiter accounts are created INACTIVE and these fields let an Admin review the request before activation.
CREATE TABLE IF NOT EXISTS recruiter_profiles (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    organization_name VARCHAR(150) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    work_phone VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_recruiter_profiles_user_id (user_id),
    CONSTRAINT fk_recruiter_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OTP values are BCrypt hashes. Raw codes, mail credentials and OAuth tokens are never stored.
CREATE TABLE IF NOT EXISTS password_reset_otps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    otp_hash VARCHAR(60) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempt_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
    consumed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_password_reset_otps_user_active (user_id, consumed_at, expires_at),
    KEY idx_password_reset_otps_created_at (created_at),
    CONSTRAINT chk_password_reset_otps_attempt_count CHECK (attempt_count <= 5),
    CONSTRAINT fk_password_reset_otps_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS login_verification_otps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    otp_hash VARCHAR(60) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempt_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
    consumed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_login_verification_otps_user_active (user_id, consumed_at, expires_at),
    KEY idx_login_verification_otps_created_at (created_at),
    CONSTRAINT chk_login_verification_otps_attempt_count CHECK (attempt_count <= 5),
    CONSTRAINT fk_login_verification_otps_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Only a provider's stable subject is stored, never the Google access token, refresh token or ID token.
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_oauth_accounts_provider_subject (provider, provider_subject),
    UNIQUE KEY uq_oauth_accounts_user_provider (user_id, provider),
    CONSTRAINT fk_oauth_accounts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resumes (
    id INT NOT NULL AUTO_INCREMENT,
    candidate_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    extracted_text MEDIUMTEXT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    -- A generated nullable key permits many non-default CVs and only one default CV per candidate.
    default_candidate_id INT GENERATED ALWAYS AS (CASE WHEN is_default THEN candidate_id ELSE NULL END) STORED,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_resumes_default_candidate (default_candidate_id),
    KEY idx_resumes_candidate_uploaded_at (candidate_id, uploaded_at),
    CONSTRAINT chk_resumes_file_size CHECK (file_size > 0 AND file_size <= 5242880),
    CONSTRAINT fk_resumes_candidate FOREIGN KEY (candidate_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS jobs (
    id INT NOT NULL AUTO_INCREMENT,
    job_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    department_id INT NOT NULL,
    category_id INT NULL,
    location VARCHAR(255) NOT NULL,
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'CONTRACT', 'REMOTE') NOT NULL,
    number_of_positions INT NOT NULL,
    salary_min DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    salary_max DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    description TEXT NOT NULL,
    requirements TEXT NOT NULL,
    experience_required INT NOT NULL DEFAULT 0,
    deadline DATE NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_by INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_jobs_job_code (job_code),
    KEY idx_jobs_status_deadline (status, deadline),
    KEY idx_jobs_department_status (department_id, status),
    KEY idx_jobs_category_status (category_id, status),
    KEY idx_jobs_location_type_status (location, employment_type, status),
    KEY idx_jobs_created_by (created_by),
    CONSTRAINT chk_jobs_positions CHECK (number_of_positions > 0),
    CONSTRAINT chk_jobs_salary_min CHECK (salary_min >= 0),
    CONSTRAINT chk_jobs_salary_range CHECK (salary_max >= salary_min),
    CONSTRAINT chk_jobs_experience_required CHECK (experience_required >= 0),
    CONSTRAINT fk_jobs_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_jobs_category FOREIGN KEY (category_id) REFERENCES job_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_jobs_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_skills (
    id INT NOT NULL AUTO_INCREMENT,
    job_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    weight INT NOT NULL DEFAULT 1,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_job_skills_job_skill (job_id, skill_name),
    KEY idx_job_skills_skill_name (skill_name),
    CONSTRAINT chk_job_skills_weight CHECK (weight > 0),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS applications (
    id INT NOT NULL AUTO_INCREMENT,
    job_id INT NOT NULL,
    candidate_id INT NOT NULL,
    resume_id INT NOT NULL,
    status ENUM('SUBMITTED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'INTERVIEWED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'SUBMITTED',
    match_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_applications_job_candidate (job_id, candidate_id),
    KEY idx_applications_candidate_applied_at (candidate_id, applied_at),
    KEY idx_applications_job_status (job_id, status),
    KEY idx_applications_status_applied_at (status, applied_at),
    KEY idx_applications_resume (resume_id),
    CONSTRAINT chk_applications_match_score CHECK (match_score >= 0 AND match_score <= 100),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE RESTRICT,
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_resume FOREIGN KEY (resume_id) REFERENCES resumes (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS application_status_history (
    id INT NOT NULL AUTO_INCREMENT,
    application_id INT NOT NULL,
    old_status ENUM('SUBMITTED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'INTERVIEWED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN') NULL,
    new_status ENUM('SUBMITTED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'INTERVIEWED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN') NOT NULL,
    changed_by INT NULL,
    remarks TEXT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_application_history_application_changed_at (application_id, changed_at),
    KEY idx_application_history_changed_by (changed_by),
    CONSTRAINT fk_application_history_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_history_changed_by FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interviews (
    id INT NOT NULL AUTO_INCREMENT,
    application_id INT NOT NULL,
    interviewer_id INT NOT NULL,
    interview_type ENUM('ONLINE', 'OFFLINE') NOT NULL,
    interview_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255) NULL,
    meeting_url VARCHAR(500) NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') NOT NULL DEFAULT 'SCHEDULED',
    note TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_interviews_application (application_id),
    KEY idx_interviews_interviewer_schedule (interviewer_id, interview_date, start_time, end_time, status),
    KEY idx_interviews_date_status (interview_date, status),
    CONSTRAINT chk_interviews_time_range CHECK (start_time < end_time),
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_interviews_interviewer FOREIGN KEY (interviewer_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_feedbacks (
    id INT NOT NULL AUTO_INCREMENT,
    interview_id INT NOT NULL,
    technical_score DECIMAL(4,2) NOT NULL,
    communication_score DECIMAL(4,2) NOT NULL,
    experience_score DECIMAL(4,2) NOT NULL,
    attitude_score DECIMAL(4,2) NOT NULL,
    overall_score DECIMAL(4,2) NOT NULL,
    comment TEXT NULL,
    recommendation ENUM('STRONG_HIRE', 'HIRE', 'CONSIDER', 'NO_HIRE') NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_interview_feedbacks_interview (interview_id),
    CONSTRAINT chk_interview_feedback_technical CHECK (technical_score >= 0 AND technical_score <= 10),
    CONSTRAINT chk_interview_feedback_communication CHECK (communication_score >= 0 AND communication_score <= 10),
    CONSTRAINT chk_interview_feedback_experience CHECK (experience_score >= 0 AND experience_score <= 10),
    CONSTRAINT chk_interview_feedback_attitude CHECK (attitude_score >= 0 AND attitude_score <= 10),
    CONSTRAINT chk_interview_feedback_overall CHECK (overall_score >= 0 AND overall_score <= 10),
    CONSTRAINT fk_interview_feedbacks_interview FOREIGN KEY (interview_id) REFERENCES interviews (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS offers (
    id INT NOT NULL AUTO_INCREMENT,
    application_id INT NOT NULL,
    salary DECIMAL(15,2) NOT NULL,
    start_date DATE NOT NULL,
    probation_months INT NOT NULL DEFAULT 2,
    location VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    status ENUM('DRAFT', 'SENT', 'ACCEPTED', 'DECLINED', 'EXPIRED') NOT NULL DEFAULT 'DRAFT',
    note TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_offers_application (application_id),
    KEY idx_offers_status_expiry_date (status, expiry_date),
    CONSTRAINT chk_offers_salary CHECK (salary > 0),
    CONSTRAINT chk_offers_probation_months CHECK (probation_months >= 0 AND probation_months <= 36),
    CONSTRAINT fk_offers_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS onboardings (
    id INT NOT NULL AUTO_INCREMENT,
    application_id INT NOT NULL,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'NOT_STARTED',
    progress DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_onboardings_application (application_id),
    KEY idx_onboardings_status (status),
    CONSTRAINT chk_onboardings_progress CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT fk_onboardings_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS onboarding_tasks (
    id INT NOT NULL AUTO_INCREMENT,
    onboarding_id INT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    status ENUM('TODO', 'IN_PROGRESS', 'DONE') NOT NULL DEFAULT 'TODO',
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_onboarding_tasks_onboarding_status (onboarding_id, status),
    CONSTRAINT fk_onboarding_tasks_onboarding FOREIGN KEY (onboarding_id) REFERENCES onboardings (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    link_url VARCHAR(500) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notifications_user_read_created_at (user_id, is_read, created_at),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NULL,
    action VARCHAR(255) NOT NULL,
    entity_name VARCHAR(100) NULL,
    entity_id INT NULL,
    details TEXT NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_logs_user_created_at (user_id, created_at),
    KEY idx_audit_logs_entity (entity_name, entity_id),
    KEY idx_audit_logs_action_created_at (action, created_at),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Development seed data. It is insert-only: re-running this schema must never reset real
-- passwords, roles, account states, or HR-maintained demo records.
-- The BCrypt hash below has been generated and verified for password: 123456.
INSERT IGNORE INTO roles (role_name, description) VALUES
    ('ADMIN', 'System Administrator'),
    ('HR', 'Human Resources Manager'),
    ('INTERVIEWER', 'Technical or culture interviewer'),
    ('CANDIDATE', 'Job applicant');

-- Baseline RBAC matrix. INSERT IGNORE preserves any later changes made by an Admin.
INSERT IGNORE INTO permissions (permission_code, module, display_name, description) VALUES
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
    ('CANDIDATE_NOTIFICATIONS_VIEW', 'Ứng viên', 'Xem thông báo ứng viên', 'Đọc và cập nhật thông báo cá nhân');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.role_name = 'ADMIN'
   OR (r.role_name = 'HR' AND LEFT(p.permission_code, 3) = 'HR_')
   OR (r.role_name = 'INTERVIEWER' AND LEFT(p.permission_code, 12) = 'INTERVIEWER_')
   OR (r.role_name = 'CANDIDATE' AND LEFT(p.permission_code, 10) = 'CANDIDATE_');

INSERT IGNORE INTO departments (name, description) VALUES
    ('Information Technology', 'IT and Software Engineering'),
    ('Human Resources', 'HR and Recruitment'),
    ('Marketing', 'Marketing and PR'),
    ('Finance', 'Finance and Accounting'),
    ('Sales', 'Sales and Account Management');

-- Default navigation taxonomy. Admin can add, reorder or hide groups and their child categories.
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active) VALUES
    (NULL, 'Công nghệ thông tin', 'Việc làm công nghệ, phần mềm và dữ liệu.', 10, TRUE),
    (NULL, 'Kinh doanh & Bán hàng', 'Việc làm phát triển khách hàng và doanh thu.', 20, TRUE),
    (NULL, 'Marketing & Truyền thông', 'Việc làm thương hiệu, nội dung và tăng trưởng.', 30, TRUE),
    (NULL, 'Tài chính & Kế toán', 'Việc làm kế toán, kiểm toán và tài chính.', 40, TRUE),
    (NULL, 'Nhân sự & Hành chính', 'Việc làm nhân sự, tuyển dụng và vận hành văn phòng.', 50, TRUE);

INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Phát triển phần mềm', 'Backend, frontend, mobile và nền tảng phần mềm.', 10, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Công nghệ thông tin';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Kiểm thử phần mềm', 'QA, QC và kiểm thử tự động.', 20, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Công nghệ thông tin';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Phân tích nghiệp vụ', 'Business Analyst, Product Analyst và hệ thống.', 30, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Công nghệ thông tin';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Phát triển kinh doanh', 'Sales, Account Executive và tư vấn giải pháp.', 10, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Kinh doanh & Bán hàng';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Chăm sóc khách hàng', 'Customer Success, support và chăm sóc khách hàng.', 20, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Kinh doanh & Bán hàng';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Digital Marketing', 'Performance, SEO, social media và quảng cáo số.', 10, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Marketing & Truyền thông';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Nội dung & sáng tạo', 'Content, thiết kế và truyền thông thương hiệu.', 20, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Marketing & Truyền thông';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Kế toán', 'Kế toán tổng hợp, thuế và công nợ.', 10, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Tài chính & Kế toán';
INSERT IGNORE INTO job_categories (parent_id, name, description, display_order, is_active)
SELECT id, 'Tuyển dụng', 'Talent acquisition và employer branding.', 10, TRUE
FROM job_categories WHERE parent_id IS NULL AND name = 'Nhân sự & Hành chính';

INSERT IGNORE INTO users (email, password_hash, full_name, role_id, status)
SELECT 'admin@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'System Admin', id, 'ACTIVE'
FROM roles WHERE role_name = 'ADMIN';
INSERT IGNORE INTO users (email, password_hash, full_name, role_id, status)
SELECT 'hr@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'HR Manager', id, 'ACTIVE'
FROM roles WHERE role_name = 'HR';
INSERT IGNORE INTO users (email, password_hash, full_name, role_id, status)
SELECT 'interviewer@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'Lead Interviewer', id, 'ACTIVE'
FROM roles WHERE role_name = 'INTERVIEWER';
INSERT IGNORE INTO users (email, password_hash, full_name, role_id, status)
SELECT 'candidate@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'Candidate User', id, 'ACTIVE'
FROM roles WHERE role_name = 'CANDIDATE';

INSERT IGNORE INTO candidate_profiles (user_id, experience_years, skills, summary)
SELECT id, 0, 'Java, JDBC, MySQL, Git', 'Seed candidate account for RecruitFlow demonstrations.'
FROM users WHERE email = 'candidate@recruitflow.com';

INSERT IGNORE INTO jobs (job_code, title, department_id, category_id, location, employment_type, number_of_positions, salary_min, salary_max, description, requirements, experience_required, deadline, status, created_by) VALUES
    ('JOB-001', 'Java Backend Intern', (SELECT id FROM departments WHERE name = 'Information Technology'), (SELECT c.id FROM job_categories c JOIN job_categories p ON p.id = c.parent_id WHERE p.name = 'Công nghệ thông tin' AND c.name = 'Phát triển phần mềm'), 'Hanoi', 'INTERNSHIP', 3, 3000000, 5000000, 'Support the backend team in building reliable Java services.', 'Basic Java, OOP, JDBC, MySQL and Git.', 0, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-002', 'Java Developer', (SELECT id FROM departments WHERE name = 'Information Technology'), (SELECT c.id FROM job_categories c JOIN job_categories p ON p.id = c.parent_id WHERE p.name = 'Công nghệ thông tin' AND c.name = 'Phát triển phần mềm'), 'Hanoi', 'FULL_TIME', 2, 15000000, 30000000, 'Develop and maintain Java backend services.', 'Java, JDBC, MySQL, REST API and Git.', 2, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-003', 'Frontend Developer', (SELECT id FROM departments WHERE name = 'Information Technology'), (SELECT c.id FROM job_categories c JOIN job_categories p ON p.id = c.parent_id WHERE p.name = 'Công nghệ thông tin' AND c.name = 'Phát triển phần mềm'), 'Ho Chi Minh City', 'FULL_TIME', 1, 15000000, 25000000, 'Build responsive web interfaces.', 'HTML, CSS, JavaScript and Git.', 1, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-004', 'Software Tester', (SELECT id FROM departments WHERE name = 'Information Technology'), (SELECT c.id FROM job_categories c JOIN job_categories p ON p.id = c.parent_id WHERE p.name = 'Công nghệ thông tin' AND c.name = 'Kiểm thử phần mềm'), 'Da Nang', 'FULL_TIME', 2, 10000000, 20000000, 'Execute manual and automation test plans.', 'Manual testing, SQL, API testing and Git.', 1, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-005', 'Business Analyst Intern', (SELECT id FROM departments WHERE name = 'Information Technology'), (SELECT c.id FROM job_categories c JOIN job_categories p ON p.id = c.parent_id WHERE p.name = 'Công nghệ thông tin' AND c.name = 'Phân tích nghiệp vụ'), 'Hanoi', 'INTERNSHIP', 2, 3000000, 5000000, 'Assist with requirement analysis and documentation.', 'Communication, UML, SQL and documentation.', 0, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com'));

INSERT IGNORE INTO job_skills (job_id, skill_name, weight, is_required) VALUES
    ((SELECT id FROM jobs WHERE job_code = 'JOB-001'), 'Java', 5, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-001'), 'JDBC', 4, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-001'), 'MySQL', 4, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-001'), 'Git', 2, FALSE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-002'), 'Java', 5, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-002'), 'JDBC', 4, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-002'), 'MySQL', 4, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-002'), 'REST API', 3, FALSE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-003'), 'HTML', 4, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-003'), 'CSS', 4, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-003'), 'JavaScript', 5, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-004'), 'Manual Testing', 5, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-004'), 'SQL', 3, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-005'), 'Communication', 5, TRUE),
    ((SELECT id FROM jobs WHERE job_code = 'JOB-005'), 'UML', 4, TRUE);
