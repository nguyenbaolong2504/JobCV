# Cài đặt, chạy và kiểm thử

## Yêu cầu

| Phần mềm | Version |
| --- | --- |
| JDK | 17 |
| Maven | 3.8+ |
| MySQL | 8.x |
| Tomcat | 9.x |

~~~powershell
java -version
mvn -version
~~~

Java phải là 17. Không dùng Tomcat 10+ khi project vẫn dùng javax.servlet.

## 0. Chạy local bằng `npm run dev`

Sau khi đã cài Node.js **18.18+**, Java 17, Maven, MySQL 8 và Tomcat 9, chạy một lệnh tại thư mục project:

~~~powershell
npm run dev
~~~

Runner thực hiện theo thứ tự an toàn:

1. Tìm Tomcat 9, đọc port từ `conf/server.xml` và kiểm tra Maven. Runner tạo `.recruitflow/tomcat-base` riêng (trừ khi bạn đặt `tomcatBase`) để chỉ auto-deploy RecruitFlow, không khởi động lại toàn bộ WAR backup trong thư mục Tomcat cài sẵn.
2. Yêu cầu mật khẩu MySQL bằng prompt không hiển thị (hoặc đọc `RECRUITFLOW_DB_PASSWORD` trong terminal hiện tại), rồi chỉ truyền secret vào child process `mysql`/Tomcat đang chạy. Runner **không ghi password vào file**.
3. Nếu DB local dùng Windows service `MySQL80` đang dừng, runner khởi động service đó. Có thể đặt tên khác qua `RECRUITFLOW_MYSQL_SERVICE` hoặc tắt tự khởi động bằng `RECRUITFLOW_MYSQL_AUTOSTART=false`.
4. Kiểm tra đúng database theo `RECRUITFLOW_DB_URL`, chạy các migration idempotent đã được đánh dấu/review (auth OTP, session revocation, category, RBAC), build WAR, deploy `recruitflow.war`, sau đó chạy Tomcat.

Với máy hiện có một Tomcat 9, không cần file cấu hình. Nếu runner không tìm được hoặc có nhiều Tomcat, copy file mẫu và chỉ điền **cấu hình không bí mật**:

~~~powershell
Copy-Item dev.config.example.json dev.config.json
~~~

`dev.config.json` đã bị Git ignore; tuy vậy vẫn không được đặt password, SMTP password, OAuth secret hoặc token vào file. Các key hỗ trợ là `tomcatHome`, `tomcatBase` (tùy chọn), `databaseUrl`, `databaseUser`, `mysqlService`, `mysqlBin`, `httpPort`, `contextPath`, `startupTimeoutSeconds` (15–600, mặc định 90). Biến môi trường luôn ưu tiên file cấu hình: `RECRUITFLOW_TOMCAT_HOME`, `RECRUITFLOW_TOMCAT_BASE`, `RECRUITFLOW_DB_URL`, `RECRUITFLOW_DB_USER`, `RECRUITFLOW_MYSQL_SERVICE`, `RECRUITFLOW_MYSQL_BIN`, `RECRUITFLOW_HTTP_PORT`, `RECRUITFLOW_CONTEXT_PATH`, `RECRUITFLOW_STARTUP_TIMEOUT_SECONDS`.

Lệnh hữu ích:

~~~powershell
npm run dev:check     # chỉ kiểm tra tool/Tomcat, không chạm DB hay server
npm run dev:migrate   # chỉ chạy migration idempotent vào database đang cấu hình
npm test              # chạy JUnit regression test
npm run dev -- --no-migrate  # build/deploy/start nhưng không chạy migration
npm run dev -- --skip-build  # deploy WAR target hiện có rồi start Tomcat
~~~

`npm run dev` không chạy `schema.sql` tự động vì file đó seed/reset dữ liệu demo. Với database mới, hãy khởi tạo có chủ đích bằng phần 1 bên dưới; với `jobcvdb` hiện có, runner áp dụng migration vào chính schema từ JDBC URL, không còn hard-code `USE jobcvdb`. Không dùng `--no-migrate` nếu schema hiện tại chưa có các migration mà bản WAR cần.

### Chính sách migration an toàn

