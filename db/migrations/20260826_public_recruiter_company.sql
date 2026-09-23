-- RECRUITFLOW_APPROVED_DEV_MIGRATION
-- Supply an organisation profile for the bundled HR demo account so public company pages
-- can be backed by recruiter registration data instead of synthetic department companies.
-- Existing recruiter profiles are never overwritten.

INSERT INTO recruiter_profiles (user_id, organization_name, job_title, work_phone)
SELECT u.id, 'JobCV Technologies', 'HR Manager', '0900000000'
FROM users u
JOIN roles r ON r.id = u.role_id
WHERE u.email = 'hr@recruitflow.com'
  AND r.role_name = 'HR'
  AND NOT EXISTS (SELECT 1 FROM recruiter_profiles rp WHERE rp.user_id = u.id);
