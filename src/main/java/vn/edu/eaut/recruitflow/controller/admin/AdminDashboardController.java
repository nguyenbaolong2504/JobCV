package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.AdminService;
import vn.edu.eaut.recruitflow.service.AuditLogService;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardController extends BaseController {
    private AdminService adminService;
    private AuditLogService auditLogService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
        auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("stats", adminService.getDashboardStats());
        try {
            request.setAttribute("recentAuditLogs", auditLogService.getRecent(6));
        } catch (BusinessException exception) {
            request.setAttribute("recentAuditLogs", java.util.List.of());
        }
        view(request, response, "/WEB-INF/views/admin/dashboard.jsp", "Bảng điều khiển quản trị | RecruitFlow");
    }
}
