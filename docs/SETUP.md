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

## 1. Tạo database

Chạy từ thư mục project:

~~~powershell
mysql -u root -p < schema.sql
~~~

Hoặc mở schema.sql trong MySQL Workbench và Run All. Script tạo database recruitflow, 16 bảng, role/department/user/job/job skill mẫu.

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

DBUtil ưu tiên JVM property → environment variable. URL và user mặc định lần lượt là
`localhost:3306/recruitflow` và `root`; mật khẩu không có giá trị mặc định và phải được cấu hình
theo môi trường. Source không chứa mật khẩu database.

## 3. AI CV Coach (tùy chọn)

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

## 4. Build và deploy

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

## 5. Tài khoản demo

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@recruitflow.com | 123456 |
| HR | hr@recruitflow.com | 123456 |
| Interviewer | interviewer@recruitflow.com | 123456 |
| Candidate | candidate@recruitflow.com | 123456 |

Chỉ dùng dev/demo. Đổi password và secret ở môi trường thật.

## 6. Entry route

| Role | URL |
| --- | --- |
| Guest | /home, /jobs |
| Candidate | /candidate/dashboard |
| HR | /hr/dashboard |
| Interviewer | /interviewer/dashboard |
| Admin | /admin/dashboard |

Chưa login vào private route phải về /login. Sai role phải 403.

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
