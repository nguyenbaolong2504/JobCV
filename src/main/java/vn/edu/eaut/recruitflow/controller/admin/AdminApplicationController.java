package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/applications")
public class AdminApplicationController extends BaseController {
    private ApplicationService applicationService;
    @Override public void init() { applicationService = new ApplicationService(); }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ApplicationStatus status = RequestUtil.text(request, "status").isEmpty() ? null : ApplicationStatus.fromValue(RequestUtil.text(request, "status"));
            var page = applicationService.searchForHr(RequestUtil.text(request, "keyword"), null,
                    status == null ? null : status.name(), null, RequestUtil.page(request), RequestUtil.pageSize(request),
                    RequestUtil.currentUserId(request));
            request.setAttribute("applications", page.getItems());
            request.setAttribute("page", page);
        } catch (BusinessException | IllegalArgumentException exception) { request.setAttribute("error", exception.getMessage()); }
        view(request, response, "/WEB-INF/views/admin/applications.jsp", "Quản trị hồ sơ ứng tuyển | RecruitFlow");
    }
}
