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
| Backend | Java 25, Servlet 4 (javax.servlet), JSP, JSTL |
| Data | JDBC, MySQL Connector/J 8, MySQL 8 |
| Build/server | Maven WAR, Apache Tomcat 9 |
| Frontend | HTML5, CSS3, JavaScript, Bootstrap 5 |
| Bổ trợ | BCrypt, JavaMail (OTP tùy chọn), PDFBox, Apache POI |

> Dùng Tomcat 9.x: source dùng javax.servlet. Tomcat 10+ yêu cầu migrate sang jakarta.servlet.

## Tài liệu

- [Kiến trúc và cách mở rộng source](docs/ARCHITECTURE.md)
- [Workflow, status và business rules](docs/WORKFLOW.md)
- [Cổng Người tìm việc, CV mẫu và tìm việc](docs/CANDIDATE_PORTAL_GUIDE.md)
- [Tìm việc và lọc nâng cao](docs/JOB_DISCOVERY_SEARCH.md)
- [Cập nhật các lỗi QA core](docs/QA_CORE_FIXES.md)
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

### Chạy local bằng một lệnh

Sau khi đã có Java 25, Maven, MySQL 8, Tomcat 9 và Node.js 18.18+, chạy:

~~~powershell
npm run dev
~~~

Runner sẽ hỏi mật khẩu MySQL bằng prompt không hiển thị, kiểm tra/khởi động service `MySQL80` nếu cần, áp dụng các migration idempotent đã được review vào **database đang cấu hình**, build WAR và khởi động Tomcat. Mật khẩu không được ghi vào source, `dev.config.json` hay Git. Lần đầu runner tự tạo `.recruitflow/tomcat-base` và chỉ deploy RecruitFlow vào base riêng đó, nên các WAR backup trong Tomcat cài sẵn không làm chậm lần chạy sau. Truy cập URL runner in ra; port lấy từ `conf/server.xml` của Tomcat hoặc từ cấu hình local (ví dụ `http://localhost:8080/recruitflow/home`).

