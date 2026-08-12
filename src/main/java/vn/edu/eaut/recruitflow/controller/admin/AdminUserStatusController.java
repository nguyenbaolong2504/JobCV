package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.service.AdminService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/users/status")
public class AdminUserStatusController extends BaseController {
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int userId = RequestUtil.requiredPositiveInt(request, "userId", "Người dùng");
            UserStatus status = UserStatus.fromValue(RequestUtil.text(request, "status"));
            adminService.updateUserStatus(userId, status, RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/admin/users", "Đã cập nhật trạng thái tài khoản.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/admin/users", ex.getMessage());
        }
    }
}
