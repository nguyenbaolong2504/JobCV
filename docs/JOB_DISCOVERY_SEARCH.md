# Tìm việc và lọc nâng cao

Hai trang tìm việc dùng cùng một hợp đồng lọc để Guest và Candidate nhìn thấy cùng tập tin tuyển dụng hợp lệ:

- Guest: `/jobs`
- Candidate: `/candidate/jobs` (bổ sung điểm match CV và thao tác ứng tuyển)

## Quy tắc hiển thị

Một tin chỉ xuất hiện trong danh sách công khai, danh sách Candidate, đề xuất CV và trang chi tiết công khai khi đồng thời thỏa:

```text
status = PUBLISHED
deadline >= CURRENT_DATE
```

Điều kiện này nằm ở `JobDAO.searchPublished`, `JobDAO.countPublished`, `JobDAO.findPublishedJobs` và `JobDAO.findOpenPublishedById`; không chỉ phụ thuộc vào giao diện. Vì vậy URL chi tiết của tin DRAFT, CLOSED, ARCHIVED hoặc PUBLISHED đã quá hạn trả 404.

`ApplicationService.apply` vẫn kiểm tra lại trạng thái và deadline trong transaction, bảo vệ trường hợp người dùng mở trang trước khi deadline hết hạn hoặc gửi request trực tiếp.

HR/Admin không thể chuyển DRAFT/CLOSED có deadline quá khứ sang `PUBLISHED`. Họ phải cập nhật hạn nộp từ hôm nay trở đi trước.

## Bộ lọc hỗ trợ

| Query parameter | Ý nghĩa |
| --- | --- |
| `keyword` | Tìm không phân biệt hoa thường trong title, job code, description, requirements và `job_skills.skill_name`. |
| `title` | Lọc riêng theo vị trí/chức danh. |
| `departmentId` | Phòng ban. |
| `categoryId` | Danh mục nghề nghiệp do Admin quản lý. Chọn danh mục chính sẽ lấy cả tin thuộc các danh mục con trực tiếp; chọn danh mục con chỉ lấy tin của danh mục đó. |
| `location` | Địa điểm làm việc (khớp một phần). |
| `employmentType` | `FULL_TIME`, `PART_TIME`, `INTERNSHIP`, `CONTRACT`, `REMOTE`. |
| `salaryMin`, `salaryMax` | Khoảng lương mong muốn. Tin khớp khi dải lương quảng cáo **giao nhau** với dải đã chọn: `job.salary_max >= salaryMin` và `job.salary_min <= salaryMax`. |
| `experienceMin`, `experienceMax` | Khoảng số năm kinh nghiệm mà tin yêu cầu. |
| `deadlineFrom`, `deadlineTo` | Khoảng hạn nộp hồ sơ. |
| `sort` | `newest`, `deadline`, `salary`, `experience`. |
| `page`, `pageSize` | Phân trang; `pageSize` hợp lệ là 10, 20 hoặc 50. |

Backend validate độ dài text, giá trị số không âm, giới hạn kinh nghiệm 0–100, enum loại hình, ngày hợp lệ và ràng buộc `from <= to`. Query sai ở public được quay về `/jobs` với flash error; Candidate nhìn thấy alert lỗi ngay trên trang.

## Trải nghiệm giao diện

- Bộ lọc nằm ở sidebar trên desktop, xếp bình thường trên màn hình nhỏ; danh mục lấy từ taxonomy đang hiển thị do Admin quản lý, không hard-code trong JSP.
- Sort có các hidden field nên giữ đủ filter khi đổi thứ tự.
- `common/pagination.jsp` giữ toàn bộ tham số lọc mới qua các trang.
- Card việc làm hiển thị phòng ban, địa điểm, hình thức đã Việt hóa, kinh nghiệm, khoảng lương và hạn nộp.
- Candidate thấy `Match %`, kỹ năng khớp và vẫn có thể mở chi tiết hoặc ứng tuyển bằng CV mặc định.

Schema hiện có `idx_jobs_status_deadline (status, deadline)`, phù hợp với điều kiện bắt buộc của danh sách công khai. Bộ lọc `categoryId` dùng bảng/cột danh mục mới; với database cũ hãy chạy migration danh mục theo [SETUP.md](SETUP.md#chính-sách-migration-an-toàn).

## Checklist kiểm thử thủ công

| ID | Thao tác | Kỳ vọng |
| --- | --- | --- |
| JS-01 | Tạo PUBLISHED còn hạn, DRAFT, CLOSED, ARCHIVED và PUBLISHED quá hạn. Mở `/jobs`. | Chỉ PUBLISHED còn hạn xuất hiện. |
| JS-02 | Mở `/jobs/detail?id=<id>` với tin quá hạn/DRAFT/CLOSED. | HTTP 404, không lộ dữ liệu job. |
| JS-03 | Vào `/candidate/jobs` với cùng dữ liệu. | Kết quả giống `/jobs`; chỉ khác thông tin Match. |
| JS-04 | Lọc `keyword=Java`, `title=Developer`, phòng ban, location, employment type. | Mọi job trả về thỏa tất cả điều kiện đã nhập. |
| JS-05 | Lọc `salaryMin=20000000&salaryMax=25000000`. | Tin 15–30 triệu được giữ lại vì dải lương giao nhau; tin 3–5 triệu bị loại. |
| JS-06 | Lọc `experienceMin=1&experienceMax=2` và deadline from/to. | Chỉ tin trong cả hai dải xuất hiện. |
| JS-07 | Đi tới trang 2 rồi đổi sort; kiểm tra URL. | Tất cả query filter và `pageSize` còn nguyên; sort quay về trang 1. |
| JS-08 | Gửi `salaryMin > salaryMax`, `experienceMin > experienceMax` hoặc `deadlineFrom > deadlineTo`. | Không query DB với điều kiện lỗi; hiển thị thông báo validation. |
| JS-09 | Cố Publish job có deadline hôm qua qua `/hr/jobs/status`. | Bị từ chối với thông báo phải cập nhật hạn nộp. |
| JS-10 | Gửi POST apply cho job vừa hết hạn. | Không tạo application; service báo hết hạn. |
| JS-11 | Tạo danh mục chính có hai danh mục con, gán các job vào từng danh mục con; lọc bằng `categoryId` của danh mục chính. | Kết quả gồm các job ở cả hai danh mục con; không lộ job ngoài nhóm. |
