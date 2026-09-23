package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.RoleName;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.AdminService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/users")
public class AdminUserController extends BaseController {
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            RoleName role = RequestUtil.text(request, "role").isEmpty()
                    ? null : RoleName.fromValue(RequestUtil.text(request, "role"));
            UserStatus status = RequestUtil.text(request, "status").isEmpty()
                    ? null : UserStatus.fromValue(RequestUtil.text(request, "status"));
            PageResult<User> page = adminService.searchUsers(
                    RequestUtil.text(request, "keyword"), role, status,
                    RequestUtil.page(request), RequestUtil.pageSize(request));
            int actorId = RequestUtil.currentUserId(request);
            request.setAttribute("users", page.getItems());
            request.setAttribute("page", page);
            request.setAttribute("roles", adminService.getRoles(actorId));
            request.setAttribute("recruiterProfiles", adminService.getRecruiterProfiles(page.getItems(), actorId));
            request.setAttribute("companies", adminService.getCompanies(actorId));
            request.setAttribute("companyMemberships", adminService.getCompanyMemberships(page.getItems(), actorId));
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/admin/users.jsp", "Quản lý người dùng | JobCV");
    }
}
