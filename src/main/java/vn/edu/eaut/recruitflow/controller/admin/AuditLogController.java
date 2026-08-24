package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.AuditLog;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.AdminService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/admin/audit-logs")
public class AuditLogController extends BaseController {
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LocalDate fromDate = RequestUtil.text(request, "fromDate").isEmpty()
                    ? null : RequestUtil.date(request, "fromDate", "Từ ngày");
            PageResult<AuditLog> page = adminService.searchAuditLogs(
                    RequestUtil.text(request, "keyword"),
                    RequestUtil.text(request, "entityName"),
                    fromDate,
                    RequestUtil.page(request),
                    RequestUtil.pageSize(request)
            );
            request.setAttribute("auditLogs", page.getItems());
            request.setAttribute("page", page);
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/admin/audit-logs.jsp", "Nhật ký hệ thống | RecruitFlow");
    }
}