Nếu Tomcat 9 không được nhận diện tự động, copy [dev.config.example.json](dev.config.example.json) thành `dev.config.json` và điền đường dẫn Tomcat/URL DB không chứa secret. Xem chi tiết tại [SETUP.md](docs/SETUP.md#0-chạy-local-bằng-npm-run-dev).

### Chạy thủ công

1. Chạy [schema.sql](schema.sql) để tạo database `recruitflow` và seed cho môi trường mới.
2. Cấu hình database bằng biến môi trường/JVM properties; xem [SETUP.md](docs/SETUP.md).
3. Build:

   ~~~powershell
   mvn clean package
   ~~~

4. Deploy target/recruitflow-1.0-SNAPSHOT.war lên Tomcat. Đổi tên thành recruitflow.war nếu muốn URL context là /recruitflow.
5. Truy cập http://localhost:8080/recruitflow/home.

## Tài khoản demo

Mật khẩu tất cả tài khoản là 123456; schema lưu BCrypt hash, không lưu plaintext.

| Role | Email |
| --- | --- |
| ADMIN | admin@recruitflow.com |
| HR | hr@recruitflow.com |
| INTERVIEWER | interviewer@recruitflow.com |
| CANDIDATE | candidate@recruitflow.com |

## Tài khoản và bảo mật

- **Người tìm việc (Candidate):** đăng ký xong hoạt động ngay. Sau đăng nhập, hệ thống đưa về `/home` để khám phá việc làm; **Bảng điều khiển** luôn có trong menu nếu cần theo dõi hồ sơ.
- **Nhà tuyển dụng (HR):** phải cung cấp tổ chức, chức danh và số điện thoại công việc. Tài khoản tạo ở trạng thái `INACTIVE`; Admin xem thông tin xác minh tại **Quản lý người dùng** rồi bấm **Kích hoạt**. Không có tự nâng quyền HR công khai.
- Mật khẩu mới/reset dùng BCrypt và yêu cầu tối thiểu 8 ký tự gồm chữ cái lẫn chữ số. Có luồng **Quên mật khẩu** dùng OTP Gmail khi SMTP được cấu hình.
- Khi reset mật khẩu thành công, mọi session browser cũ của tài khoản bị vô hiệu ở request tiếp theo; login password bị giới hạn theo email/IP để giảm brute-force.
- Có thể bật OTP email như bước hai sau mật khẩu, và có thể bật Google OAuth cho Candidate. Hai chức năng đều tắt mặc định và không có secret hard-code. Xem [SETUP.md](docs/SETUP.md#3-otp-gmail-và-xác-minh-đăng-nhập-tùy-chọn).

## Danh mục nghề nghiệp và phân quyền nâng cao

- **Danh mục hiển thị ở `/home`:** Admin quản lý tại `/admin/job-categories`. Cấu trúc có đúng hai cấp: danh mục chính (ví dụ *Công nghệ thông tin*) và danh mục con (ví dụ *Phát triển phần mềm*). Chỉ danh mục đang hiển thị mới xuất hiện trong menu; lọc theo danh mục chính sẽ bao gồm các tin nằm trong các danh mục con của nó.
- **Tính toàn vẹn danh mục:** một tin tuyển dụng chỉ gán vào danh mục con, hoặc danh mục chính chưa có danh mục con **đang hiển thị**. Không thể xóa/ngừng hiển thị danh mục đang được dùng hay danh mục chính còn danh mục con đang hiển thị.
- **RBAC theo hành động:** Admin cấu hình ma trận quyền theo vai trò tại `/admin/permissions`; mỗi request trong khu vực riêng tư được kiểm tra ở server và thay đổi áp dụng ngay. Đây là quyền theo **vai trò**, không phải cấp quyền riêng lẻ cho từng user. Ranh giới workspace vẫn cố định: Candidate, Interviewer và HR không thể nhận quyền của actor khác; Admin luôn giữ các quyền quản trị cốt lõi để tránh tự khóa hệ thống.
- Thay đổi danh mục và quyền đều ghi audit log. Chi tiết vận hành, migration và checklist kiểm thử tại [SETUP.md](docs/SETUP.md#9-danh-mục-nghề-nghiệp-và-phân-quyền-nâng-cao).

## AI CV Coach

Ở **Candidate → CV của tôi**, ứng viên có thể chọn CV để nhận điểm sẵn sàng, phần còn thiếu, từ khóa và mẫu viết lại phần giới thiệu. Chức năng mặc định chạy phân tích cục bộ nên không cần API key và không tự thay đổi tệp CV gốc.

Khi cấu hình AI bên ngoài, ứng dụng chỉ gửi **nội dung CV đã trích xuất** sau khi ứng viên tích ô đồng ý riêng cho đúng lần đánh giá đó. Hướng dẫn cấu hình và lưu ý bảo mật ở [SETUP.md](docs/SETUP.md#3-ai-cv-coach-tùy-chọn).

## Phân quyền URL

| URL | Role hợp lệ |
| --- | --- |
| /home, /jobs, /jobs/detail, /login, /register, /forgot-password, /oauth/google | Guest hoặc user đã login |
| /candidate/* | CANDIDATE |
| /hr/* | HR, ADMIN |
| /interviewer/* | INTERVIEWER |
| /admin/* | ADMIN |

AuthenticationFilter redirect user chưa login về /login và đối chiếu lại trạng thái/role từ database trên request có session. AuthorizationFilter trả 403 cho role hoặc quyền hành động sai; CsrfFilter bảo vệ các request thay đổi dữ liệu. Phân quyền không dựa vào menu frontend.

## Quy ước khi phát triển tiếp

- Không đặt SQL/JDBC trong Servlet hoặc JSP.
- Dùng PreparedStatement cho mọi SQL.
- Đổi application status phải qua ApplicationService để kiểm tra transition và insert application_status_history.
- Use case nhiều bảng chạy trong transaction, lỗi phải rollback.
- POST phải redirect về GET với flash message (PRG).
- Job đã có application chỉ được ARCHIVED, không hard delete.
