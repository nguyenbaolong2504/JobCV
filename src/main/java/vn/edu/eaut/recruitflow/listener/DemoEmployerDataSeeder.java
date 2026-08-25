package vn.edu.eaut.recruitflow.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Creates an idempotent set of database-backed employers for the local demo catalog.
 * Views and services still render only persisted recruiter profiles and owned jobs.
 */
final class DemoEmployerDataSeeder {
    private static final String DEMO_PASSWORD_HASH =
            "$2a$12$v5qHyl5qQU5TBVS4ZNOclulg2e9nBQ7D91WZ/bIayPjPK2ACDA3xe";

    private static final List<SeedEmployer> EMPLOYERS = List.of(
            employer("horizon.finance@recruitflow.demo", "Horizon Finance", "Mai Thanh Hà",
                    "Tài chính · Kế toán", "100–500 nhân sự", "Hà Nội",
                    "Horizon Finance xây dựng môi trường tài chính hiện đại, minh bạch và chú trọng phát triển năng lực phân tích.",
                    "horizon-finance.svg", "JOB-006", "JOB-007", "JOB-008"),
            employer("peoplefirst@recruitflow.demo", "PeopleFirst Vietnam", "Nguyễn Thu Trang",
                    "Nhân sự · Tuyển dụng", "25–99 nhân sự", "TP. Hồ Chí Minh",
                    "PeopleFirst đồng hành cùng doanh nghiệp xây dựng đội ngũ, trải nghiệm nhân viên và văn hóa làm việc bền vững.",
                    "peoplefirst.svg", "JOB-009", "JOB-010", "JOB-011"),
            employer("aurora.media@recruitflow.demo", "Aurora Media", "Lê Minh Anh",
                    "Marketing · Truyền thông · Thiết kế", "25–99 nhân sự", "TP. Hồ Chí Minh · Làm việc linh hoạt",
                    "Aurora Media kết hợp chiến lược thương hiệu, nội dung và thiết kế để tạo ra các chiến dịch truyền thông có chiều sâu.",
                    "aurora-media.svg", "JOB-012", "JOB-013", "JOB-014", "JOB-021", "JOB-022", "JOB-023"),
            employer("nextcommerce@recruitflow.demo", "NextCommerce", "Trần Hoàng Nam",
                    "Thương mại điện tử · Kinh doanh", "100–500 nhân sự", "Hà Nội · TP. Hồ Chí Minh",
                    "NextCommerce phát triển các giải pháp thương mại số và trao quyền cho đội ngũ kinh doanh tăng trưởng dựa trên dữ liệu.",
                    "nextcommerce.svg", "JOB-015", "JOB-016", "JOB-017"),
            employer("remoteworks@recruitflow.demo", "RemoteWorks Asia", "Phạm Khánh Linh",
                    "Dịch vụ khách hàng · SaaS", "25–99 nhân sự", "Làm việc từ xa",
                    "RemoteWorks Asia vận hành đội ngũ dịch vụ khách hàng phân tán với quy trình rõ ràng và văn hóa làm việc linh hoạt.",
                    "remoteworks.svg", "JOB-018", "JOB-019", "JOB-020"),
            employer("projectlink@recruitflow.demo", "ProjectLink Engineering", "Đỗ Quang Huy",
                    "Xây dựng · Kỹ thuật", "100–500 nhân sự", "Hà Nội · Đà Nẵng",
                    "ProjectLink Engineering triển khai các dự án xây dựng với trọng tâm an toàn, chất lượng và quản trị tiến độ minh bạch.",
                    "projectlink.svg", "JOB-024", "JOB-025", "JOB-026"),
            employer("novatech@recruitflow.demo", "NovaTech Solutions", "Vũ Đức Long",
                    "IT · Phần mềm · Dữ liệu", "100–500 nhân sự", "TP. Hồ Chí Minh · Làm việc từ xa",
                    "NovaTech Solutions xây dựng sản phẩm phần mềm, nền tảng dữ liệu và hạ tầng cloud cho doanh nghiệp trong khu vực.",
                    "novatech.svg", "JOB-027", "JOB-028", "JOB-029", "JOB-030", "JOB-031", "JOB-032", "JOB-033"),
            employer("futureskills@recruitflow.demo", "FutureSkills Academy", "Bùi Ngọc Mai",
                    "Giáo dục · Vận hành", "25–99 nhân sự", "Đà Nẵng · Làm việc linh hoạt",
                    "FutureSkills Academy phát triển chương trình kỹ năng nghề nghiệp và vận hành trải nghiệm học tập lấy người học làm trung tâm.",
                    "futureskills.svg", "JOB-034", "JOB-035", "JOB-036")
    );

