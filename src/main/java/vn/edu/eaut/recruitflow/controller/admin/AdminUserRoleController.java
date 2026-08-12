package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.AdminService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Assigns one of the fixed RecruitFlow roles to a user. */
@WebServlet("/admin/users/role")
public class AdminUserRoleController extends BaseController {
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int userId = RequestUtil.requiredPositiveInt(request, "userId", "Người dùng");
            int roleId = RequestUtil.requiredPositiveInt(request, "roleId", "Vai trò");
            adminService.updateUserRole(userId, roleId, RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/admin/users", "Đã cập nhật vai trò người dùng.");
        } catch (BusinessException | IllegalArgumentException exception) {
            redirectWithError(request, response, "/admin/users", exception.getMessage());
        }
    }
}
