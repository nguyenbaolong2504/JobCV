package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.JobSearchCriteriaFactory;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.Set;

/** Serves only publicly visible, published job advertisements. */
@WebServlet(name = "PublicJobController", urlPatterns = {"/jobs", "/jobs/detail"})
public class PublicJobController extends BaseController {
    private static final Set<String> ALLOWED_SORTS = Set.of("newest", "deadline", "salary", "experience");

    private JobService jobService;
    private DepartmentService departmentService;
    private JobCategoryService jobCategoryService;
    private MatchingService matchingService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        jobCategoryService = new JobCategoryService();
        matchingService = new MatchingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if ("/jobs/detail".equals(request.getServletPath())) {
            showDetail(request, response);
            return;
        }
        showList(request, response);
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JobSearchCriteria criteria = JobSearchCriteriaFactory.fromRequest(request);
            String sort = allowedSort(RequestUtil.text(request, "sort"));
            int page = RequestUtil.page(request);
            int pageSize = RequestUtil.pageSize(request);

            // The service contract performs a currently-open, published-only search.
            PageResult<Job> pageResult = jobService.searchPublishedJobs(criteria, page, pageSize, sort);

            request.setAttribute("page", pageResult);
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("criteria", criteria);
            request.setAttribute("sort", sort);
            attachCategoryMetadata(request);
            view(request, response, "/WEB-INF/views/public/jobs.jsp", "Việc làm đang tuyển | RecruitFlow");
        } catch (BusinessException ex) {
            // Invalid query input must never reach DAO SQL construction.
            redirectWithError(request, response, "/jobs", ex.getMessage());
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final int jobId;
        try {
            jobId = RequestUtil.requiredPositiveInt(request, "id", "Mã công việc");
        } catch (BusinessException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            // This service method is intentionally public-only; it will not return expired, draft, closed, or archived jobs.
            Job job = jobService.getPublishedJobById(jobId);

            request.setAttribute("job", job);
            request.setAttribute("skills", job.getSkills());
            attachCategoryMenu(request);
            attachCandidateMatchWhenAvailable(request, jobId);
            view(request, response, "/WEB-INF/views/public/job-detail.jsp", "Chi tiết việc làm | RecruitFlow");
        } catch (BusinessException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void attachCandidateMatchWhenAvailable(HttpServletRequest request, int jobId) {
        HttpSession session = request.getSession(false);
        if (session == null || !"CANDIDATE".equals(session.getAttribute("role"))) {
            return;
        }

        try {
            int candidateId = RequestUtil.currentUserId(request);
            request.setAttribute("matchResult", matchingService.calculateForCandidate(candidateId, jobId));
        } catch (BusinessException ignored) {
            // A public job page remains usable even when a session expires between filter and controller.
        }
    }

    private void attachCategoryMenu(HttpServletRequest request) {
        try {
            request.setAttribute("categoryRoots", jobCategoryService.getPublicHierarchy());
        } catch (BusinessException ignored) {
            // A public job detail page is still useful when optional menu metadata is temporarily unavailable.
            request.setAttribute("categoryRoots", List.of());
        }
    }

    private void attachCategoryMetadata(HttpServletRequest request) {
        request.setAttribute("categoryRoots", List.of());
        request.setAttribute("jobCategories", List.of());
        try {
            request.setAttribute("categoryRoots", jobCategoryService.getPublicHierarchy());
            request.setAttribute("jobCategories", jobCategoryService.getActiveLeafCategories());
        } catch (BusinessException ignored) {
            // The job finder remains available even before the optional category migration is applied.
        }
    }

    private String allowedSort(String rawSort) {
        String normalized = rawSort == null ? "" : rawSort.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_SORTS.contains(normalized) ? normalized : "newest";
    }

}
