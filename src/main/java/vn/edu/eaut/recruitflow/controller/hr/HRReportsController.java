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

@WebServlet("/hr/reports")
public class HRReportsController extends BaseController {
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LocalDate fromDate = optionalDate(request, "fromDate", "Từ ngày");
            LocalDate toDate = optionalDate(request, "toDate", "Đến ngày");
            Map<String, Object> report = reportService.getRecruitmentReport(
                    fromDate, toDate, RequestUtil.currentUserId(request));
            request.setAttribute("report", report);
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/reports.jsp", "Báo cáo tuyển dụng | RecruitFlow");
    }

    private LocalDate optionalDate(HttpServletRequest request, String field, String label) throws BusinessException {
        return RequestUtil.text(request, field).isEmpty() ? null : RequestUtil.date(request, field, label);
    }
}
