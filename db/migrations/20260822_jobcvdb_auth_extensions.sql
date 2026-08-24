-- RECRUITFLOW_SAFE_ADDITIVE_MIGRATION
-- Additive migration for an existing RecruitFlow-compatible installation.
-- Target: MySQL 8.0.16+. Run it against the schema selected in MySQL Workbench or by the dev runner.
-- Make a database backup before applying any schema migration.
-- This file only creates missing tables and indexes; it never deletes, truncates, or seeds data.

-- Recruiter registrations wait for an Admin to verify organisation details before activation.
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

-- Raw OTP values are never stored. These tables contain a BCrypt hash, expiry and attempt count only.
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

-- Persist only Google's stable subject; no Google access, refresh or ID token is stored.
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

-- Expected output: four rows after a successful first run.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'recruiter_profiles',
      'password_reset_otps',
      'login_verification_otps',
      'oauth_accounts'
  )
ORDER BY table_name;
