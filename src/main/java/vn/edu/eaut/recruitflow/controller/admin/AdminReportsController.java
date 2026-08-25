package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.ReportService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/admin/reports")
public class AdminReportsController extends BaseController {
    private ReportService reportService;
    @Override public void init() { reportService = new ReportService(); }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            LocalDate from = RequestUtil.text(request, "fromDate").isEmpty() ? null : RequestUtil.date(request, "fromDate", "Từ ngày");
            LocalDate to = RequestUtil.text(request, "toDate").isEmpty() ? null : RequestUtil.date(request, "toDate", "Đến ngày");
            request.setAttribute("report", reportService.getRecruitmentReport(from, to, RequestUtil.currentUserId(request)));
        } catch (BusinessException exception) { request.setAttribute("error", exception.getMessage()); }
        view(request, response, "/WEB-INF/views/admin/reports.jsp", "Báo cáo nền tảng | RecruitFlow");
    }
}
