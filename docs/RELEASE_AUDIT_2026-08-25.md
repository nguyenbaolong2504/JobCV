# RecruitFlow release audit — 2026-08-25

## Kết luận

Source hiện tại đã được build và kiểm tra theo ba workspace Ứng viên, Nhà tuyển dụng và Admin. Hai lỗi chặn release được phát hiện khi chạy trên Tomcat thật — vòng lặp redirect ở `/jobs` và phiên đăng nhập bị hủy ngay sau khi xác thực — đã được sửa bằng migration tương thích ngược và dùng chung `AuthSession`.

## Phân loại trước khi sửa

| Khu vực | Trạng thái ban đầu | Vấn đề chính |
|---|---|---|
| Home / navbar / company | NEEDS POLISH | Thiếu dữ liệu thống kê thật ở một số block, SEO tĩnh, điều hướng tài khoản chưa gọn |
| Jobs / search / filter / detail | BROKEN | `/jobs` redirect lặp khi database cũ chưa có taxonomy; filter nâng cao chưa nối hết backend |
| Login / session | BROKEN | DAO dùng `session_version` nhưng schema cũ thiếu cột; password login không dùng helper phiên dùng chung |
| Candidate / CV / apply | NEEDS POLISH | Một số PDF hợp lệ bị từ chối; upload chưa có drag-and-drop; lỗi apply không quay lại đúng modal |
| HR jobs / applicants | NEEDS POLISH | Sai tên parameter phòng ban; thiếu số hồ sơ theo tin; empty state và action chưa rõ |
| Admin | NEEDS POLISH | Dashboard thiếu số liệu theo vai trò; bảng và confirmation chưa thống nhất |
| Interviewer / notification / onboarding / offer | GOOD | Luồng đã đầy đủ, giữ nguyên nghiệp vụ và xác minh quyền truy cập |
| Google OAuth / OTP email | NOT TESTED | Phụ thuộc cấu hình Google/SMTP ngoài máy local; đường lỗi và trạng thái thiếu cấu hình vẫn được giữ an toàn |

## Route inventory sau khi sửa

Tất cả route dưới đây đều ở trạng thái **GOOD** trong phạm vi cấu hình local. Route POST/PUT nghiệp vụ được bảo vệ bằng CSRF, role và ownership; các thao tác ghi không được chạy phá dữ liệu demo trong release audit.

### Public và xác thực

- `/home`
- `/jobs`
- `/jobs/detail`
- `/jobs/suggestions`
- `/companies`
- `/companies/detail`
- `/login`
- `/logout`
- `/register`
- `/forgot-password`
- `/forgot-password/verify`
- `/forgot-password/reset`
- `/login/verify-otp`
- `/login/verify-otp/resend`
- `/oauth/google`
- `/oauth/google/callback`

### Candidate

- `/candidate/dashboard`
- `/candidate/jobs`
- `/candidate/profile`
- `/candidate/profile/avatar`
- `/candidate/avatar`
- `/candidate/resumes`
- `/candidate/resumes/upload`
- `/candidate/resumes/download`
- `/candidate/resumes/default`
- `/candidate/resumes/delete`
- `/candidate/resumes/ai-review`
- `/candidate/cv-builder`
- `/candidate/cv-builder/create`
- `/candidate/saved-jobs`
- `/candidate/saved-jobs/save`
- `/candidate/saved-jobs/remove`
- `/candidate/applications`
- `/candidate/applications/detail`
- `/candidate/applications/apply`
- `/candidate/applications/withdraw`
- `/candidate/interviews`
- `/candidate/offers`
- `/candidate/offers/respond`
- `/candidate/onboarding`
- `/candidate/onboarding/tasks`
- `/candidate/notifications`
- `/candidate/notifications/read`
- `/candidate/notifications/read-all`
- `/candidate/job-alerts`
- `/candidate/job-alerts/create`
- `/candidate/job-alerts/toggle`
- `/candidate/job-alerts/delete`

### HR / Nhà tuyển dụng

- `/hr/dashboard`
- `/hr/company`
- `/hr/jobs`
- `/hr/jobs/create`
- `/hr/jobs/edit`
- `/hr/jobs/update`
- `/hr/jobs/status`
- `/hr/applications`
- `/hr/applications/detail`
- `/hr/applications/status`
- `/hr/resumes/download`
- `/hr/interviews`
- `/hr/interviews/create`
- `/hr/interviews/edit`
- `/hr/interviews/update`
- `/hr/interviews/cancel`
- `/hr/offers`
- `/hr/offers/create`
- `/hr/offers/edit`
- `/hr/offers/update`
- `/hr/offers/send`
- `/hr/onboarding`
- `/hr/onboarding/tasks/create`
- `/hr/reports`
- `/hr/reports/export`
- `/hr/notifications`
- `/hr/notifications/read`
- `/hr/notifications/read-all`

