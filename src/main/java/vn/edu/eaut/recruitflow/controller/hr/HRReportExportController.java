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
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Map;

/** Exports the same filtered recruitment report displayed in the HR portal as UTF-8 CSV. */
@WebServlet("/hr/reports/export")
public class HRReportExportController extends BaseController {
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            LocalDate fromDate = optionalDate(request, "fromDate", "Từ ngày");
            LocalDate toDate = optionalDate(request, "toDate", "Đến ngày");
            Map<String, Object> report = reportService.getRecruitmentReport(fromDate, toDate);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=recruitment-report.csv");
            try (PrintWriter writer = response.getWriter()) {
                writer.write('\uFEFF');
                writer.println("Metric,Value");
                writeRow(writer, "From date", fromDate == null ? "" : fromDate.toString());
                writeRow(writer, "To date", toDate == null ? "" : toDate.toString());
                writeRow(writer, "Total applications", report.get("totalApplications"));
                writeRow(writer, "Screening", report.get("screening"));
                writeRow(writer, "Interview", report.get("interview"));
                writeRow(writer, "Offers", report.get("offered"));
                writeRow(writer, "Hired", report.get("hired"));
                writeRow(writer, "Hire rate (%)", report.get("hireRate"));
            }
        } catch (BusinessException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    private LocalDate optionalDate(HttpServletRequest request, String field, String label) throws BusinessException {
        return RequestUtil.text(request, field).isEmpty() ? null : RequestUtil.date(request, field, label);
    }

    private void writeRow(PrintWriter writer, String label, Object value) {
        writer.println(csv(label) + "," + csv(value == null ? "" : String.valueOf(value)));
    }

    private String csv(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
