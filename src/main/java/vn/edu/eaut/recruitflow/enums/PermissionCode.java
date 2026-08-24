package vn.edu.eaut.recruitflow.enums;

import java.util.Locale;

/**
 * Stable permission identifiers for the role-permission matrix. Values are deliberately
 * action-oriented so new URLs do not silently inherit broad module access.
 */
public enum PermissionCode {
    ADMIN_DASHBOARD_VIEW("Quản trị", "Xem tổng quan quản trị", "Xem dashboard hệ thống"),
    ADMIN_USERS_MANAGE("Quản trị", "Quản lý người dùng", "Xem, khóa, kích hoạt và gán vai trò người dùng"),
    ADMIN_PERMISSIONS_MANAGE("Quản trị", "Quản lý phân quyền", "Cấu hình ma trận quyền theo vai trò"),
    ADMIN_DEPARTMENTS_MANAGE("Quản trị", "Quản lý phòng ban", "Tạo, sửa và lưu trữ phòng ban"),
    ADMIN_CATEGORIES_MANAGE("Quản trị", "Quản lý danh mục", "Quản lý danh mục nghề nghiệp và danh mục con"),
    ADMIN_AUDIT_VIEW("Quản trị", "Xem nhật ký", "Xem nhật ký audit hệ thống"),

    HR_DASHBOARD_VIEW("Tuyển dụng", "Xem dashboard HR", "Xem số liệu tuyển dụng"),
    HR_JOBS_MANAGE("Tuyển dụng", "Quản lý tin tuyển", "Tạo, sửa, publish, đóng và lưu trữ tin tuyển"),
    HR_APPLICATIONS_MANAGE("Tuyển dụng", "Quản lý hồ sơ", "Sàng lọc và quản lý hồ sơ ứng tuyển"),
    HR_INTERVIEWS_MANAGE("Tuyển dụng", "Quản lý phỏng vấn", "Lên lịch, đổi lịch và hủy phỏng vấn"),
    HR_OFFERS_MANAGE("Tuyển dụng", "Quản lý offer", "Tạo, gửi và quản lý offer"),
    HR_ONBOARDING_MANAGE("Tuyển dụng", "Quản lý onboarding", "Theo dõi và giao việc onboarding"),
    HR_REPORTS_VIEW("Tuyển dụng", "Xem báo cáo", "Xem và xuất báo cáo tuyển dụng"),
    HR_NOTIFICATIONS_VIEW("Tuyển dụng", "Xem thông báo HR", "Đọc và cập nhật thông báo HR"),

    INTERVIEWER_DASHBOARD_VIEW("Phỏng vấn", "Xem dashboard Interviewer", "Xem tổng quan các lịch được phân công"),
    INTERVIEWER_INTERVIEWS_VIEW("Phỏng vấn", "Xem lịch được phân công", "Xem lịch và hồ sơ ứng viên được phân công"),
    INTERVIEWER_FEEDBACK_SUBMIT("Phỏng vấn", "Gửi đánh giá", "Gửi feedback cho lịch phỏng vấn đã kết thúc"),
    INTERVIEWER_NOTIFICATIONS_VIEW("Phỏng vấn", "Xem thông báo Interviewer", "Đọc và cập nhật thông báo phỏng vấn"),

    CANDIDATE_JOBS_VIEW("Ứng viên", "Tìm việc", "Xem và tìm kiếm tin tuyển dụng"),
    CANDIDATE_PROFILE_MANAGE("Ứng viên", "Quản lý hồ sơ cá nhân", "Cập nhật hồ sơ ứng viên"),
    CANDIDATE_RESUMES_MANAGE("Ứng viên", "Quản lý CV", "Tạo, tải lên, tải xuống và chọn CV"),
    CANDIDATE_APPLICATIONS_MANAGE("Ứng viên", "Quản lý ứng tuyển", "Ứng tuyển, xem và rút đơn của bản thân"),
    CANDIDATE_INTERVIEWS_VIEW("Ứng viên", "Xem lịch phỏng vấn", "Xem lịch phỏng vấn của bản thân"),
    CANDIDATE_OFFERS_RESPOND("Ứng viên", "Phản hồi offer", "Xem và phản hồi offer của bản thân"),
    CANDIDATE_ONBOARDING_MANAGE("Ứng viên", "Thực hiện onboarding", "Cập nhật công việc onboarding của bản thân"),
    CANDIDATE_NOTIFICATIONS_VIEW("Ứng viên", "Xem thông báo ứng viên", "Đọc và cập nhật thông báo cá nhân");

    private final String module;
    private final String displayName;
    private final String description;

    PermissionCode(String module, String displayName, String description) {
        this.module = module;
        this.displayName = displayName;
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static PermissionCode fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Permission không hợp lệ.");
        }
        return PermissionCode.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
