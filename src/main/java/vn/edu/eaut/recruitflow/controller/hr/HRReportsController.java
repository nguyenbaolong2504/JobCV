package vn.edu.eaut.recruitflow.controller.hr;

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
import java.util.Map;

@WebServlet(urlPatterns = {
        "/hr/reports",
        "/hr/reports/overview",
        "/hr/reports/trends",
        "/hr/reports/pipeline",
        "/hr/reports/top-jobs",
        "/hr/reports/breakdown"
})
public class HRReportsController extends BaseController {
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reportSection = resolveSection(request.getServletPath());
        request.setAttribute("reportSection", reportSection);
        request.setAttribute("reportSectionPath", "/hr/reports/" + reportSection);
        try {
            LocalDate fromDate = optionalDate(request, "fromDate", "Từ ngày");
            LocalDate toDate = optionalDate(request, "toDate", "Đến ngày");
            if (fromDate == null && toDate == null) {
                toDate = LocalDate.now();
                fromDate = toDate.minusYears(5).withDayOfMonth(1);
            }
            Map<String, Object> report = reportService.getRecruitmentReport(
                    fromDate, toDate, RequestUtil.currentUserId(request));
            request.setAttribute("report", report);
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/reports.jsp", "Báo cáo tuyển dụng | JobCV");
    }

    private String resolveSection(String servletPath) {
        if (servletPath == null || "/hr/reports".equals(servletPath)) {
            return "overview";
        }
        String section = servletPath.substring(servletPath.lastIndexOf('/') + 1);
        return switch (section) {
            case "overview", "trends", "pipeline", "top-jobs", "breakdown" -> section;
            default -> "overview";
        };
    }

    private LocalDate optionalDate(HttpServletRequest request, String field, String label) throws BusinessException {
        return RequestUtil.text(request, field).isEmpty() ? null : RequestUtil.date(request, field, label);
    }
}