Runner không phải trình thực thi SQL tổng quát: nó chỉ quét các file SQL trong `db/migrations` có marker an toàn/được duyệt, từ chối file không được đánh dấu và chặn các thao tác phá dữ liệu như `DROP TABLE`, `TRUNCATE`, `DELETE FROM` hoặc `REPLACE INTO`. Vì `npm run dev` tác động vào chính database từ JDBC URL, vẫn phải sao lưu database trước khi chạy trên môi trường có dữ liệu quan trọng.

- Migration danh mục tạo bảng/cột/index/FK nếu chưa có, thêm taxonomy mẫu bằng `INSERT IGNORE`; chỉ gán category cho các job demo `JOB-001` đến `JOB-005` khi chúng chưa có category.
- Migration RBAC tạo metadata quyền và cấp ma trận quyền mặc định **chỉ khi** `role_permissions` còn trống. Các chỉnh sửa sau đó của Admin tại `/admin/permissions` không bị ghi đè khi chạy lại runner.
- `schema.sql` dành cho database mới; không chạy lại lên database có dữ liệu. Nếu cần thay đổi dữ liệu nghiệp vụ hiện hữu, tạo migration được review riêng và backup/kiểm thử trên bản sao trước.

## 1. Tạo database

Chạy từ thư mục project:

~~~powershell
mysql -u root -p < schema.sql
~~~

Hoặc mở schema.sql trong MySQL Workbench và Run All. Script tạo database `recruitflow`, 20 bảng, role/department/user/job/job skill mẫu và các bảng xác minh tài khoản.

## 2. Cấu hình database

Không sửa DBUtil.java để hard-code secret.

### Environment variables (PowerShell)

~~~powershell
$env:RECRUITFLOW_DB_URL = "jdbc:mysql://localhost:3306/recruitflow?useSSL=false&serverTimezone=Asia/Bangkok&allowPublicKeyRetrieval=true&characterEncoding=utf8"
$env:RECRUITFLOW_DB_USER = "root"
$env:RECRUITFLOW_DB_PASSWORD = "your_mysql_password"
~~~

Chạy Maven/Tomcat từ cùng cửa sổ để process nhận được các biến trên.

### JVM properties

Thêm vào CATALINA_OPTS hoặc cấu hình server trong IDE:

~~~text
-Drecruitflow.db.url=jdbc:mysql://localhost:3306/recruitflow?useSSL=false&serverTimezone=Asia/Bangkok&allowPublicKeyRetrieval=true&characterEncoding=utf8
-Drecruitflow.db.user=root
-Drecruitflow.db.password=your_mysql_password
~~~

DBUtil ưu tiên JVM property → environment variable → default local (`root`, `localhost:3306/recruitflow`). Không có password mặc định trong source. Nếu local MySQL không dùng password thì có thể bỏ biến password; nếu có password, bắt buộc cấu hình bằng một trong hai cách trên.

### Dùng database hiện có tên `jobcvdb`

Nếu project của bạn đã có dữ liệu trong `jobcvdb`, **không chạy lại toàn bộ `schema.sql`** vì script đó tạo database `recruitflow` mới. Cấu hình Tomcat/IDE trỏ rõ sang database đang có dữ liệu:

~~~text
-Drecruitflow.db.url=jdbc:mysql://localhost:3306/jobcvdb?useSSL=false&serverTimezone=Asia/Bangkok&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
-Drecruitflow.db.user=root
-Drecruitflow.db.password=<mat_khau_MySQL_thuc_te>
~~~

Khi chạy Tomcat từ IDE, đặt ba dòng trên vào **VM options** của cấu hình Tomcat (không phải ô Program arguments), sau đó stop server, build/deploy lại WAR và start server. Nếu dùng `startup.bat`, đặt các biến `RECRUITFLOW_DB_*` trong đúng terminal trước khi chạy script để process Tomcat kế thừa chúng. Không đưa mật khẩu MySQL vào source, Git hay ảnh chụp màn hình.

Sau khi restart, kiểm tra account và role trong Workbench:

~~~sql
USE jobcvdb;

SELECT u.id, u.email, u.status, r.role_name,
       CHAR_LENGTH(u.password_hash) AS hash_len
FROM users u
LEFT JOIN roles r ON r.id = u.role_id
WHERE LOWER(TRIM(u.email)) = LOWER('nguyenbaolongg2504@gmail.com');
~~~

