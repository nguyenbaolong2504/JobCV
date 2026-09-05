-- RECRUITFLOW_APPROVED_DEV_MIGRATION
-- Company is an independent tenant. A user is a member of at most one company.

CREATE TABLE IF NOT EXISTS companies (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    logo_path VARCHAR(500) NULL,
    industry VARCHAR(150) NULL,
    company_size VARCHAR(100) NULL,
    address VARCHAR(255) NULL,
    website VARCHAR(255) NULL,
    description TEXT NULL,
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_companies_name (name),
    KEY idx_companies_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS company_members (
    company_id INT NOT NULL,
    user_id INT NOT NULL,
    member_role ENUM('HR','INTERVIEWER') NOT NULL,
    job_title VARCHAR(150) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, user_id),
    UNIQUE KEY uk_company_members_user (user_id),
    KEY idx_company_members_company_role (company_id, member_role, is_active),
    CONSTRAINT fk_company_members_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_company_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO companies (name, industry, company_size, address, website, description)
SELECT DISTINCT TRIM(rp.organization_name), 'Đa lĩnh vực', 'Chưa cập nhật',
       'Chưa cập nhật', NULL,
       CONCAT('Trang tuyển dụng chính thức của ', TRIM(rp.organization_name), ' trên JobCV.')
FROM recruiter_profiles rp
WHERE rp.organization_name IS NOT NULL AND TRIM(rp.organization_name) <> '';

INSERT IGNORE INTO companies (name, industry, company_size, address, website, description)
SELECT 'JobCV Technologies', 'Công nghệ thông tin', '50 - 200 nhân sự', 'Hà Nội',
       'https://jobcv.vn', 'Nền tảng kết nối ứng viên và nhà tuyển dụng.'
WHERE NOT EXISTS (SELECT 1 FROM companies);

INSERT IGNORE INTO company_members (company_id, user_id, member_role, job_title)
SELECT c.id, rp.user_id, 'HR', rp.job_title
FROM recruiter_profiles rp
JOIN companies c ON c.name = TRIM(rp.organization_name)
JOIN users u ON u.id = rp.user_id
JOIN roles r ON r.id = u.role_id AND r.role_name = 'HR';

INSERT IGNORE INTO company_members (company_id, user_id, member_role, job_title)
SELECT (SELECT MIN(id) FROM companies WHERE status = 'ACTIVE'), u.id, 'HR', 'Nhân sự'
FROM users u JOIN roles r ON r.id = u.role_id
WHERE r.role_name = 'HR';

INSERT IGNORE INTO company_members (company_id, user_id, member_role, job_title)
SELECT (SELECT MIN(id) FROM companies WHERE status = 'ACTIVE'), u.id, 'INTERVIEWER', 'Chuyên viên phỏng vấn'
FROM users u JOIN roles r ON r.id = u.role_id
WHERE r.role_name = 'INTERVIEWER';

DELIMITER $$
DROP PROCEDURE IF EXISTS migrate_company_tenant$$
CREATE PROCEDURE migrate_company_tenant()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND column_name = 'company_id'
    ) THEN
        ALTER TABLE jobs ADD COLUMN company_id INT NULL AFTER created_by;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND column_name = 'benefits'
    ) THEN
        ALTER TABLE jobs ADD COLUMN benefits TEXT NULL AFTER requirements;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND column_name = 'auto_closed'
    ) THEN
        ALTER TABLE jobs ADD COLUMN auto_closed BOOLEAN NOT NULL DEFAULT FALSE AFTER status;
    END IF;

    UPDATE jobs j
    LEFT JOIN company_members cm ON cm.user_id = j.created_by AND cm.member_role = 'HR'
    SET j.company_id = COALESCE(j.company_id, cm.company_id, (SELECT MIN(id) FROM companies WHERE status = 'ACTIVE'))
    WHERE j.company_id IS NULL;

    UPDATE jobs
    SET benefits = 'Chế độ đãi ngộ và quyền lợi sẽ được trao đổi chi tiết trong quá trình tuyển dụng.'
    WHERE benefits IS NULL OR TRIM(benefits) = '';

    UPDATE jobs j
    SET j.status = 'CLOSED', j.auto_closed = TRUE
    WHERE j.status = 'PUBLISHED' AND (
        SELECT COUNT(*) FROM applications a
        WHERE a.job_id = j.id AND a.status NOT IN ('REJECTED','WITHDRAWN')
    ) >= j.number_of_positions;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND column_name = 'company_id' AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE jobs MODIFY COLUMN company_id INT NOT NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND index_name = 'idx_jobs_company_status'
    ) THEN
        ALTER TABLE jobs ADD KEY idx_jobs_company_status (company_id, status, deadline);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.referential_constraints
        WHERE constraint_schema = DATABASE() AND constraint_name = 'fk_jobs_company'
    ) THEN
        ALTER TABLE jobs ADD CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES companies(id);
    END IF;
END$$
CALL migrate_company_tenant()$$
DROP PROCEDURE IF EXISTS migrate_company_tenant$$
DELIMITER ;
