package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobAlertService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Candidate job-alert builder, activation state, and deletion endpoints. */
@WebServlet(name = "CandidateJobAlertController", urlPatterns = {
        "/candidate/job-alerts",
        "/candidate/job-alerts/create",
        "/candidate/job-alerts/toggle",
        "/candidate/job-alerts/delete"
})
public class CandidateJobAlertController extends CandidateBaseController {
    private JobAlertService jobAlertService;
    private DepartmentService departmentService;

    @Override
    public void init() throws ServletException {
        jobAlertService = new JobAlertService();
        departmentService = new DepartmentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/job-alerts".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("alerts", List.of());
        request.setAttribute("departments", List.of());
        try {
            int candidateId = currentCandidateId(request);
            request.setAttribute("alerts", jobAlertService.getAlerts(candidateId));
            request.setAttribute("departments", departmentService.getAllDepartments());
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/job-alerts.jsp", "Thông báo việc làm | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        String path = request.getServletPath();
        try {
            int candidateId = currentCandidateId(request);
            if ("/candidate/job-alerts/create".equals(path)) {
                Integer departmentId = RequestUtil.text(request, "departmentId").isEmpty()
                        ? null : RequestUtil.requiredPositiveInt(request, "departmentId", "Phòng ban");
                jobAlertService.create(candidateId, RequestUtil.text(request, "name"),
                        RequestUtil.text(request, "keyword"), departmentId, RequestUtil.text(request, "location"),
                        RequestUtil.text(request, "employmentType"), RequestUtil.text(request, "frequency"));
                redirectWithSuccess(request, response, "/candidate/job-alerts", "Đã tạo thông báo việc làm mới.");
                return;
            }
            int alertId = RequestUtil.requiredPositiveInt(request, "alertId", "Thông báo việc làm");
            if ("/candidate/job-alerts/toggle".equals(path)) {
                jobAlertService.setActive(candidateId, alertId, Boolean.parseBoolean(RequestUtil.text(request, "active")));
                redirectWithSuccess(request, response, "/candidate/job-alerts", "Đã cập nhật trạng thái thông báo.");
                return;
            }
            if ("/candidate/job-alerts/delete".equals(path)) {
                jobAlertService.delete(candidateId, alertId);
                redirectWithSuccess(request, response, "/candidate/job-alerts", "Đã xóa thông báo việc làm.");
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/job-alerts", ex.getMessage());
        }
    }
}