Kết quả hợp lệ cho login là đúng một dòng, `status = 'ACTIVE'`, có role hợp lệ và `hash_len = 60`. Nếu server log còn báo SQL error, kiểm tra URL/user/password MySQL và sự tồn tại của hai bảng `users`, `roles`; màn hình đăng nhập cố ý không hiện chi tiết SQL để không làm lộ hạ tầng.

Database `jobcvdb` cũ có thể chưa có các bảng dùng cho đăng ký Nhà tuyển dụng, quên mật khẩu OTP, login OTP, Google OAuth, session revocation, danh mục nghề nghiệp và RBAC. `npm run dev` tự chạy các migration idempotent đã được review vào database trên JDBC URL. Nếu chạy bằng Workbench, hãy chọn đúng schema trước (ví dụ `USE jobcvdb;`) rồi chạy lần lượt các file tại [db/migrations](../db/migrations/). Không chạy `schema.sql` lên database đang có dữ liệu.

## 3. OTP Gmail và xác minh đăng nhập (tùy chọn)

Mặc định, ứng dụng vẫn chạy với login email/mật khẩu để tài khoản demo local hoạt động. Không có mật khẩu Gmail hoặc secret OAuth nào trong source. Để bật OTP, dùng **Gmail App Password** (không phải password Gmail thường) và đặt biến môi trường trước khi khởi động Tomcat:

~~~powershell
$env:RECRUITFLOW_MAIL_ENABLED = "true"
$env:RECRUITFLOW_MAIL_HOST = "smtp.gmail.com"
$env:RECRUITFLOW_MAIL_PORT = "587"
$env:RECRUITFLOW_MAIL_USERNAME = "your-gmail-address@gmail.com"
$env:RECRUITFLOW_MAIL_PASSWORD = "your_16_character_google_app_password"
$env:RECRUITFLOW_MAIL_FROM = "your-gmail-address@gmail.com"
~~~

Tương đương JVM properties:

~~~text
-Drecruitflow.mail.enabled=true
-Drecruitflow.mail.host=smtp.gmail.com
-Drecruitflow.mail.port=587
-Drecruitflow.mail.username=your-gmail-address@gmail.com
-Drecruitflow.mail.password=your_16_character_google_app_password
-Drecruitflow.mail.from=your-gmail-address@gmail.com
~~~

- Khi SMTP hợp lệ, `/forgot-password` gửi OTP 6 số. Hệ thống chỉ trả phản hồi chung cho email không tồn tại, lưu **BCrypt hash** của OTP, OTP hết hạn sau 10 phút, tối đa 5 lần nhập và gửi lại tối đa một lần/phút.
- Muốn bắt buộc OTP như bước thứ hai sau khi password login, đặt thêm `RECRUITFLOW_AUTH_OTP_REQUIRED=true` hoặc `-Drecruitflow.authOtp.required=true`. Khi bật cờ này mà SMTP chưa đủ cấu hình, login password sẽ báo lỗi cấu hình thay vì âm thầm bỏ qua xác minh.
- Giữ các biến này ở environment secret manager hoặc `CATALINA_OPTS`; không commit chúng vào `.properties`, source hoặc tài liệu thực tế của dự án.
- `web.xml` đặt cookie session `HttpOnly`, Tomcat context đặt `SameSite=Lax`; production phải chạy HTTPS và cấu hình Tomcat/reverse proxy để phát cookie `Secure` (không ép `Secure` trong source để local `http://localhost` vẫn chạy). Khi reset password, `session_version` tăng để tất cả session browser cũ bị từ chối ở request kế tiếp. Login password có giới hạn 5 lỗi/email-IP và 20 lỗi/IP trong 15 phút.

## 4. Google OAuth cho Người tìm việc (tùy chọn)

Tạo OAuth 2.0 **Web application** trong Google Cloud Console và khai báo chính xác callback, ví dụ local: `http://localhost:8080/recruitflow/oauth/google/callback`. Cấu hình:

~~~powershell
$env:RECRUITFLOW_GOOGLE_OAUTH_ENABLED = "true"
$env:RECRUITFLOW_GOOGLE_OAUTH_CLIENT_ID = "your_google_client_id"
$env:RECRUITFLOW_GOOGLE_OAUTH_CLIENT_SECRET = "your_google_client_secret"
$env:RECRUITFLOW_GOOGLE_OAUTH_REDIRECT_URI = "http://localhost:8080/recruitflow/oauth/google/callback"
~~~