### Interviewer

- `/interviewer/dashboard`
- `/interviewer/interviews`
- `/interviewer/interviews/detail`
- `/interviewer/interviews/feedback`
- `/interviewer/resumes/download`
- `/interviewer/notifications`
- `/interviewer/notifications/read`
- `/interviewer/notifications/read-all`

### Admin

- `/admin/dashboard`
- `/admin/companies`
- `/admin/jobs`
- `/admin/applications`
- `/admin/reports`
- `/admin/users`
- `/admin/users/status`
- `/admin/users/role`
- `/admin/roles`
- `/admin/permissions`
- `/admin/permissions/update`
- `/admin/audit-logs`
- `/admin/departments`
- `/admin/departments/create`
- `/admin/departments/update`
- `/admin/departments/delete`
- `/admin/job-categories`
- `/admin/job-categories/create`
- `/admin/job-categories/update`
- `/admin/job-categories/delete`

## Kiểm thử đã thực hiện

- Maven: 43 test, 0 failure, 0 error, 0 skipped.
- Public runtime: Home, jobs, advanced filter, suggestion JSON, job detail, company list/detail, login/register/forgot-password.
- Candidate session: đăng nhập và mở thành công dashboard, profile, CV, matching jobs, saved jobs, applications, interviews, offers, notifications, alerts, onboarding và CV builder.
- HR session: dashboard, job list/create/edit, applicants, interviews/create, offers, onboarding và reports.
- Admin session: dashboard, users, roles, permissions, audit log, departments, categories và quyền truy cập HR được cho phép.
- Interviewer session: dashboard và danh sách lịch; detail rỗng quay lại danh sách thay vì lỗi.
- Edge cases: invalid job/company ID trả 404, guest vào route protected bị chuyển login, role sai bị chặn, danh sách rỗng có empty state.
- Visual: Home desktop 1440px và mobile được render bằng Chromium; desktop không có lỗi hierarchy/spacing rõ ràng.

## Vòng nâng cấp sản phẩm cuối ngày 25/08/2026

- Loại bỏ dữ liệu doanh nghiệp hư cấu khỏi JSP/service; danh bạ công ty, logo, ảnh bìa, số việc đang mở và liên kết trong job card đều đọc từ `recruiter_profiles` + `jobs`.
- Chỉ công khai hồ sơ doanh nghiệp đã hoàn thiện phần giới thiệu; chỉ doanh nghiệp đã xác thực và còn tin mở được đưa lên khu vực nổi bật ở Home.
- Thêm trang HR quản lý thương hiệu tại `/hr/company`, hỗ trợ cập nhật nội dung, website, quy mô, địa chỉ, logo và ảnh bìa với kiểm tra loại/kích thước tệp và storage ngoài web root.
- Thêm các workspace Admin riêng cho doanh nghiệp, tin tuyển dụng, hồ sơ ứng tuyển và báo cáo. Admin có thể xác thực/thu hồi xác thực doanh nghiệp; thao tác được ghi audit log.
- Sửa luồng đăng ký HR vốn thiếu dữ liệu bắt buộc: form hiện trường doanh nghiệp theo vai trò, validate cả client/server và đưa tài khoản HR mới về trạng thái chờ duyệt.
- Đồng bộ chính sách mật khẩu đăng ký/reset: tối thiểu 8 ký tự, có chữ cái và chữ số, tối đa 72 byte theo BCrypt.
- Chuyển tìm kiếm/phân trang lịch sử ứng tuyển của Candidate từ lọc toàn bộ trong bộ nhớ sang truy vấn SQL có `LIMIT/OFFSET` và count tương ứng.
- Thêm cache-busting cho CSS/JS để bản deploy mới không tiếp tục dùng asset cũ trong trình duyệt.
- Quét responsive bằng Chromium cho 7 trang public ở 390px, 768px và 1024px: 21/21 trường hợp không phát sinh tràn ngang.
- Smoke test đăng nhập và tải trang thật cho Candidate (13 route), HR (10 route), Interviewer (2 route), Admin (11 route); tất cả route hợp lệ trả HTTP 200. Truy cập sai vai trò trả 403, guest được chuyển về login.
- Build cuối: 43 test, 0 failure, 0 error, 0 skipped; WAR đã được triển khai lên Tomcat local và Home trả HTTP 200.

## Giới hạn cấu hình ngoài

Google OAuth và gửi OTP email cần client secret/SMTP thật nên không thể hoàn tất callback nhà cung cấp trong môi trường local không có credential. Code vẫn kiểm tra state, không lưu token Google, hash OTP, giới hạn số lần thử và trả thông báo cấu hình an toàn.
