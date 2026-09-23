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

/** Displays the fixed authorization roles and their management policy. */
@WebServlet("/admin/roles")
public class AdminRoleController extends BaseController {
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roles", adminService.getRoles(RequestUtil.currentUserId(request)));
        } catch (BusinessException exception) {
            request.setAttribute("error", exception.getMessage());
        }
        view(request, response, "/WEB-INF/views/admin/roles.jsp", "Phân quyền | JobCV");
    }
}