JVM properties tương ứng là `recruitflow.googleOAuth.enabled`, `.clientId`, `.clientSecret`, `.redirectUri`. Callback production phải dùng HTTPS; `http://localhost` chỉ được chấp nhận cho development. Google sign-in kiểm tra state, authorization code và ID token (issuer, audience, email_verified); chỉ tạo/liên kết `CANDIDATE`, không liên kết hay nâng quyền tài khoản HR/Admin. Hệ thống chỉ lưu stable provider subject, không lưu access token/refresh token/ID token.

## 5. AI CV Coach (tùy chọn)

Trang **Candidate → CV của tôi** có AI CV Coach. Mặc định chức năng này dùng phân tích cục bộ, không cần API key và không gửi nội dung CV ra ngoài.

Muốn dùng nhà cung cấp AI hỗ trợ OpenAI **Responses API**, cấu hình đủ ba biến sau trước khi khởi động Tomcat:

~~~powershell
$env:RECRUITFLOW_AI_BASE_URL = "https://api.openai.com/v1"
$env:RECRUITFLOW_AI_API_KEY = "your_provider_api_key"
$env:RECRUITFLOW_AI_MODEL = "your_configured_model"
~~~

Có thể dùng JVM properties tương ứng:

~~~text
-Drecruitflow.ai.baseUrl=https://api.openai.com/v1
-Drecruitflow.ai.apiKey=your_provider_api_key
-Drecruitflow.ai.model=your_configured_model
~~~

`RECRUITFLOW_AI_BASE_URL` có thể là base URL hoặc URL kết thúc bằng `/responses`; ứng dụng tự thêm `/responses` nếu cần. Không có model mặc định và không có key được hard-code trong source. Khi thiếu một trong ba cấu hình, hệ thống tự chuyển về phân tích cục bộ.

