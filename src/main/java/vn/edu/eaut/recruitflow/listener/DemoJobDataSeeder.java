package vn.edu.eaut.recruitflow.listener;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adds a realistic, idempotent catalog to development databases. */
final class DemoJobDataSeeder {
    private static final Map<String, String> DEPARTMENTS = new LinkedHashMap<>();
    private static final List<SeedJob> JOBS;

    static {
        DEPARTMENTS.put("Information Technology", "IT, software engineering, data and cloud services");
        DEPARTMENTS.put("Human Resources", "HR, recruitment and people operations");
        DEPARTMENTS.put("Marketing", "Marketing, communications and brand development");
        DEPARTMENTS.put("Finance", "Finance, accounting and business control");
        DEPARTMENTS.put("Sales", "Sales, business development and account management");
        DEPARTMENTS.put("Customer Service", "Customer care and customer success");
        DEPARTMENTS.put("Design", "Graphic, product and motion design");
        DEPARTMENTS.put("Construction", "Construction, civil engineering and site management");
        DEPARTMENTS.put("Operations", "Business operations and project coordination");

        JOBS = List.of(
                job("JOB-006", "Kế toán tổng hợp", "Finance", "Hanoi", "FULL_TIME", 2, 12000000, 20000000,
                        "Kiểm soát chứng từ, lập báo cáo tài chính và phối hợp quyết toán định kỳ.",
                        "Tốt nghiệp kế toán hoặc tài chính; thành thạo Excel và phần mềm kế toán.", 2,
                        "Kế toán", "Excel", "Báo cáo tài chính"),
                job("JOB-007", "Chuyên viên phân tích tài chính", "Finance", "Ho Chi Minh City", "FULL_TIME", 2, 18000000, 32000000,
                        "Phân tích ngân sách, dòng tiền và hiệu quả đầu tư cho các đơn vị kinh doanh.",
                        "Có tư duy phân tích, sử dụng tốt Excel hoặc Power BI và hiểu báo cáo tài chính.", 2,
                        "Financial Analysis", "Excel", "Power BI"),
                job("JOB-008", "Nhân viên kế toán bán thời gian", "Finance", "Da Nang", "PART_TIME", 2, 6000000, 9000000,
                        "Hỗ trợ nhập liệu, đối soát hóa đơn và theo dõi công nợ khách hàng.",
                        "Cẩn thận, trung thực và có kiến thức kế toán căn bản.", 0,
                        "Kế toán", "Excel"),
                job("JOB-009", "Chuyên viên hành chính nhân sự", "Human Resources", "Hanoi", "FULL_TIME", 2, 11000000, 18000000,
                        "Quản lý hồ sơ nhân sự, chấm công, phúc lợi và các hoạt động văn phòng.",
                        "Có kỹ năng tổ chức, giao tiếp và nắm được nghiệp vụ hành chính nhân sự.", 1,
                        "Nhân sự", "Hành chính", "Excel"),
                job("JOB-010", "Chuyên viên tuyển dụng", "Human Resources", "Ho Chi Minh City", "FULL_TIME", 3, 12000000, 22000000,
                        "Tìm nguồn, phỏng vấn và đồng hành cùng ứng viên trong toàn bộ quy trình tuyển dụng.",
                        "Giao tiếp tốt, chủ động và có kinh nghiệm tuyển dụng đa kênh.", 1,
                        "Tuyển dụng", "Phỏng vấn", "Sourcing"),
                job("JOB-011", "Thực tập sinh nhân sự", "Human Resources", "Da Nang", "INTERNSHIP", 4, 3000000, 5000000,
                        "Hỗ trợ đăng tin, sàng lọc hồ sơ và tổ chức hoạt động gắn kết nhân viên.",
                        "Sinh viên năm cuối, yêu thích lĩnh vực nhân sự và có tinh thần học hỏi.", 0,
                        "Nhân sự", "Giao tiếp"),
                job("JOB-012", "Digital Marketing Executive", "Marketing", "Ho Chi Minh City", "FULL_TIME", 3, 13000000, 24000000,
                        "Triển khai chiến dịch quảng cáo số, theo dõi chuyển đổi và tối ưu chi phí.",
                        "Biết Google Ads, Meta Ads và đọc hiểu dữ liệu chiến dịch.", 1,
                        "Digital Marketing", "Google Ads", "Meta Ads"),
                job("JOB-013", "Content Marketing Specialist", "Marketing", "Hanoi", "FULL_TIME", 2, 12000000, 21000000,
                        "Xây dựng kế hoạch nội dung đa kênh và phát triển tiếng nói thương hiệu.",
                        "Viết tốt, có tư duy sáng tạo và biết nghiên cứu insight khách hàng.", 1,
                        "Content Marketing", "SEO", "Copywriting"),
                job("JOB-014", "Thực tập sinh Marketing", "Marketing", "Da Nang", "INTERNSHIP", 4, 3500000, 5500000,
                        "Hỗ trợ nội dung mạng xã hội, sự kiện và báo cáo hiệu quả truyền thông.",
                        "Năng động, sáng tạo và sử dụng được công cụ thiết kế cơ bản.", 0,
                        "Marketing", "Social Media", "Canva"),
                job("JOB-015", "Nhân viên kinh doanh", "Sales", "Ho Chi Minh City", "FULL_TIME", 6, 10000000, 30000000,
                        "Tìm kiếm khách hàng, tư vấn giải pháp và phát triển doanh số theo khu vực.",
                        "Giao tiếp thuyết phục, chủ động và chịu được áp lực mục tiêu.", 0,
                        "Kinh doanh", "Bán hàng", "CRM"),
                job("JOB-016", "Chuyên viên kinh doanh B2B", "Sales", "Hanoi", "FULL_TIME", 3, 15000000, 35000000,
                        "Phát triển khách hàng doanh nghiệp và quản lý cơ hội bán hàng dài hạn.",
                        "Có kinh nghiệm B2B, đàm phán hợp đồng và quản lý pipeline.", 2,
                        "B2B Sales", "Đàm phán", "CRM"),
                job("JOB-017", "Sales Admin bán thời gian", "Sales", "Da Nang", "PART_TIME", 2, 6000000, 9000000,
                        "Hỗ trợ báo giá, cập nhật dữ liệu khách hàng và theo dõi đơn hàng.",
                        "Tổ chức tốt, cẩn thận và sử dụng thành thạo công cụ văn phòng.", 0,
                        "Sales Admin", "Excel"),
                job("JOB-018", "Chuyên viên chăm sóc khách hàng", "Customer Service", "Ho Chi Minh City", "FULL_TIME", 5, 9000000, 16000000,
                        "Tiếp nhận yêu cầu, giải quyết vấn đề và nâng cao trải nghiệm khách hàng.",
                        "Giao tiếp tích cực, kiên nhẫn và có khả năng xử lý tình huống.", 0,
                        "Chăm sóc khách hàng", "CRM", "Giao tiếp"),
                job("JOB-019", "Customer Success Executive", "Customer Service", "Remote", "REMOTE", 3, 14000000, 26000000,
                        "Đồng hành cùng khách hàng sử dụng sản phẩm và thúc đẩy tỷ lệ duy trì.",
                        "Có tư duy dịch vụ, phân tích nhu cầu và làm việc từ xa hiệu quả.", 1,
                        "Customer Success", "SaaS", "English"),
                job("JOB-020", "Nhân viên trực tổng đài", "Customer Service", "Hanoi", "PART_TIME", 4, 6000000, 10000000,
                        "Tư vấn thông tin dịch vụ và ghi nhận phản hồi khách hàng theo ca.",
                        "Giọng nói rõ ràng, lịch sự và có thể làm việc theo ca.", 0,
                        "Tổng đài", "Chăm sóc khách hàng"),
                job("JOB-021", "Thiết kế đồ họa", "Design", "Ho Chi Minh City", "FULL_TIME", 2, 12000000, 22000000,
                        "Thiết kế ấn phẩm truyền thông, nhận diện thương hiệu và nội dung số.",
                        "Có portfolio và sử dụng tốt Photoshop, Illustrator hoặc Figma.", 1,
                        "Thiết kế đồ họa", "Photoshop", "Illustrator"),
                job("JOB-022", "UI UX Designer", "Design", "Remote", "REMOTE", 2, 18000000, 32000000,
                        "Nghiên cứu người dùng và thiết kế trải nghiệm cho sản phẩm web, mobile.",
                        "Có portfolio sản phẩm số, thành thạo Figma và quy trình UX.", 2,
                        "UI UX", "Figma", "User Research"),
                job("JOB-023", "Motion Graphic Designer", "Design", "Hanoi", "CONTRACT", 2, 14000000, 26000000,
                        "Sản xuất video motion cho chiến dịch thương hiệu và mạng xã hội.",
                        "Sử dụng tốt After Effects, Premiere và có tư duy kể chuyện hình ảnh.", 1,
                        "Motion Graphic", "After Effects", "Premiere"),
                job("JOB-024", "Kỹ sư xây dựng", "Construction", "Hanoi", "FULL_TIME", 4, 15000000, 28000000,
                        "Triển khai hồ sơ kỹ thuật, giám sát tiến độ và chất lượng thi công.",
                        "Tốt nghiệp xây dựng, đọc tốt bản vẽ và sẵn sàng làm việc tại công trường.", 2,
                        "Xây dựng", "AutoCAD", "Giám sát"),
                job("JOB-025", "Giám sát công trình", "Construction", "Ho Chi Minh City", "CONTRACT", 3, 16000000, 30000000,
                        "Điều phối nhà thầu, nghiệm thu khối lượng và kiểm soát an toàn công trường.",
                        "Có chứng chỉ phù hợp và kinh nghiệm giám sát công trình dân dụng.", 3,
                        "Giám sát công trình", "An toàn lao động"),
                job("JOB-026", "QS Engineer", "Construction", "Da Nang", "FULL_TIME", 2, 14000000, 25000000,
                        "Bóc tách khối lượng, lập dự toán và quản lý hồ sơ thanh quyết toán.",
                        "Thành thạo Excel, phần mềm dự toán và đọc hiểu bản vẽ kỹ thuật.", 2,
                        "QS", "Dự toán", "AutoCAD"),
                job("JOB-027", "DevOps Engineer", "Information Technology", "Remote", "REMOTE", 2, 22000000, 45000000,
                        "Xây dựng pipeline CI/CD và vận hành hạ tầng cloud ổn định, an toàn.",
                        "Có kinh nghiệm Docker, Linux, CI/CD và nền tảng đám mây.", 2,
                        "DevOps", "Docker", "AWS"),
                job("JOB-028", "Mobile Developer", "Information Technology", "Ho Chi Minh City", "FULL_TIME", 3, 18000000, 35000000,
                        "Phát triển ứng dụng mobile hiệu năng cao và phối hợp cùng nhóm sản phẩm.",
                        "Thành thạo Flutter, React Native, Kotlin hoặc Swift.", 2,
                        "Mobile", "Flutter", "REST API"),
                job("JOB-029", "Data Analyst", "Information Technology", "Hanoi", "CONTRACT", 3, 16000000, 30000000,
                        "Khai thác dữ liệu, xây dựng dashboard và cung cấp insight cho kinh doanh.",
                        "Thành thạo SQL, Excel và một công cụ trực quan hóa dữ liệu.", 1,
                        "Data Analysis", "SQL", "Power BI"),
                job("JOB-030", "Product Manager", "Information Technology", "Ho Chi Minh City", "FULL_TIME", 2, 28000000, 50000000,
                        "Định hướng sản phẩm, quản lý roadmap và phối hợp các nhóm liên chức năng.",
                        "Có kinh nghiệm phát triển sản phẩm số và ra quyết định dựa trên dữ liệu.", 3,
                        "Product Management", "Agile", "Analytics"),
                job("JOB-031", "Software Engineer bán thời gian", "Information Technology", "Hanoi", "PART_TIME", 2, 10000000, 18000000,
                        "Phát triển tính năng web và xử lý lỗi theo sprint với lịch làm việc linh hoạt.",
                        "Nắm vững lập trình hướng đối tượng, Git và cơ sở dữ liệu quan hệ.", 1,
                        "Software Engineering", "Git", "SQL"),
                job("JOB-032", "Remote Java Developer", "Information Technology", "Remote", "REMOTE", 3, 22000000, 42000000,
                        "Phát triển dịch vụ Java, REST API và làm việc cùng nhóm sản phẩm phân tán.",
                        "Thành thạo Java, Spring, SQL và giao tiếp từ xa tốt.", 2,
                        "Java", "Spring", "MySQL"),
                job("JOB-033", "Cloud Engineer", "Information Technology", "Da Nang", "FULL_TIME", 2, 20000000, 38000000,
                        "Thiết kế hạ tầng cloud, giám sát hệ thống và tối ưu chi phí vận hành.",
                        "Có kinh nghiệm AWS hoặc Azure, Terraform và Linux.", 2,
                        "Cloud", "Terraform", "Linux"),
                job("JOB-034", "Chuyên viên vận hành tuyển dụng", "Operations", "Hanoi", "FULL_TIME", 2, 13000000, 22000000,
                        "Chuẩn hóa quy trình, dữ liệu và báo cáo vận hành cho hoạt động tuyển dụng.",
                        "Có tư duy quy trình, sử dụng tốt Excel và phối hợp liên phòng ban.", 1,
                        "Operations", "Recruitment", "Excel"),
                job("JOB-035", "Điều phối dự án", "Operations", "Ho Chi Minh City", "CONTRACT", 3, 14000000, 26000000,
                        "Theo dõi kế hoạch, nguồn lực, rủi ro và báo cáo tiến độ dự án.",
                        "Tổ chức tốt, chủ động và hiểu các phương pháp quản lý dự án.", 1,
                        "Project Coordination", "Agile", "Reporting"),
                job("JOB-036", "Thực tập sinh vận hành", "Operations", "Da Nang", "INTERNSHIP", 4, 3500000, 5500000,
                        "Hỗ trợ nhập liệu, xây dựng báo cáo và cải tiến quy trình nội bộ.",
                        "Sinh viên năm cuối, cẩn thận và có khả năng học nhanh.", 0,
                        "Operations", "Excel", "Communication")
        );
    }

