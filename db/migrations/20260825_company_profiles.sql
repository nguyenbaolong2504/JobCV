-- RECRUITFLOW_APPROVED_DEV_MIGRATION
-- Employer-branding fields stored on the existing one-to-one recruiter profile.

ALTER TABLE recruiter_profiles ADD COLUMN industry VARCHAR(120) NULL AFTER work_phone;
ALTER TABLE recruiter_profiles ADD COLUMN company_size VARCHAR(60) NULL AFTER industry;
ALTER TABLE recruiter_profiles ADD COLUMN address VARCHAR(255) NULL AFTER company_size;
ALTER TABLE recruiter_profiles ADD COLUMN website VARCHAR(255) NULL AFTER address;
ALTER TABLE recruiter_profiles ADD COLUMN description TEXT NULL AFTER website;
ALTER TABLE recruiter_profiles ADD COLUMN logo_path VARCHAR(255) NULL AFTER description;
ALTER TABLE recruiter_profiles ADD COLUMN cover_path VARCHAR(255) NULL AFTER logo_path;
ALTER TABLE recruiter_profiles ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER cover_path;

-- Repair only placeholder profiles created by the earlier development migration.
UPDATE recruiter_profiles rp
JOIN users u ON u.id = rp.user_id
SET rp.is_verified = FALSE
WHERE u.email <> 'hr@recruitflow.com'
  AND rp.logo_path = 'generic-careers.svg'
  AND (rp.description IS NULL OR TRIM(rp.description) = '');

-- Existing databases should run through DatabaseMigrationListener, which applies these additions
-- idempotently and creates a profile for any HR account that predates recruiter self-registration.
