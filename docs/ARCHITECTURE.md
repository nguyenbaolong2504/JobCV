# Kiến trúc RecruitFlow

## MVC + Service + DAO

~~~mermaid
flowchart LR
    B[Browser] --> F[Filters]
    F --> C[Servlet Controller]
    C --> S[Service]
    S --> D[DAO]
    D --> J[JDBC PreparedStatement]
    J --> M[(MySQL)]
    M --> D
    D --> S
    S --> C
    C --> V[JSP + JSTL / EL]
    V --> B
~~~

## Package

| Package | Trách nhiệm |
| --- | --- |
| controller | Nhận HTTP request, lấy session/parameter, gọi service, forward/redirect. |
| controller.auth | Login/register/logout, Gmail OTP login/reset and Google OAuth callback. |
| controller.candidate | Profile, CV, job, application, interview, offer, onboarding, notification. |
| controller.hr | Dashboard, jobs, candidates/pipeline, interview, offer, onboarding, reports. |
| controller.interviewer | Lịch interview và feedback. |
| controller.admin | Dashboard, users, departments, audit logs. |
| service | Business rules, ownership/actor check, transaction, phối hợp DAO. |
| dao | SQL JDBC và mapping ResultSet sang model. |
| model | Entity, PageResult, dashboard stats, matching result. |
| enums | Role và state machine. |
| filter | UTF-8, login/session, URL authorization. |
| util | DBUtil, BCrypt PasswordUtil, session/OTP/OAuth state, FlashMessage, UploadUtil, ResumeParser. |

## Quy tắc theo lớp

### Controller

- GET lấy dữ liệu từ service, set request attribute và forward JSP ở WEB-INF/views.
- POST gọi service, ghi flash message, sendRedirect để áp dụng PRG.
- Lấy userId/fullName/role từ HttpSession; không tin actor/role gửi từ browser.
- Không có JDBC, SQL hoặc workflow logic lớn.

### Service

Service là nơi thực thi nghiệp vụ:

| Service | Nhiệm vụ |
| --- | --- |
| ApplicationService | Apply, BR01–BR04, status transition, timeline/history. |
| JobService | Job validation, CRUD, publish/close/archive, skills. |
| InterviewService | Schedule/reschedule/cancel, conflict, feedback. |
| OfferService | Create/send/respond offer, hire/onboarding. |
| OnboardingService | Checklist, task status và progress. |
| MatchingService | Weighted skill matching, recommended jobs. |
| User/Admin/Department Service | Account, profile, user management, department. |
| Notification/AuditLog Service | Notifications và audit actions. |

BusinessException là lỗi nghiệp vụ để controller trả flash/error rõ ràng cho user.

### DAO

DAO chỉ chứa query, mapping và persistence. Input SQL luôn qua PreparedStatement. DAO có overload nhận Connection cho transaction đa bảng.

DAO bao phủ: User/Role/CandidateProfile/RecruiterProfile/Resume/Department, Job/JobSkill, Application/ApplicationStatusHistory, Interview/InterviewFeedback, Offer, Onboarding/OnboardingTask, Notification, AuditLog, OTP và OAuth account link.

### JSP

JSP chỉ render view bằng EL/JSTL (c:if, c:forEach, c:out); không query database, không dùng scriptlet cho logic. View nằm dưới WEB-INF/views nên không truy cập trực tiếp.

~~~text
WEB-INF/views/
├── auth/          login, register, OTP verify/reset, Google OAuth callback
├── public/        home, jobs, job detail, 403/404/500
├── candidate/     dashboard, profile, resumes, applications, interviews, offers, onboarding
├── hr/            dashboard, jobs, applications, interviews, offers, onboarding, reports
├── interviewer/   dashboard, interview list/detail/feedback
├── admin/         dashboard, users, departments, audit logs
└── common/        header, footer, navbar, sidebars, flash, pagination
~~~

## Session, filters và role

Login thành công lưu vào HttpSession:

~~~text
userId
fullName
role
~~~

| Prefix | Role |
| --- | --- |
| /candidate/ | CANDIDATE |
| /hr/ | HR hoặc ADMIN |
| /interviewer/ | INTERVIEWER |
| /admin/ | ADMIN |