    private DemoJobDataSeeder() {
    }

    static int seed(Connection connection) throws SQLException {
        Integer hrUserId = findId(connection, "SELECT id FROM users WHERE email = ?", "hr@recruitflow.com");
        if (hrUserId == null) {
            return 0;
        }

        boolean originalAutoCommit = connection.getAutoCommit();
        if (originalAutoCommit) {
            connection.setAutoCommit(false);
        }
        try {
            ensureDepartments(connection);
            int inserted = insertJobs(connection, hrUserId);
            if (originalAutoCommit) {
                connection.commit();
            }
            return inserted;
        } catch (SQLException exception) {
            if (originalAutoCommit) {
                connection.rollback();
            }
            throw exception;
        } finally {
            if (originalAutoCommit) {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void ensureDepartments(Connection connection) throws SQLException {
        String sql = "INSERT INTO departments (name, description) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE description = VALUES(description)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, String> department : DEPARTMENTS.entrySet()) {
                statement.setString(1, department.getKey());
                statement.setString(2, department.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static int insertJobs(Connection connection, int hrUserId) throws SQLException {
        String jobSql = "INSERT IGNORE INTO jobs (job_code, title, department_id, location, employment_type, "
                + "number_of_positions, salary_min, salary_max, description, requirements, experience_required, "
                + "deadline, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PUBLISHED', ?)";
        String skillSql = "INSERT IGNORE INTO job_skills (job_id, skill_name, weight, is_required) VALUES (?, ?, ?, ?)";
        int inserted = 0;
        Date deadline = Date.valueOf(LocalDate.now().plusMonths(18));
        try (PreparedStatement jobStatement = connection.prepareStatement(jobSql);
             PreparedStatement skillStatement = connection.prepareStatement(skillSql)) {
            for (SeedJob job : JOBS) {
                Integer departmentId = findId(connection, "SELECT id FROM departments WHERE name = ?", job.department());
                if (departmentId == null) {
                    throw new SQLException("Missing department for demo job: " + job.department());
                }
                int index = 1;
                jobStatement.setString(index++, job.code());
                jobStatement.setString(index++, job.title());
                jobStatement.setInt(index++, departmentId);
                jobStatement.setString(index++, job.location());
                jobStatement.setString(index++, job.employmentType());
                jobStatement.setInt(index++, job.positions());
                jobStatement.setBigDecimal(index++, BigDecimal.valueOf(job.salaryMin()));
                jobStatement.setBigDecimal(index++, BigDecimal.valueOf(job.salaryMax()));
                jobStatement.setString(index++, job.description());
                jobStatement.setString(index++, job.requirements());
                jobStatement.setInt(index++, job.experienceYears());
                jobStatement.setDate(index++, deadline);
                jobStatement.setInt(index, hrUserId);
                inserted += jobStatement.executeUpdate();

                Integer jobId = findId(connection, "SELECT id FROM jobs WHERE job_code = ?", job.code());
                if (jobId != null) {
                    int weight = 5;
                    for (String skill : job.skills()) {
                        skillStatement.setInt(1, jobId);
                        skillStatement.setString(2, skill);
                        skillStatement.setInt(3, Math.max(2, weight--));
                        skillStatement.setBoolean(4, weight >= 3);
                        skillStatement.addBatch();
                    }
                }
            }
            skillStatement.executeBatch();
        }
        return inserted;
    }

    private static Integer findId(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : null;
            }
        }
    }

    private static SeedJob job(String code, String title, String department, String location, String employmentType,
                               int positions, long salaryMin, long salaryMax, String description, String requirements,
                               int experienceYears, String... skills) {
        return new SeedJob(code, title, department, location, employmentType, positions, salaryMin, salaryMax,
                description, requirements, experienceYears, skills);
    }

    private record SeedJob(String code, String title, String department, String location, String employmentType,
                           int positions, long salaryMin, long salaryMax, String description, String requirements,
                           int experienceYears, String[] skills) {
    }
}
