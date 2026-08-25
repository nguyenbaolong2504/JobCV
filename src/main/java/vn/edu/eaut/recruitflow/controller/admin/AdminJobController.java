package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/jobs")
public class AdminJobController extends BaseController {
    private JobService jobService;
    private ApplicationService applicationService;
    @Override public void init() { jobService = new JobService(); applicationService = new ApplicationService(); }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JobStatus status = RequestUtil.text(request, "status").isEmpty() ? null : JobStatus.fromValue(RequestUtil.text(request, "status"));
            var page = jobService.searchForHr(RequestUtil.text(request, "keyword"), null, RequestUtil.text(request, "location"),
                    null, status, RequestUtil.text(request, "sort"), RequestUtil.page(request), RequestUtil.pageSize(request),
                    RequestUtil.currentUserId(request));
            request.setAttribute("jobs", page.getItems());
            request.setAttribute("page", page);
            request.setAttribute("applicationCounts", applicationService.countByJobForHr(RequestUtil.currentUserId(request)));
        } catch (BusinessException | IllegalArgumentException exception) { request.setAttribute("error", exception.getMessage()); }
        view(request, response, "/WEB-INF/views/admin/jobs.jsp", "Quản trị tin tuyển dụng | RecruitFlow");
    }
}
