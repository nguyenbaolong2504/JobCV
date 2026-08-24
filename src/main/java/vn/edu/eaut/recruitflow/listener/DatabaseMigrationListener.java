package vn.edu.eaut.recruitflow.listener;

import vn.edu.eaut.recruitflow.util.DBUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
}
