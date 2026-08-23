package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Public recruitment portal landing page. */
@WebServlet(name = "HomeController", urlPatterns = "/home")
public class HomeController extends BaseController {
    private static final int FEATURED_JOB_LIMIT = 6;

    private JobService jobService;
    private DepartmentService departmentService;
    private JobCategoryService jobCategoryService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        jobCategoryService = new JobCategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            request.setAttribute("featuredJobs", jobService.getFeaturedPublishedJobs(FEATURED_JOB_LIMIT));
            request.setAttribute("departments", departmentService.getAllDepartments());
        } catch (BusinessException ex) {
            request.setAttribute("featuredJobs", List.of());
            request.setAttribute("departments", List.of());
            request.setAttribute("error", ex.getMessage());
        }
        try {
            request.setAttribute("categoryRoots", jobCategoryService.getPublicHierarchy());
        } catch (BusinessException ignored) {
            // The taxonomy is optional metadata. A delayed migration must not hide core public jobs.
            request.setAttribute("categoryRoots", List.of());
        }
        view(request, response, "/WEB-INF/views/public/home.jsp", "RecruitFlow | Tuyển dụng và Onboarding");
    }
}
