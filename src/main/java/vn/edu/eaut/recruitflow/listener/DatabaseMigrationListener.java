package vn.edu.eaut.recruitflow.listener;

import vn.edu.eaut.recruitflow.enums.PermissionCode;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.PermissionPolicy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Applies small, backward-compatible schema changes required by newer deployments. */
@WebListener
public class DatabaseMigrationListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try (Connection connection = DBUtil.getConnection()) {
            migrateJobCategories(connection, context);
            addColumnIfMissing(connection, context, "users", "session_version",
                    "ALTER TABLE users ADD COLUMN session_version INT NOT NULL DEFAULT 0 AFTER status");
            migrateAuthenticationExtensions(connection, context);
            migrateCompanyProfiles(connection, context);
            migrateRolePermissions(connection, context);
            if (!columnExists(connection, "candidate_profiles", "avatar_path")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(
                            "ALTER TABLE candidate_profiles ADD COLUMN avatar_path VARCHAR(255) NULL AFTER summary");
                }
                context.log("RecruitFlow migration applied: candidate_profiles.avatar_path");
            }
            if (!columnExists(connection, "applications", "cover_letter")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(
                            "ALTER TABLE applications ADD COLUMN cover_letter VARCHAR(2000) NULL AFTER match_score");
                }
                context.log("RecruitFlow migration applied: applications.cover_letter");
            }
            addColumnIfMissing(connection, context, "candidate_profiles", "phone",
                    "ALTER TABLE candidate_profiles ADD COLUMN phone VARCHAR(20) NULL AFTER avatar_path");
            addColumnIfMissing(connection, context, "candidate_profiles", "target_position",
                    "ALTER TABLE candidate_profiles ADD COLUMN target_position VARCHAR(150) NULL AFTER phone");
            addColumnIfMissing(connection, context, "candidate_profiles", "target_location",
                    "ALTER TABLE candidate_profiles ADD COLUMN target_location VARCHAR(100) NULL AFTER target_position");
            addColumnIfMissing(connection, context, "candidate_profiles", "expected_salary",
                    "ALTER TABLE candidate_profiles ADD COLUMN expected_salary DECIMAL(15,2) NULL AFTER target_location");
            addColumnIfMissing(connection, context, "candidate_profiles", "career_goal",
                    "ALTER TABLE candidate_profiles ADD COLUMN career_goal TEXT NULL AFTER expected_salary");
            addColumnIfMissing(connection, context, "candidate_profiles", "certificates",
                    "ALTER TABLE candidate_profiles ADD COLUMN certificates TEXT NULL AFTER career_goal");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS saved_jobs ("
                        + "candidate_id INT NOT NULL, job_id INT NOT NULL, "
                        + "saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "PRIMARY KEY (candidate_id, job_id), "
                        + "KEY idx_saved_jobs_candidate_saved_at (candidate_id, saved_at), "
                        + "KEY idx_saved_jobs_job (job_id), "
                        + "CONSTRAINT fk_saved_jobs_candidate FOREIGN KEY (candidate_id) "
                        + "REFERENCES users (id) ON DELETE CASCADE, "
                        + "CONSTRAINT fk_saved_jobs_job FOREIGN KEY (job_id) "
                        + "REFERENCES jobs (id) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            }
            context.log("RecruitFlow migration checked: saved_jobs");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS job_alerts ("
                        + "id INT NOT NULL AUTO_INCREMENT, candidate_id INT NOT NULL, "
                        + "name VARCHAR(80) NOT NULL, keyword VARCHAR(150) NULL, department_id INT NULL, "
                        + "location VARCHAR(100) NULL, "
                        + "employment_type ENUM('FULL_TIME','PART_TIME','INTERNSHIP','CONTRACT','REMOTE') NULL, "
                        + "frequency ENUM('DAILY','WEEKLY') NOT NULL DEFAULT 'DAILY', "
                        + "is_active BOOLEAN NOT NULL DEFAULT TRUE, last_notified_at TIMESTAMP NULL, "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                        + "PRIMARY KEY (id), KEY idx_job_alerts_candidate_active (candidate_id, is_active), "
                        + "KEY idx_job_alerts_department (department_id), "
                        + "CONSTRAINT fk_job_alerts_candidate FOREIGN KEY (candidate_id) "
                        + "REFERENCES users (id) ON DELETE CASCADE, "
                        + "CONSTRAINT fk_job_alerts_department FOREIGN KEY (department_id) "
                        + "REFERENCES departments (id) ON DELETE SET NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            }
            context.log("RecruitFlow migration checked: job_alerts");
            int insertedDemoJobs = DemoJobDataSeeder.seed(connection);
            context.log("RecruitFlow demo catalog checked: " + insertedDemoJobs + " new jobs added");
            int insertedDemoEmployers = DemoEmployerDataSeeder.seed(connection);
            context.log("RecruitFlow demo employers checked: " + insertedDemoEmployers + " new profiles added");
        } catch (SQLException exception) {
            // Keep the application deployable when the database is temporarily unavailable.
            // The error remains visible in the Tomcat log and the existing pages can report it.
            context.log("RecruitFlow database migration could not be applied.", exception);
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            return columns.next();
        }
    }

    private void addColumnIfMissing(Connection connection, ServletContext context, String table, String column, String ddl)
            throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(ddl);
        }
        context.log("RecruitFlow migration applied: " + table + "." + column);
    }

    /**
     * Keeps installations created from older versions compatible with the
     * curated category navigation. Root categories are derived from existing
     * departments, so the migration never invents business data.
     */
    private void migrateJobCategories(Connection connection, ServletContext context) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS job_categories ("
                    + "id INT NOT NULL AUTO_INCREMENT, parent_id INT NULL, name VARCHAR(120) NOT NULL, "
                    + "description VARCHAR(500) NULL, display_order INT NOT NULL DEFAULT 0, "
                    + "is_active BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (id), UNIQUE KEY uq_job_categories_parent_name (parent_id, name), "
                    + "KEY idx_job_categories_parent_active_order (parent_id, is_active, display_order), "
                    + "CONSTRAINT fk_job_categories_parent FOREIGN KEY (parent_id) "
                    + "REFERENCES job_categories (id) ON DELETE RESTRICT"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }

        if (!columnExists(connection, "jobs", "category_id")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE jobs ADD COLUMN category_id INT NULL AFTER department_id");
                statement.executeUpdate("ALTER TABLE jobs ADD KEY idx_jobs_category_status (category_id, status)");
                statement.executeUpdate("ALTER TABLE jobs ADD CONSTRAINT fk_jobs_category "
                        + "FOREIGN KEY (category_id) REFERENCES job_categories (id) ON DELETE RESTRICT");
            }
            context.log("RecruitFlow migration applied: jobs.category_id");
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO job_categories (parent_id, name, description, display_order, is_active) "
                    + "SELECT NULL, d.name, LEFT(d.description, 500), d.id, TRUE FROM departments d "
                    + "WHERE NOT EXISTS (SELECT 1 FROM job_categories c "
                    + "WHERE c.parent_id IS NULL AND LOWER(c.name) = LOWER(d.name))");
            statement.executeUpdate("UPDATE jobs j JOIN departments d ON d.id = j.department_id "
                    + "JOIN job_categories c ON c.parent_id IS NULL AND LOWER(c.name) = LOWER(d.name) "
                    + "SET j.category_id = c.id WHERE j.category_id IS NULL");
        }
        context.log("RecruitFlow migration checked: job_categories");
    }

    private void migrateAuthenticationExtensions(Connection connection, ServletContext context) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS recruiter_profiles ("
                    + "id INT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, "
                    + "organization_name VARCHAR(150) NOT NULL, job_title VARCHAR(100) NOT NULL, "
                    + "work_phone VARCHAR(30) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (id), UNIQUE KEY uq_recruiter_profiles_user_id (user_id), "
                    + "CONSTRAINT fk_recruiter_profiles_user FOREIGN KEY (user_id) "
                    + "REFERENCES users (id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS password_reset_otps ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, otp_hash VARCHAR(60) NOT NULL, "
                    + "expires_at TIMESTAMP NOT NULL, attempt_count TINYINT UNSIGNED NOT NULL DEFAULT 0, "
                    + "consumed_at TIMESTAMP NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (id), KEY idx_password_reset_otps_user_active (user_id, consumed_at, expires_at), "
                    + "KEY idx_password_reset_otps_created_at (created_at), "
                    + "CONSTRAINT fk_password_reset_otps_user FOREIGN KEY (user_id) "
                    + "REFERENCES users (id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS login_verification_otps ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, otp_hash VARCHAR(60) NOT NULL, "
                    + "expires_at TIMESTAMP NOT NULL, attempt_count TINYINT UNSIGNED NOT NULL DEFAULT 0, "
                    + "consumed_at TIMESTAMP NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (id), KEY idx_login_verification_otps_user_active (user_id, consumed_at, expires_at), "
                    + "KEY idx_login_verification_otps_created_at (created_at), "
                    + "CONSTRAINT fk_login_verification_otps_user FOREIGN KEY (user_id) "
                    + "REFERENCES users (id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS oauth_accounts ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, provider VARCHAR(30) NOT NULL, "
                    + "provider_subject VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "last_login_at TIMESTAMP NULL, PRIMARY KEY (id), "
                    + "UNIQUE KEY uq_oauth_accounts_provider_subject (provider, provider_subject), "
                    + "UNIQUE KEY uq_oauth_accounts_user_provider (user_id, provider), "
                    + "CONSTRAINT fk_oauth_accounts_user FOREIGN KEY (user_id) "
                    + "REFERENCES users (id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }
        context.log("RecruitFlow migration checked: authentication extensions");
    }

    /** Turns recruiter verification data into a real, editable employer-branding profile. */
    private void migrateCompanyProfiles(Connection connection, ServletContext context) throws SQLException {
        addColumnIfMissing(connection, context, "recruiter_profiles", "industry",
                "ALTER TABLE recruiter_profiles ADD COLUMN industry VARCHAR(120) NULL AFTER work_phone");
        addColumnIfMissing(connection, context, "recruiter_profiles", "company_size",
                "ALTER TABLE recruiter_profiles ADD COLUMN company_size VARCHAR(60) NULL AFTER industry");
        addColumnIfMissing(connection, context, "recruiter_profiles", "address",
                "ALTER TABLE recruiter_profiles ADD COLUMN address VARCHAR(255) NULL AFTER company_size");
        addColumnIfMissing(connection, context, "recruiter_profiles", "website",
                "ALTER TABLE recruiter_profiles ADD COLUMN website VARCHAR(255) NULL AFTER address");
        addColumnIfMissing(connection, context, "recruiter_profiles", "description",
                "ALTER TABLE recruiter_profiles ADD COLUMN description TEXT NULL AFTER website");
        addColumnIfMissing(connection, context, "recruiter_profiles", "logo_path",
                "ALTER TABLE recruiter_profiles ADD COLUMN logo_path VARCHAR(255) NULL AFTER description");
        addColumnIfMissing(connection, context, "recruiter_profiles", "cover_path",
                "ALTER TABLE recruiter_profiles ADD COLUMN cover_path VARCHAR(255) NULL AFTER logo_path");
        addColumnIfMissing(connection, context, "recruiter_profiles", "is_verified",
                "ALTER TABLE recruiter_profiles ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER cover_path");

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO recruiter_profiles "
                    + "(user_id, organization_name, job_title, work_phone, industry, company_size, address, "
                    + "website, description, logo_path, is_verified) "
                    + "SELECT u.id, CASE WHEN u.email = 'hr@recruitflow.com' THEN 'RecruitFlow Technology' "
                    + "ELSE CONCAT(u.full_name, ' - Doanh nghiệp') END, 'Nhà tuyển dụng', 'Chưa cập nhật', "
                    + "CASE WHEN u.email = 'hr@recruitflow.com' THEN 'Công nghệ tuyển dụng & phần mềm' ELSE NULL END, "
                    + "CASE WHEN u.email = 'hr@recruitflow.com' THEN '100–500 nhân sự' ELSE NULL END, "
                    + "CASE WHEN u.email = 'hr@recruitflow.com' THEN 'Hà Nội · TP.HCM · Làm việc linh hoạt' ELSE NULL END, "
                    + "CASE WHEN u.email = 'hr@recruitflow.com' THEN 'https://recruitflow.local' ELSE NULL END, "
                    + "CASE WHEN u.email = 'hr@recruitflow.com' THEN "
                    + "'Nền tảng công nghệ tuyển dụng tập trung vào trải nghiệm ứng viên, dữ liệu minh bạch và quy trình tuyển dụng liền mạch.' "
                    + "ELSE NULL END, CASE WHEN u.email = 'hr@recruitflow.com' THEN 'recruitflow-tech.svg' "
                    + "ELSE 'generic-careers.svg' END, u.email = 'hr@recruitflow.com' "
                    + "FROM users u JOIN roles r ON r.id = u.role_id AND r.role_name = 'HR' "
                    + "WHERE NOT EXISTS (SELECT 1 FROM recruiter_profiles rp WHERE rp.user_id = u.id)");
            // Profiles generated by an older development migration were incorrectly marked verified
            // merely because the account was active. Only explicit review may grant verification.
            statement.executeUpdate("UPDATE recruiter_profiles rp JOIN users u ON u.id = rp.user_id "
                    + "SET rp.is_verified = FALSE WHERE u.email <> 'hr@recruitflow.com' "
                    + "AND rp.logo_path = 'generic-careers.svg' AND (rp.description IS NULL OR TRIM(rp.description) = '')");
        }
        context.log("RecruitFlow migration checked: company profiles");
    }

    private void migrateRolePermissions(Connection connection, ServletContext context) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS permissions ("
                    + "id INT NOT NULL AUTO_INCREMENT, permission_code VARCHAR(100) NOT NULL, "
                    + "module VARCHAR(100) NOT NULL, display_name VARCHAR(150) NOT NULL, description VARCHAR(500) NULL, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id), "
                    + "UNIQUE KEY uq_permissions_code (permission_code), KEY idx_permissions_module (module)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS role_permissions ("
                    + "role_id INT NOT NULL, permission_id INT NOT NULL, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (role_id, permission_id), "
                    + "CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) "
                    + "REFERENCES roles (id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) "
                    + "REFERENCES permissions (id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }

        String upsertPermission = "INSERT INTO permissions "
                + "(permission_code, module, display_name, description) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE module = VALUES(module), display_name = VALUES(display_name), "
                + "description = VALUES(description)";
        try (PreparedStatement statement = connection.prepareStatement(upsertPermission)) {
            for (PermissionCode permission : PermissionCode.values()) {
                statement.setString(1, permission.name());
                statement.setString(2, permission.getModule());
                statement.setString(3, permission.getDisplayName());
                statement.setString(4, permission.getDescription());
                statement.addBatch();
            }
            statement.executeBatch();
        }

        long grantCount;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM role_permissions")) {
            resultSet.next();
            grantCount = resultSet.getLong(1);
        }
        if (grantCount == 0) {
            String grantSql = "INSERT IGNORE INTO role_permissions (role_id, permission_id) "
                    + "SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code = ? "
                    + "WHERE r.role_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(grantSql)) {
                for (RoleName role : RoleName.values()) {
                    for (PermissionCode permission : PermissionPolicy.defaultPermissions(role)) {
                        statement.setString(1, permission.name());
                        statement.setString(2, role.name());
                        statement.addBatch();
                    }
                }
                statement.executeBatch();
            }
        }
        context.log("RecruitFlow migration checked: RBAC permissions");
    }
}
