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

CREATE TABLE IF NOT EXISTS job_categories (
    id INT NOT NULL AUTO_INCREMENT,
    parent_id INT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_job_categories_parent_name (parent_id, name),
    KEY idx_job_categories_parent_active_order (parent_id, is_active, display_order),
    CONSTRAINT chk_job_categories_display_order CHECK (display_order >= 0),
    CONSTRAINT fk_job_categories_parent FOREIGN KEY (parent_id) REFERENCES job_categories (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE', 'LOCKED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    session_version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_role_status (role_id, status),
    KEY idx_users_status_created_at (status, created_at),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recruiter_profiles (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    organization_name VARCHAR(150) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    work_phone VARCHAR(30) NOT NULL,
    industry VARCHAR(120) NULL,
    company_size VARCHAR(60) NULL,
    address VARCHAR(255) NULL,
    website VARCHAR(255) NULL,
    description TEXT NULL,
    logo_path VARCHAR(255) NULL,
    cover_path VARCHAR(255) NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_recruiter_profiles_user_id (user_id),
    CONSTRAINT fk_recruiter_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    avatar_path VARCHAR(255) NULL,
    phone VARCHAR(20) NULL,
    target_position VARCHAR(150) NULL,
    target_location VARCHAR(100) NULL,
    expected_salary DECIMAL(15,2) NULL,
    career_goal TEXT NULL,
    certificates TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_candidate_profiles_user_id (user_id),
    KEY idx_candidate_profiles_university_major (university, major),
    CONSTRAINT chk_candidate_profiles_experience_years CHECK (experience_years >= 0),
    CONSTRAINT fk_candidate_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
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
    CONSTRAINT fk_jobs_category FOREIGN KEY (category_id) REFERENCES job_categories (id) ON DELETE RESTRICT,
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

CREATE TABLE IF NOT EXISTS saved_jobs (
    candidate_id INT NOT NULL,
    job_id INT NOT NULL,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (candidate_id, job_id),
    KEY idx_saved_jobs_candidate_saved_at (candidate_id, saved_at),
    KEY idx_saved_jobs_job (job_id),
    CONSTRAINT fk_saved_jobs_candidate FOREIGN KEY (candidate_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_jobs_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_alerts (
    id INT NOT NULL AUTO_INCREMENT,
    candidate_id INT NOT NULL,
    name VARCHAR(80) NOT NULL,
    keyword VARCHAR(150) NULL,
    department_id INT NULL,
    location VARCHAR(100) NULL,
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'CONTRACT', 'REMOTE') NULL,
    frequency ENUM('DAILY', 'WEEKLY') NOT NULL DEFAULT 'DAILY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_notified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_job_alerts_candidate_active (candidate_id, is_active),
    KEY idx_job_alerts_department (department_id),
    CONSTRAINT fk_job_alerts_candidate FOREIGN KEY (candidate_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_alerts_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS applications (
    id INT NOT NULL AUTO_INCREMENT,
    job_id INT NOT NULL,
    candidate_id INT NOT NULL,
    resume_id INT NOT NULL,
    status ENUM('SUBMITTED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'INTERVIEWED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'SUBMITTED',
    match_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    cover_letter VARCHAR(2000) NULL,
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

-- Development seed data. The BCrypt hash below has been generated and verified for password: 123456.
INSERT INTO roles (role_name, description) VALUES
    ('ADMIN', 'System Administrator'),
    ('HR', 'Human Resources Manager'),
    ('INTERVIEWER', 'Technical or culture interviewer'),
    ('CANDIDATE', 'Job applicant')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO departments (name, description) VALUES
    ('Information Technology', 'IT and Software Engineering'),
    ('Human Resources', 'HR and Recruitment'),
    ('Marketing', 'Marketing and PR'),
    ('Finance', 'Finance and Accounting'),
    ('Sales', 'Sales and Account Management'),
    ('Customer Service', 'Customer care and customer success'),
    ('Design', 'Graphic, product and motion design'),
    ('Construction', 'Construction, civil engineering and site management'),
    ('Operations', 'Business operations and project coordination')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO users (email, password_hash, full_name, role_id, status)
SELECT 'admin@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'System Admin', id, 'ACTIVE'
FROM roles WHERE role_name = 'ADMIN'
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), full_name = VALUES(full_name), role_id = VALUES(role_id), status = VALUES(status);
INSERT INTO users (email, password_hash, full_name, role_id, status)
SELECT 'hr@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'HR Manager', id, 'ACTIVE'
FROM roles WHERE role_name = 'HR'
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), full_name = VALUES(full_name), role_id = VALUES(role_id), status = VALUES(status);
INSERT INTO users (email, password_hash, full_name, role_id, status)
SELECT 'interviewer@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'Lead Interviewer', id, 'ACTIVE'
FROM roles WHERE role_name = 'INTERVIEWER'
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), full_name = VALUES(full_name), role_id = VALUES(role_id), status = VALUES(status);
INSERT INTO users (email, password_hash, full_name, role_id, status)
SELECT 'candidate@recruitflow.com', '$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe', 'Candidate User', id, 'ACTIVE'
FROM roles WHERE role_name = 'CANDIDATE'
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), full_name = VALUES(full_name), role_id = VALUES(role_id), status = VALUES(status);

INSERT INTO recruiter_profiles
    (user_id, organization_name, job_title, work_phone, industry, company_size, address, website, description, logo_path, is_verified)
SELECT id, 'RecruitFlow Technology', 'HR Manager', '024 7300 1234', 'Công nghệ tuyển dụng & phần mềm',
       '100–500 nhân sự', 'Hà Nội · TP.HCM · Làm việc linh hoạt', 'https://recruitflow.local',
       'Nền tảng công nghệ tuyển dụng tập trung vào trải nghiệm ứng viên, dữ liệu minh bạch và quy trình tuyển dụng liền mạch.',
       'recruitflow-tech.svg', TRUE
FROM users WHERE email = 'hr@recruitflow.com'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO candidate_profiles (user_id, experience_years, skills, summary)
SELECT id, 0, 'Java, JDBC, MySQL, Git', 'Seed candidate account for RecruitFlow demonstrations.'
FROM users WHERE email = 'candidate@recruitflow.com'
ON DUPLICATE KEY UPDATE skills = VALUES(skills), summary = VALUES(summary);

INSERT INTO jobs (job_code, title, department_id, location, employment_type, number_of_positions, salary_min, salary_max, description, requirements, experience_required, deadline, status, created_by) VALUES
    ('JOB-001', 'Java Backend Intern', (SELECT id FROM departments WHERE name = 'Information Technology'), 'Hanoi', 'INTERNSHIP', 3, 3000000, 5000000, 'Support the backend team in building reliable Java services.', 'Basic Java, OOP, JDBC, MySQL and Git.', 0, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-002', 'Java Developer', (SELECT id FROM departments WHERE name = 'Information Technology'), 'Hanoi', 'FULL_TIME', 2, 15000000, 30000000, 'Develop and maintain Java backend services.', 'Java, JDBC, MySQL, REST API and Git.', 2, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-003', 'Frontend Developer', (SELECT id FROM departments WHERE name = 'Information Technology'), 'Ho Chi Minh City', 'FULL_TIME', 1, 15000000, 25000000, 'Build responsive web interfaces.', 'HTML, CSS, JavaScript and Git.', 1, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-004', 'Software Tester', (SELECT id FROM departments WHERE name = 'Information Technology'), 'Da Nang', 'FULL_TIME', 2, 10000000, 20000000, 'Execute manual and automation test plans.', 'Manual testing, SQL, API testing and Git.', 1, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com')),
    ('JOB-005', 'Business Analyst Intern', (SELECT id FROM departments WHERE name = 'Information Technology'), 'Hanoi', 'INTERNSHIP', 2, 3000000, 5000000, 'Assist with requirement analysis and documentation.', 'Communication, UML, SQL and documentation.', 0, '2027-12-31', 'PUBLISHED', (SELECT id FROM users WHERE email = 'hr@recruitflow.com'))
ON DUPLICATE KEY UPDATE
    title = VALUES(title), department_id = VALUES(department_id), location = VALUES(location), employment_type = VALUES(employment_type),
    number_of_positions = VALUES(number_of_positions), salary_min = VALUES(salary_min), salary_max = VALUES(salary_max),
    description = VALUES(description), requirements = VALUES(requirements), experience_required = VALUES(experience_required), deadline = VALUES(deadline),
    status = VALUES(status), created_by = VALUES(created_by);

INSERT INTO job_skills (job_id, skill_name, weight, is_required) VALUES
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
    ((SELECT id FROM jobs WHERE job_code = 'JOB-005'), 'UML', 4, TRUE)
ON DUPLICATE KEY UPDATE weight = VALUES(weight), is_required = VALUES(is_required);
