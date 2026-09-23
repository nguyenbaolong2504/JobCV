package vn.edu.eaut.recruitflow.listener;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Creates an idempotent, realistic popular-job data set for analytics demonstrations. */
final class DemoPopularJobSeeder {
    private static final String JOB_CODE = "DEMO-HOT-2026";
    private static final String PASSWORD_HASH = "$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe";
    private static final int CANDIDATE_COUNT = 60;

    private DemoPopularJobSeeder() {}

    static int seed(Connection connection) throws SQLException {
        Integer candidateRoleId = findInt(connection, "SELECT id FROM roles WHERE role_name='CANDIDATE'");
        Integer hrUserId = findInt(connection, "SELECT id FROM users WHERE email='hr@recruitflow.com'");
        Integer companyId = findInt(connection, "SELECT company_id FROM company_members WHERE user_id=(SELECT id FROM users WHERE email='hr@recruitflow.com') AND is_active=TRUE");
        Integer departmentId = findInt(connection, "SELECT id FROM departments WHERE name='Information Technology'");
        if (candidateRoleId == null || hrUserId == null || companyId == null || departmentId == null) return 0;

        boolean originalAutoCommit = connection.getAutoCommit();
        if (originalAutoCommit) connection.setAutoCommit(false);
        try {
            ensurePopularJob(connection, hrUserId, companyId, departmentId);
            Integer jobId = findInt(connection, "SELECT id FROM jobs WHERE job_code='" + JOB_CODE + "'");
            if (jobId == null) throw new SQLException("Demo popular job was not created");
            int inserted = seedCandidatesAndApplications(connection, candidateRoleId, jobId);
            seedOffers(connection, jobId);
            inserted += seedVariedCatalogApplications(connection);
            spreadCatalogApplicationsAcrossHistory(connection);
            if (originalAutoCommit) connection.commit();
            return inserted;
        } catch (SQLException exception) {
            if (originalAutoCommit) connection.rollback();
            throw exception;
        } finally {
            if (originalAutoCommit) connection.setAutoCommit(true);
        }
    }

