# RecruitFlow

RecruitFlow là đồ án Java Web quản lý tuyển dụng và onboarding. Luồng cốt lõi là: HR đăng tin → Candidate ứng tuyển → HR sàng lọc → phỏng vấn → feedback → offer → Candidate nhận offer → onboarding.

Kiến trúc bắt buộc của ứng dụng:

~~~text
Browser → Filter → Servlet Controller → Service → DAO → JDBC → MySQL
MySQL   → DAO → Service → Servlet → JSP/JSTL → Browser
~~~

## Stack

| Nhóm | Công nghệ |
| --- | --- |
| Backend | Java 17, Servlet 4 (javax.servlet), JSP, JSTL |
| Data | JDBC, MySQL Connector/J 8, MySQL 8 |
| Build/server | Maven WAR, Apache Tomcat 9 |
| Frontend | HTML5, CSS3, JavaScript, Bootstrap 5 |
| Bổ trợ | BCrypt, PDFBox, Apache POI |

> Dùng Tomcat 9.x: source dùng javax.servlet. Tomcat 10+ yêu cầu migrate sang jakarta.servlet.

## Tài liệu

- [Kiến trúc và cách mở rộng source](docs/ARCHITECTURE.md)
- [Workflow, status và business rules](docs/WORKFLOW.md)
- [Cài đặt, deploy và test manual](docs/SETUP.md)

## Cấu trúc project

~~~text
src/main/java/vn/edu/eaut/recruitflow/
├── controller/       Servlet theo actor
├── service/          Nghiệp vụ và transaction
├── dao/              JDBC / PreparedStatement
├── model/            Domain models và DTO hiển thị
├── enums/            Role, status, transitions
├── filter/           Encoding, CSRF, authentication, authorization
└── util/             DB, BCrypt, upload, parser, flash

src/main/webapp/
├── assets/           CSS và JavaScript
└── WEB-INF/views/    JSP auth/public/candidate/hr/interviewer/admin/common
~~~

## Chạy nhanh

1. Cài Java 17, Maven 3.8+, MySQL 8+ và Tomcat 9.
2. Chạy [schema.sql](schema.sql) để tạo database recruitflow và seed.
3. Cấu hình database bằng biến môi trường/JVM properties; xem [SETUP.md](docs/SETUP.md).
4. Build:

   ~~~powershell
   mvn clean package
   ~~~

5. Deploy target/recruitflow-1.0-SNAPSHOT.war lên Tomcat. Đổi tên thành recruitflow.war nếu muốn URL context là /recruitflow.
6. Truy cập http://localhost:8080/recruitflow/home.

## Tài khoản demo

Mật khẩu tất cả tài khoản là 123456; schema lưu BCrypt hash, không lưu plaintext.

| Role | Email |
| --- | --- |
| ADMIN | admin@recruitflow.com |
| HR | hr@recruitflow.com |
| INTERVIEWER | interviewer@recruitflow.com |
| CANDIDATE | candidate@recruitflow.com |

## AI CV Coach

Ở **Candidate → CV của tôi**, ứng viên có thể chọn CV để nhận điểm sẵn sàng, phần còn thiếu, từ khóa và mẫu viết lại phần giới thiệu. Chức năng mặc định chạy phân tích cục bộ nên không cần API key và không tự thay đổi tệp CV gốc.

Khi cấu hình AI bên ngoài, ứng dụng chỉ gửi **nội dung CV đã trích xuất** sau khi ứng viên tích ô đồng ý riêng cho đúng lần đánh giá đó. Hướng dẫn cấu hình và lưu ý bảo mật ở [SETUP.md](docs/SETUP.md#3-ai-cv-coach-tùy-chọn).

## Phân quyền URL

| URL | Role hợp lệ |
| --- | --- |
| /home, /jobs, /jobs/detail, /login, /register | Guest hoặc user đã login |
| /candidate/* | CANDIDATE |
| /hr/* | HR, ADMIN |
| /interviewer/* | INTERVIEWER, ADMIN |
| /admin/* | ADMIN |

AuthenticationFilter redirect user chưa login về /login. AuthorizationFilter trả 403 cho role sai; CsrfFilter bảo vệ các request thay đổi dữ liệu. Phân quyền không dựa vào menu frontend.

## Quy ước khi phát triển tiếp

- Không đặt SQL/JDBC trong Servlet hoặc JSP.
- Dùng PreparedStatement cho mọi SQL.
- Đổi application status phải qua ApplicationService để kiểm tra transition và insert application_status_history.
- Use case nhiều bảng chạy trong transaction, lỗi phải rollback.
- POST phải redirect về GET với flash message (PRG).
- Job đã có application chỉ được ARCHIVED, không hard delete.