Ngoài filter, service nhạy cảm kiểm tra ownership: interviewer được gán mới feedback, candidate chỉ phản hồi offer/task của mình, HR/Admin mới quản lý job/offer/onboarding. Admin không vào workspace `/interviewer/` vì không có interview assignment; Admin dùng workspace quản trị hoặc HR.

Mỗi request có session đăng nhập được đối chiếu lại `users.status` và role hiện tại trước khi tiếp tục. Nếu tài khoản bị lock/deactivate, bị xóa hoặc role đổi, session cũ bị hủy và user phải đăng nhập lại.

## Auth và account lifecycle

- Password login tạo session mới (chống session fixation) và Candidate luôn landing tại `/home`; dashboard vẫn là `/candidate/dashboard` theo lựa chọn của user.
- `CANDIDATE` public registration tạo user `ACTIVE` và `candidate_profiles`. Public recruiter registration tạo `HR/INACTIVE` + `recruiter_profiles`; Admin nhìn thấy organization/job title/work phone rồi mới kích hoạt.
- OTP password reset và OTP login dùng hai bảng riêng, BCrypt hash, expiry 10 phút, one-time consume, tối đa 5 lần thử và resend cooldown 60 giây. OTP login chỉ được ép khi cả SMTP và `RECRUITFLOW_AUTH_OTP_REQUIRED=true` đều có.
- Google OAuth dùng Authorization Code + state session-bound. Server đổi code, xác minh ID token với Google (issuer/audience/email verified), chỉ tạo/liên kết Candidate theo provider subject; không lưu token OAuth.

## Database config

DBUtil nhận config theo thứ tự JVM property → environment variable → default local.

| Ý nghĩa | JVM property | Environment variable | Default |
| --- | --- | --- | --- |
| JDBC URL | recruitflow.db.url | RECRUITFLOW_DB_URL | localhost:3306/recruitflow |
| DB user | recruitflow.db.user | RECRUITFLOW_DB_USER | root |
| DB password | recruitflow.db.password | RECRUITFLOW_DB_PASSWORD | Không có fallback |

`schema.sql` và default JDBC URL đều dùng database `recruitflow`. Không commit DB secret thật vào source; nếu MySQL yêu cầu password, phải cung cấp qua JVM property hoặc environment variable. Cách cấu hình cụ thể nằm trong [SETUP.md](SETUP.md).

## Transaction

| Use case | Dữ liệu phải đồng nhất |
| --- | --- |
| Apply | application + status history + notification |
| Status change | application status + history + notification |
| Feedback | feedback + interview completed + application interviewed + history |
| Offer | offer + application/history + notification; accept tạo onboarding |
| Onboarding task | task + onboarding progress/status |

Mọi lỗi SQL hoặc BusinessException phải rollback.

## CV upload và matching

Upload chỉ nhận PDF/DOC/DOCX tối đa 5 MB, kiểm tra extension/MIME và sinh file name resume_{candidateId}_{timestamp}.{extension}. PDFBox parse PDF; Apache POI parse DOC/DOCX.

MatchingService chuẩn hóa CV text và skill, tính tổng weight skill match/tổng weight job_skills, trả matchScore, matchedSkills, missingSkills. Service này tách riêng để sau này thay bằng AI API mà không đổi UI/controller.

## Schema

schema.sql tạo 20 bảng: roles, users, candidate_profiles, recruiter_profiles, password_reset_otps, login_verification_otps, oauth_accounts, resumes, departments, jobs, job_skills, applications, application_status_history, interviews, interview_feedbacks, offers, onboardings, onboarding_tasks, notifications, audit_logs.

Quan hệ chính:

~~~text
roles → users → candidate_profiles / recruiter_profiles / resumes
users → password_reset_otps / login_verification_otps / oauth_accounts
departments → jobs → job_skills
jobs + candidate + resume → applications → application_status_history
applications → interviews → interview_feedbacks
applications → offers
applications → onboardings → onboarding_tasks
users → notifications / audit_logs
~~~

## Cách thêm chức năng

1. Bổ sung enum/model/schema nếu cần data mới.
2. Viết DAO PreparedStatement và Connection overload nếu use case có transaction.
3. Đặt rule ở service.
4. Tạo Servlet đúng namespace actor, để filter bảo vệ URL.
5. Viết JSP dùng JSTL/EL; POST áp dụng PRG + flash.
6. Build WAR và chạy manual test module bị ảnh hưởng.
