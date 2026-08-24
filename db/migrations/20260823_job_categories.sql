-- RECRUITFLOW_APPROVED_DEV_MIGRATION
-- Idempotent migration: hierarchical job/career categories for the selected RecruitFlow schema.
-- Target: MySQL 8.0.16+. This migration preserves all existing users, jobs and applications.
-- The starter taxonomy uses INSERT IGNORE; only bundled demo jobs without a category are enriched.

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

-- MySQL has no portable ALTER TABLE IF NOT EXISTS for all versions supported by this project,
-- so guard the optional jobs.category_id column, index and FK through metadata checks.
DROP PROCEDURE IF EXISTS ensure_job_category_job_link;
DELIMITER $$
CREATE PROCEDURE ensure_job_category_job_link()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND column_name = 'category_id'
    ) THEN
        ALTER TABLE jobs ADD COLUMN category_id INT NULL AFTER department_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'jobs' AND index_name = 'idx_jobs_category_status'
    ) THEN
        ALTER TABLE jobs ADD KEY idx_jobs_category_status (category_id, status);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = DATABASE() AND table_name = 'jobs'
          AND constraint_name = 'fk_jobs_category' AND constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE jobs ADD CONSTRAINT fk_jobs_category
            FOREIGN KEY (category_id) REFERENCES job_categories (id) ON DELETE SET NULL;
    END IF;
END$$
DELIMITER ;
CALL ensure_job_category_job_link();
DROP PROCEDURE IF EXISTS ensure_job_category_job_link;

-- Starter taxonomy. INSERT IGNORE intentionally preserves Admin changes on later runs.
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

-- Only enrich the bundled demo rows that have no category yet. Real HR-created jobs are never changed.
UPDATE jobs j
JOIN job_categories c ON c.name = 'Phát triển phần mềm'
JOIN job_categories p ON p.id = c.parent_id AND p.name = 'Công nghệ thông tin'
SET j.category_id = c.id
WHERE j.category_id IS NULL AND j.job_code IN ('JOB-001', 'JOB-002', 'JOB-003');

UPDATE jobs j
JOIN job_categories c ON c.name = 'Kiểm thử phần mềm'
JOIN job_categories p ON p.id = c.parent_id AND p.name = 'Công nghệ thông tin'
SET j.category_id = c.id
WHERE j.category_id IS NULL AND j.job_code = 'JOB-004';

UPDATE jobs j
JOIN job_categories c ON c.name = 'Phân tích nghiệp vụ'
JOIN job_categories p ON p.id = c.parent_id AND p.name = 'Công nghệ thông tin'
SET j.category_id = c.id
WHERE j.category_id IS NULL AND j.job_code = 'JOB-005';

SELECT id, parent_id, name, display_order, is_active
FROM job_categories
ORDER BY COALESCE(parent_id, 0), display_order, name;