    private DemoEmployerDataSeeder() {
    }

    static int seed(Connection connection) throws SQLException {
        Integer hrRoleId = findId(connection, "SELECT id FROM roles WHERE role_name = 'HR'");
        if (hrRoleId == null) return 0;

        boolean originalAutoCommit = connection.getAutoCommit();
        if (originalAutoCommit) connection.setAutoCommit(false);
        try {
            int insertedProfiles = 0;
            for (SeedEmployer employer : EMPLOYERS) {
                upsertUser(connection, employer, hrRoleId);
                Integer ownerId = findId(connection, "SELECT id FROM users WHERE email = ?", employer.email());
                if (ownerId == null) throw new SQLException("Could not create demo employer: " + employer.email());
                insertedProfiles += insertProfile(connection, ownerId, employer);
                assignJobs(connection, ownerId, employer.jobCodes());
            }
            if (originalAutoCommit) connection.commit();
            return insertedProfiles;
        } catch (SQLException exception) {
            if (originalAutoCommit) connection.rollback();
            throw exception;
        } finally {
            if (originalAutoCommit) connection.setAutoCommit(true);
        }
    }

    private static void upsertUser(Connection connection, SeedEmployer employer, int hrRoleId) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, full_name, role_id, status) VALUES (?, ?, ?, ?, 'ACTIVE') "
                + "ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), role_id = VALUES(role_id), status = 'ACTIVE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employer.email());
            statement.setString(2, DEMO_PASSWORD_HASH);
            statement.setString(3, employer.ownerName());
            statement.setInt(4, hrRoleId);
            statement.executeUpdate();
        }
    }

    private static int insertProfile(Connection connection, int ownerId, SeedEmployer employer) throws SQLException {
        String sql = "INSERT INTO recruiter_profiles (user_id, organization_name, job_title, work_phone, industry, "
                + "company_size, address, description, logo_path, is_verified) VALUES (?, ?, 'Talent Acquisition', "
                + "'090 000 0000', ?, ?, ?, ?, ?, TRUE) ON DUPLICATE KEY UPDATE user_id = VALUES(user_id)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setString(2, employer.companyName());
            statement.setString(3, employer.industry());
            statement.setString(4, employer.size());
            statement.setString(5, employer.location());
            statement.setString(6, employer.description());
            statement.setString(7, employer.logoPath());
            return statement.executeUpdate();
        }
    }

    private static void assignJobs(Connection connection, int ownerId, List<String> jobCodes) throws SQLException {
        if (jobCodes.isEmpty()) return;
        String placeholders = String.join(",", java.util.Collections.nCopies(jobCodes.size(), "?"));
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE jobs SET created_by = ? WHERE job_code IN (" + placeholders + ")")) {
            statement.setInt(1, ownerId);
            for (int index = 0; index < jobCodes.size(); index++) {
                statement.setString(index + 2, jobCodes.get(index));
            }
            statement.executeUpdate();
        }
    }

    private static Integer findId(Connection connection, String sql, String... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) statement.setString(index + 1, values[index]);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : null;
            }
        }
    }

    private static SeedEmployer employer(String email, String companyName, String ownerName, String industry,
                                          String size, String location, String description, String logoPath,
                                          String... jobCodes) {
        return new SeedEmployer(email, companyName, ownerName, industry, size, location, description,
                logoPath, List.of(jobCodes));
    }

    private record SeedEmployer(String email, String companyName, String ownerName, String industry,
                                String size, String location, String description, String logoPath,
                                List<String> jobCodes) {
    }
}
