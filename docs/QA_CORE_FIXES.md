# Cập nhật QA core – 22/08/2026

Tài liệu này ghi nhận các sửa đổi core sau báo cáo QA ban đầu. Đây là thay đổi source; browser E2E vẫn cần chạy trên Tomcat 9 + MySQL 8 trước khi release.

| Mã QA | Trạng thái sau sửa | Cách xử lý |
| --- | --- | --- |
| F-01 DB config | Đã sửa | JDBC mặc định dùng `recruitflow` như `schema.sql`; không còn password hard-code. Password lấy qua `recruitflow.db.password`/`RECRUITFLOW_DB_PASSWORD`. |
| F-02 Session stale | Đã sửa (request kế tiếp) | Filter đối chiếu user/status/role với DB; lock, deactivate, xóa user hoặc đổi role sẽ hủy session cũ. |
| F-03 Report export | Đã sửa | Link CSV truyền `fromDate`/`toDate`; export dùng cùng `ReportService`. Metric offer áp dụng cùng cohort application. |
| F-05 Offer expiry | Đã sửa (lazy normalization) | Offer `SENT` quá hạn được chuyển `EXPIRED` trước các luồng create/list/get/send. Không cần scheduler để đảm bảo UI/API có state hiện hành. |
| F-06 Offer expiry filter | Đã sửa | Nhãn và SQL cùng semantics: “Hết hạn vào hoặc trước”, điều kiện `expiry_date <= ?`. |
| F-07 Staff notifications | Đã sửa | Thêm inbox và mark-read cho `/hr/notifications` và `/interviewer/notifications`. |
| F-08 Audit filter/pagination | Đã sửa | Keyword/date filter chạy trong SQL trước `LIMIT/OFFSET`; `count` dùng đúng cùng predicate. |
| F-09 Admin/interviewer | Đã sửa | `/interviewer/*` chỉ dành cho `INTERVIEWER`; Admin dùng khu vực Admin/HR, tránh bypass ownership không có assignment context. |
| F-10 Interview lifecycle | Đã sửa | Mỗi application chỉ có một lịch `SCHEDULED`/`RESCHEDULED`; feedback chỉ được gửi sau giờ kết thúc và không thể làm nhảy trạng thái khi còn lịch active khác. |

## Chính sách reissue offer

Mỗi application chỉ giữ một row offer vì unique key `offers.application_id`. Khi offer `SENT` hết hạn, row chuyển `EXPIRED`; HR chọn **Tạo lại offer** để thay nội dung và đưa chính row đó về `DRAFT`. Lần gửi lại không tạo row trùng và không đổi application ra khỏi `OFFERED`.

Trên dashboard Candidate, chỉ offer `SENT` mới được đếm là đang chờ phản hồi. Offer đã accepted, declined hoặc expired vẫn nằm trong lịch sử offer nhưng không còn kích hoạt cảnh báo chờ xử lý.

## Regression checklist cần chạy

1. Đăng nhập HR ở Browser A, lock hoặc đổi role bằng Admin ở Browser B; request kế tiếp ở A phải quay về login.
2. Lọc Reports theo ngày và export CSV; toàn bộ dòng metric, gồm Offers sent, phải thuộc cùng date range application.
3. Tạo offer `SENT` có deadline hôm qua; vào candidate/HR offers phải thấy `EXPIRED`, candidate không còn nút phản hồi; HR có thể reissue và gửi lại.
4. Tạo notification cho HR và Interviewer qua apply/schedule; mở inbox tương ứng, mark one/read-all và kiểm tra chỉ owner thay đổi `is_read`.
5. Lọc Audit log theo một phần tên/email/action và `fromDate`; kiểm tra table, `totalPages` và page 2 đều cùng tập kết quả.
6. Login Admin, mở `/interviewer/interviews`; kỳ vọng HTTP 403. Login Interviewer được gán; kỳ vọng truy cập bình thường.
7. Tạo lịch cho ứng viên đã `SHORTLISTED`, sau đó thử tạo thêm lịch active khác cho cùng application; kỳ vọng bị chặn. Thử gửi feedback trước `end_time`; kỳ vọng bị chặn và application vẫn `INTERVIEW_SCHEDULED`.
