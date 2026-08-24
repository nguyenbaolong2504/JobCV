package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/hr/applications")
public class HRApplicationController extends BaseController {
    private ApplicationService applicationService;
    private JobService jobService;

    @Override
    public void init() throws ServletException {
        applicationService = new ApplicationService();
        jobService = new JobService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            boolean boardView = !"list".equalsIgnoreCase(RequestUtil.text(request, "view"));
            ApplicationStatus status = optionalStatus(RequestUtil.text(request, "status"));
            PageResult<Application> applicationPage = applicationService.searchForHr(
                    RequestUtil.text(request, "keyword"),
                    optionalPositiveInt(request, "jobId", "Tin tuyển dụng"),
                    status == null ? null : status.name(),
                    optionalDecimal(request, "minMatchScore", "Match score"),
                    boardView ? 1 : RequestUtil.page(request),
                    boardView ? 500 : RequestUtil.pageSize(request),
                    RequestUtil.currentUserId(request)
            );
            request.setAttribute("boardView", boardView);
            request.setAttribute("applicationPage", applicationPage);
            request.setAttribute("jobs", jobService.searchForHr(
                    null, null, null, null, null, "newest", 1, 100,
                    RequestUtil.currentUserId(request)).getItems());
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/applications.jsp", "Đơn ứng tuyển | RecruitFlow");
    }

    private Integer optionalPositiveInt(HttpServletRequest request, String field, String label) throws BusinessException {
        return RequestUtil.text(request, field).isEmpty() ? null : RequestUtil.requiredPositiveInt(request, field, label);
    }

    private BigDecimal optionalDecimal(HttpServletRequest request, String field, String label) throws BusinessException {
        return RequestUtil.text(request, field).isEmpty() ? null : RequestUtil.decimal(request, field, label);
    }

    private ApplicationStatus optionalStatus(String value) {
        return value.isEmpty() ? null : ApplicationStatus.fromValue(value);
    }
}