    private static void ensurePopularJob(Connection connection, int hrUserId, int companyId, int departmentId)
            throws SQLException {
        String sql = "INSERT INTO jobs(job_code,title,department_id,location,employment_type,number_of_positions,"
                + "salary_min,salary_max,description,requirements,benefits,experience_required,deadline,status,created_by,company_id) "
                + "VALUES(?, ?, ?, 'Hanoi', 'FULL_TIME', 120, 25000000, 45000000, ?, ?, ?, 3, ?, 'PUBLISHED', ?, ?) "
                + "ON DUPLICATE KEY UPDATE title=VALUES(title),department_id=VALUES(department_id),location=VALUES(location),"
                + "employment_type=VALUES(employment_type),number_of_positions=VALUES(number_of_positions),"
                + "salary_min=VALUES(salary_min),salary_max=VALUES(salary_max),description=VALUES(description),"
                + "requirements=VALUES(requirements),benefits=VALUES(benefits),deadline=VALUES(deadline),status='PUBLISHED',"
                + "created_by=VALUES(created_by),company_id=VALUES(company_id)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, JOB_CODE);
            statement.setString(2, "Senior Java Backend Engineer - Sản phẩm tuyển dụng AI");
            statement.setInt(3, departmentId);
            statement.setString(4, "Phát triển nền tảng tuyển dụng quy mô lớn, tối ưu REST API và các dịch vụ phân tích hồ sơ ứng viên.");
            statement.setString(5, "Từ 3 năm kinh nghiệm Java, SQL, thiết kế API; ưu tiên Spring, Docker và kiến thức cloud.");
            statement.setString(6, "Thu nhập cạnh tranh, thưởng sản phẩm, bảo hiểm đầy đủ, ngân sách học tập và lịch làm việc linh hoạt.");
            statement.setDate(7, Date.valueOf(LocalDate.now().plusMonths(18)));
            statement.setInt(8, hrUserId);
            statement.setInt(9, companyId);
            statement.executeUpdate();
        }
    }

    private static int seedCandidatesAndApplications(Connection connection, int roleId, int jobId) throws SQLException {
        String userSql = "INSERT INTO users(email,password_hash,full_name,role_id,status) VALUES(?,?,?,?,'ACTIVE') "
                + "ON DUPLICATE KEY UPDATE role_id=VALUES(role_id),status='ACTIVE'";
        String profileSql = "INSERT INTO candidate_profiles(user_id,experience_years,skills,summary) VALUES(?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE experience_years=VALUES(experience_years),skills=VALUES(skills),summary=VALUES(summary)";
        String resumeSql = "INSERT INTO resumes(candidate_id,file_name,file_path,file_type,file_size,extracted_text,is_default) "
                + "SELECT ?,?,?,?,?,?,TRUE WHERE NOT EXISTS(SELECT 1 FROM resumes WHERE candidate_id=? AND is_default=TRUE)";
        String applicationSql = "INSERT IGNORE INTO applications(job_id,candidate_id,resume_id,status,match_score,cover_letter,applied_at,updated_at) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        int inserted = 0;
        try (PreparedStatement user = connection.prepareStatement(userSql);
             PreparedStatement profile = connection.prepareStatement(profileSql);
             PreparedStatement resume = connection.prepareStatement(resumeSql);
             PreparedStatement application = connection.prepareStatement(applicationSql)) {
            for (int index = 1; index <= CANDIDATE_COUNT; index++) {
                String email = String.format("demo.hotjob.%03d@jobcv.local", index);
                user.setString(1, email); user.setString(2, PASSWORD_HASH);
                user.setString(3, String.format("Ứng viên Demo %02d", index)); user.setInt(4, roleId);
                user.executeUpdate();
                Integer candidateId = findInt(connection, "SELECT id FROM users WHERE email=?", email);
                if (candidateId == null) continue;

                profile.setInt(1, candidateId); profile.setInt(2, 2 + index % 7);
                profile.setString(3, "Java, Spring, MySQL, REST API, Docker, Git, Cloud");
                profile.setString(4, "Hồ sơ dữ liệu mẫu phục vụ trình diễn báo cáo tuyển dụng.");
                profile.executeUpdate();

                String fileName = String.format("CV_Demo_Java_%02d.pdf", index);
                resume.setInt(1, candidateId); resume.setString(2, fileName);
                resume.setString(3, "demo/" + fileName); resume.setString(4, "application/pdf");
                resume.setLong(5, 2048L); resume.setString(6, "Java Spring MySQL REST API Docker Git Cloud");
                resume.setInt(7, candidateId); resume.executeUpdate();
                Integer resumeId = findInt(connection, "SELECT id FROM resumes WHERE candidate_id=? ORDER BY is_default DESC,id LIMIT 1", candidateId);
                if (resumeId == null) continue;

                String status = statusFor(index);
                BigDecimal score = BigDecimal.valueOf(64 + (index * 7) % 34).setScale(2);
                LocalDateTime appliedAt = LocalDateTime.now().minusDays((index * 11L) % 720L).withHour(9 + index % 8).withMinute(index % 60).withSecond(0).withNano(0);
                application.setInt(1, jobId); application.setInt(2, candidateId); application.setInt(3, resumeId);
                application.setString(4, status); application.setBigDecimal(5, score);
                application.setString(6, "Tôi quan tâm vị trí Java Backend và mong muốn tham gia phát triển sản phẩm tuyển dụng quy mô lớn.");
                application.setTimestamp(7, Timestamp.valueOf(appliedAt)); application.setTimestamp(8, Timestamp.valueOf(appliedAt.plusDays(index % 12)));
                inserted += application.executeUpdate();
            }
        }
        return inserted;
    }

    private static void seedOffers(Connection connection, int jobId) throws SQLException {
        String sql = "INSERT IGNORE INTO offers(application_id,salary,start_date,probation_months,location,expiry_date,status,note,created_at) "
                + "SELECT a.id,35000000,DATE_ADD(CURRENT_DATE,INTERVAL 30 DAY),2,'Hà Nội',DATE_ADD(CURRENT_DATE,INTERVAL 14 DAY),"
                + "CASE WHEN a.status='HIRED' THEN 'ACCEPTED' ELSE 'SENT' END,'Thư mời dữ liệu mẫu phục vụ báo cáo.',a.updated_at "
                + "FROM applications a WHERE a.job_id=? AND a.status IN ('OFFERED','HIRED')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobId);
            statement.executeUpdate();
        }
    }

    /**
     * Makes the large 2021-2026 demonstration catalog look like real traffic instead of
     * assigning the same number of applications to every job. The candidate subset and
     * target volume are derived from stable keys, so this remains idempotent on restart.
     */
    private static int seedVariedCatalogApplications(Connection connection) throws SQLException {
        String sql = "INSERT IGNORE INTO applications(job_id,candidate_id,resume_id,status,match_score,cover_letter,applied_at,updated_at) "
                + "SELECT j.id,d.candidate_id,d.resume_id,"
                + "CASE WHEN MOD(d.seed_rank,17)=0 THEN 'HIRED' "
                + "WHEN MOD(d.seed_rank,11)=0 THEN 'OFFERED' "
                + "WHEN MOD(d.seed_rank,7)=0 THEN 'INTERVIEWED' "
                + "WHEN MOD(d.seed_rank,5)=0 THEN 'INTERVIEW_SCHEDULED' "
                + "WHEN MOD(d.seed_rank,4)=0 THEN 'SHORTLISTED' "
                + "WHEN MOD(d.seed_rank,3)=0 THEN 'SCREENING' ELSE 'SUBMITTED' END,"
                + "CAST(60 + MOD(CRC32(CONCAT(j.job_code,'-',d.candidate_id)),4000)/100 AS DECIMAL(5,2)),"
                + "'Hồ sơ dữ liệu mô phỏng dùng để kiểm thử lưu lượng và phễu tuyển dụng.',"
                + "LEAST(CURRENT_TIMESTAMP,DATE_ADD(j.created_at,INTERVAL MOD(CRC32(CONCAT(j.job_code,d.candidate_id)),240) DAY)),"
                + "LEAST(CURRENT_TIMESTAMP,DATE_ADD(j.created_at,INTERVAL (MOD(CRC32(CONCAT(j.job_code,d.candidate_id)),240)+MOD(d.seed_rank,12)) DAY)) "
                + "FROM jobs j JOIN ("
                + "SELECT candidate_id,resume_id,ROW_NUMBER() OVER(ORDER BY candidate_id) AS seed_rank FROM ("
                + "SELECT u.id AS candidate_id,MIN(r.id) AS resume_id FROM users u "
                + "JOIN resumes r ON r.candidate_id=u.id "
                + "WHERE u.email LIKE 'demo.hotjob.%@jobcv.local' GROUP BY u.id"
                + ") candidates"
                + ") d WHERE j.job_code LIKE 'SEED-%' "
                + "AND d.seed_rank <= LEAST(59,38 + CAST(SUBSTRING_INDEX(j.job_code,'-',-1) AS UNSIGNED) "
                + "+ 2 * FLOOR(CAST(SUBSTRING_INDEX(j.job_code,'-',-1) AS UNSIGNED) / 4))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    /**
     * Spreads only generated catalog applications over the full demo-history window.
     * This keeps every tenant's date-filtered report useful from 2021 through today
     * while leaving genuine candidate applications untouched.
     */
    private static void spreadCatalogApplicationsAcrossHistory(Connection connection) throws SQLException {
        String historicalAppliedAt = "DATE_ADD(DATE_ADD(TIMESTAMP('2021-01-01 08:00:00'),"
                + "INTERVAL MOD(CRC32(CONCAT(j.job_code,'-',u.email)),"
                + "GREATEST(1,DATEDIFF(DATE_SUB(CURRENT_DATE,INTERVAL 1 DAY),'2021-01-01')+1)) DAY),"
                + "INTERVAL MOD(CRC32(CONCAT(u.email,'-',j.job_code)),9) HOUR)";
        String processingDays = "CASE a.status WHEN 'SUBMITTED' THEN 0 WHEN 'SCREENING' THEN 3 "
                + "WHEN 'SHORTLISTED' THEN 7 WHEN 'INTERVIEW_SCHEDULED' THEN 14 "
                + "WHEN 'INTERVIEWED' THEN 18 WHEN 'OFFERED' THEN 24 WHEN 'HIRED' THEN 30 "
                + "WHEN 'REJECTED' THEN 10 WHEN 'WITHDRAWN' THEN 5 ELSE 0 END";
        String sql = "UPDATE applications a JOIN jobs j ON j.id=a.job_id "
                + "JOIN users u ON u.id=a.candidate_id SET a.applied_at=" + historicalAppliedAt + ","
                + "a.updated_at=LEAST(CURRENT_TIMESTAMP,DATE_ADD(" + historicalAppliedAt
                + ",INTERVAL " + processingDays + " DAY)) "
                + "WHERE j.job_code LIKE 'SEED-%' "
                + "AND u.email LIKE 'demo.hotjob.%@jobcv.local' "
                + "AND a.cover_letter='Hồ sơ dữ liệu mô phỏng dùng để kiểm thử lưu lượng và phễu tuyển dụng.'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private static String statusFor(int index) {
        if (index <= 16) return "SUBMITTED";
        if (index <= 28) return "SCREENING";
        if (index <= 38) return "SHORTLISTED";
        if (index <= 46) return "INTERVIEW_SCHEDULED";
        if (index <= 51) return "INTERVIEWED";
        if (index <= 55) return "OFFERED";
        if (index <= 58) return "HIRED";
        return index == 59 ? "REJECTED" : "WITHDRAWN";
    }

    private static Integer findInt(Connection connection, String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : null;
            }
        }
    }
}