Với OpenAI, base URL là `https://api.openai.com/v1`; chọn model mà tài khoản OpenAI của bạn đang được phép dùng. Tham khảo [Responses API chính thức](https://platform.openai.com/docs/api-reference/responses) khi tạo API key hoặc chọn model.

Ngay cả khi đã cấu hình, CV chỉ được gửi đến nhà cung cấp bên ngoài khi Candidate tích riêng ô đồng ý gửi nội dung CV cho **lần đánh giá đó**. Nếu AI ngoài lỗi hoặc hết thời gian chờ, ứng dụng trả kết quả phân tích cục bộ thay vì làm hỏng luồng CV. Có thể chỉnh timeout (5–60 giây) bằng `RECRUITFLOW_AI_TIMEOUT_SECONDS` hoặc `-Drecruitflow.ai.timeoutSeconds=25`.

## 6. Build và deploy

~~~powershell
mvn clean package
~~~

WAR đầu ra là target/recruitflow-1.0-SNAPSHOT.war.

1. Tắt Tomcat.
2. Copy WAR vào <TOMCAT_HOME>/webapps.
3. Đổi tên thành recruitflow.war để có context path /recruitflow.
4. Khởi động Tomcat bằng bin/startup.bat hoặc IDE.
5. Mở http://localhost:8080/recruitflow/home.

Nếu giữ tên gốc, context thường là /recruitflow-1.0-SNAPSHOT. Khi deploy lỗi, xem <TOMCAT_HOME>/logs/catalina.*.log.

## 7. Tài khoản demo

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@recruitflow.com | 123456 |
| HR | hr@recruitflow.com | 123456 |
| Interviewer | interviewer@recruitflow.com | 123456 |
| Candidate | candidate@recruitflow.com | 123456 |

Chỉ dùng dev/demo. Đổi password và secret ở môi trường thật.

## 8. Entry route

| Role | URL |
| --- | --- |
| Guest | /home, /jobs |
| Candidate | /home (Bảng điều khiển tại /candidate/dashboard) |
| HR | /hr/dashboard |
| Interviewer | /interviewer/dashboard |
| Admin | /admin/dashboard |

Chưa login vào private route phải về /login. Sai role phải 403.

## 9. Danh mục nghề nghiệp và phân quyền nâng cao

### Danh mục hiển thị ở cổng tìm việc

Admin quản lý taxonomy tại `/admin/job-categories`. Đây là nguồn dữ liệu cho menu **Việc làm** ở `/home`, bộ lọc `categoryId` của `/jobs` và lựa chọn danh mục khi HR tạo/sửa tin.

1. Tạo **danh mục chính** (ví dụ: `Công nghệ thông tin`).
2. Tạo **danh mục con** trực tiếp dưới danh mục chính (ví dụ: `Phát triển phần mềm`). Hệ thống chủ đích chỉ hỗ trợ hai cấp để menu và truy vấn lọc luôn rõ ràng.
3. HR gán tin vào danh mục con; chỉ khi danh mục chính chưa có danh mục con đang hiển thị thì mới được gán tin trực tiếp vào danh mục chính.
4. Chỉ danh mục và danh mục cha đang hiển thị mới được công khai. Lọc theo danh mục chính bao gồm cả các tin ở danh mục con của nó.

Không thể xóa danh mục có danh mục con hoặc có tin tuyển dụng. Không thể ẩn danh mục đang được gán cho tin, hay ẩn danh mục chính khi còn danh mục con đang hiển thị. Các thao tác tạo/sửa/xóa đều được audit.

### Ma trận quyền theo vai trò

Admin mở `/admin/permissions` để cấp/bỏ quyền theo **vai trò** `ADMIN`, `HR`, `INTERVIEWER` và `CANDIDATE`. Quyền được kiểm tra trên server ở mỗi request; bỏ một quyền thì URL/hành động tương ứng trả HTTP 403 ngay cả khi user vẫn nhìn thấy một link cũ.

- Đây là RBAC theo vai trò, không phải cấp quyền riêng từng tài khoản.
- Ranh giới actor vẫn được giữ cố định: HR chỉ nhận quyền `HR_*`, Interviewer chỉ nhận `INTERVIEWER_*`, Candidate chỉ nhận `CANDIDATE_*`; không thể dùng trang này để biến Candidate thành HR hoặc cấp quyền chéo không có ý nghĩa.
- Admin có thể giữ toàn bộ quyền; ba quyền `ADMIN_DASHBOARD_VIEW`, `ADMIN_USERS_MANAGE`, `ADMIN_PERMISSIONS_MANAGE` là bắt buộc để không tự khóa khu vực quản trị.
- Lưu quyền chạy trong transaction và ghi audit log. Kiểm tra lại bằng một browser/session của đúng role sau mỗi thay đổi; khôi phục quyền từ Admin nếu user bị 403 ngoài dự kiến.

### Kiểm thử xác thực và tài khoản

1. Đăng nhập `candidate@recruitflow.com`; expected redirect là `/home`, sau đó mở **Bảng điều khiển** từ dropdown để vào `/candidate/dashboard`.
2. Đăng ký **Người tìm việc** với password hợp lệ (từ 8 ký tự, có chữ và số); expected là user `CANDIDATE/ACTIVE` cùng `candidate_profiles` mới.
3. Đăng ký **Nhà tuyển dụng**; bắt buộc có tổ chức, chức danh, số điện thoại. Expected là `HR/INACTIVE` và record `recruiter_profiles`; Admin thấy thông tin tổ chức tại `/admin/users`, bấm **Kích hoạt**, rồi tài khoản HR mới login được.
4. Nếu SMTP được bật, yêu cầu quên mật khẩu, nhập OTP đúng, đặt password mới, đăng nhập bằng password mới. Test OTP sai 5 lần, OTP quá 10 phút và resend trước 60 giây đều phải bị từ chối.
5. Nếu `RECRUITFLOW_AUTH_OTP_REQUIRED=true`, login password phải chuyển `/login/verify-otp`; chỉ login sau OTP email đúng. Lock user giữa hai bước thì OTP không được chấp nhận.
6. Nếu Google OAuth được bật, thử callback state sai (phải bị từ chối), Google email đã verify tạo Candidate mới, Google email trùng Candidate thì link an toàn; Google email trùng HR/Admin phải bị từ chối.

## Kiểm thử thủ công luồng chính

Dùng database sạch hoặc ghi lại dữ liệu trước khi test. Dùng browser profile/ẩn danh khác nhau cho mỗi role.

### A. HR tạo job

1. Login HR, mở /hr/jobs.
2. Tạo job có title, department, positions > 0, salary hợp lệ, deadline hôm nay/sau đó, skills.
3. Xác nhận DRAFT, sau đó Publish.
4. Guest mở /jobs và chỉ thấy job PUBLISHED.

### B. Candidate upload CV và apply

1. Login Candidate, upload PDF/DOC/DOCX ≤ 5 MB tại /candidate/resumes.
2. Chọn/đặt CV default.
3. Apply job PUBLISHED còn hạn ở /candidate/jobs hoặc /jobs/detail?id=...
4. Kiểm tra /candidate/applications có SUBMITTED, match score, timeline.
5. Apply lần hai vào cùng job phải bị từ chối.

Kiểm tra application, application_status_history SUBMITTED và notification được tạo; lỗi không để lại record dở dang.

### C. Screening và interview

1. HR chuyển SUBMITTED → SCREENING → SHORTLISTED tại /hr/applications.
2. Thử nhảy SUBMITTED → OFFERED; phải bị từ chối.
3. Tạo interview cho shortlisted candidate, gán interviewer, start < end.
4. Xác nhận application INTERVIEW_SCHEDULED và notifications.
5. Tạo lịch đè interviewer; phải báo conflict.

### D. Feedback

1. Interviewer mở /interviewer/interviews.
2. Chọn lịch được gán, nhập bốn score, comment, recommendation.
3. Submit.
4. Kiểm tra overall score, interview COMPLETED, application INTERVIEWED, timeline mới.
5. Interviewer khác không được feedback lịch không thuộc mình.

### E. Offer và onboarding

1. HR tạo/send offer cho application INTERVIEWED.
2. Xác nhận offer SENT, application OFFERED, Candidate có notification.
3. Candidate accept offer trước expiry date.
4. Kiểm tra ACCEPTED, HIRED, history và onboarding.
5. Candidate hoàn tất tất cả required task tại /candidate/onboarding.
6. Kiểm tra progress 100% và onboarding COMPLETED.

### F. Admin

1. Login Admin.
2. Kiểm tra /admin/dashboard, /admin/users, /admin/departments, /admin/audit-logs.
3. Lock/unlock user test.
4. Thêm/sửa department, kiểm tra dữ liệu liên quan vẫn hợp lệ.
5. Mở `/admin/job-categories`, tạo một danh mục chính và một danh mục con; kiểm tra menu `/home` và `/jobs?categoryId=<id-danh-muc-chinh>` chỉ hiện taxonomy/tin hợp lệ. Thử xóa hoặc ẩn danh mục đang dùng; hệ thống phải từ chối.
6. Mở `/admin/permissions`, bỏ tạm một quyền không cốt lõi của HR (ví dụ `HR_REPORTS_VIEW`), đăng nhập HR và mở URL tương ứng; expected HTTP 403. Cấp lại quyền và xác nhận truy cập hoạt động. Không bỏ ba quyền ADMIN cốt lõi.

## Checklist sau thay đổi

1. Chạy mvn clean package.
2. Kiểm tra @WebServlet mapping, JSP action/link và context path.
3. JSP phải ở WEB-INF/views; không query database trong JSP.
4. SQL mới dùng PreparedStatement và schema có FK/index/unique cần thiết.
5. Kiểm tra role ở filter và ownership/actor trong service.
6. Use case nhiều bảng phải transaction/rollback.
7. POST → redirect → GET + flash.
8. Test lại luồng impacted để không phá module cũ.

## Lỗi hay gặp

| Hiện tượng | Kiểm tra |
| --- | --- |
| Communications link failure | MySQL service/host/port/JDBC URL. |
| Access denied for user | RECRUITFLOW_DB_USER/PASSWORD hoặc JVM property. |
| ClassNotFoundException javax.servlet | Đổi sang Tomcat 9. |
| HTTP 404 | Context path và @WebServlet mapping. |
| HTTP 403 | Session role và URL prefix. |
| HTTP 500 khi upload | Folder upload writable, extension/MIME/size. |
| JSP không có dữ liệu | Controller setAttribute và tên EL/view path. |
