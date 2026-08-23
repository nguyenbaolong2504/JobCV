# Hướng dẫn cổng người tìm việc

## Sau khi đăng nhập

Tài khoản **Người tìm việc (Candidate)** được đưa về `/home`, tức trang tìm việc công khai đã cá nhân hóa nhẹ theo phiên đăng nhập. Đây không phải dashboard để người dùng có thể bắt đầu bằng việc khám phá cơ hội.

Từ menu hồ sơ hoặc các thẻ trên trang chủ, Candidate có thể mở:

- **Tạo CV theo mẫu** – `/candidate/cv-builder`
- **Tìm việc phù hợp** – `/candidate/jobs`
- **Đơn ứng tuyển** – `/candidate/applications`
- **Bảng điều khiển** – `/candidate/dashboard`

## Tìm việc nâng cao

Trang `/jobs` và `/candidate/jobs` có bộ lọc nhiều tiêu chí. Có thể kết hợp bất kỳ điều kiện nào sau đây:

| Điều kiện | Ý nghĩa |
| --- | --- |
| Từ khóa / kỹ năng | Tìm trong chức danh, mã tin, mô tả, yêu cầu và kỹ năng job |
| Vị trí / chức danh | Thu hẹp theo tên vị trí |
| Phòng ban | Chọn department tuyển dụng |
| Địa điểm | Ví dụ Hà Nội, Đà Nẵng, Remote |
| Hình thức làm việc | Full-time, Part-time, Internship, Contract, Remote |
| Khoảng lương | Hiển thị job có khoảng lương giao với khoảng lương mong muốn |
| Kinh nghiệm | Lọc theo số năm kinh nghiệm yêu cầu |
| Hạn nộp | Chỉ định khoảng deadline cần tìm |
| Sắp xếp | Mới đăng, hạn gần nhất, lương cao, ít kinh nghiệm trước |

Chỉ các job `PUBLISHED` **và còn hạn nhận hồ sơ** mới được public/candidate thấy hoặc mở chi tiết.

## Tạo CV theo mẫu

1. Mở **Tạo CV theo mẫu**.
2. Chọn một trong ba mẫu: **Hiện đại**, **Chuyên nghiệp**, hoặc **Tối giản**.
3. Điền thông tin liên hệ, vị trí mục tiêu, giới thiệu, kinh nghiệm/học vấn, kỹ năng, dự án và chứng chỉ.
4. Chọn “Đặt CV vừa tạo làm CV mặc định” nếu muốn sử dụng CV này ở lần ứng tuyển kế tiếp.
5. Nhấn **Tạo CV DOCX**.

Hệ thống tạo một tệp DOCX trong khu vực CV của Candidate, trích xuất text để hỗ trợ matching, và lưu theo cùng chính sách ownership/download như CV upload. CV cần có ít nhất **học vấn hoặc kinh nghiệm**, cùng họ tên, vị trí mục tiêu và giới thiệu. Nội dung phải trung thực; các mẫu chỉ cung cấp bố cục, không tự tạo kinh nghiệm giả.

## Ứng tuyển và theo dõi

Khi Candidate nhấn Apply, hệ thống kiểm tra:

1. Job còn `PUBLISHED` và chưa hết hạn.
2. Candidate chưa nộp job đó trước đây.
3. CV được chọn thuộc chính Candidate.

Nếu hợp lệ, hệ thống tạo application `SUBMITTED`, lịch sử trạng thái và notifications trong cùng transaction. Candidate có thể xem timeline tại **Đơn ứng tuyển**; chỉ được rút đơn khi trạng thái còn `SUBMITTED`.

## Luồng tài khoản

- **Người tìm việc:** đăng ký cá nhân, được dùng ngay sau khi tạo tài khoản.
- **Nhà tuyển dụng:** khai báo thông tin tổ chức, tài khoản chờ Admin kích hoạt trước khi vào khu vực HR.
- **Đăng nhập Google:** chỉ dùng khi quản trị viên đã cấu hình Google OAuth; account mới qua Google mặc định là Candidate để tránh tự nâng quyền HR.
- **OTP qua email:** các tính năng quên mật khẩu và xác minh đăng nhập chỉ hoạt động khi SMTP Gmail được cấu hình bởi quản trị viên. Xem [SETUP.md](SETUP.md) để cấu hình an toàn.
