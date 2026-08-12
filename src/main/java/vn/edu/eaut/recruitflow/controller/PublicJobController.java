package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/** Serves only publicly visible, published job advertisements. */
@WebServlet(name = "PublicJobController", urlPatterns = {"/jobs", "/jobs/detail"})
public class PublicJobController extends BaseController {
    private static final Set<String> ALLOWED_SORTS = Set.of("newest", "deadline", "salary");
    private static final int MAX_KEYWORD_LENGTH = 150;
    private static final int MAX_LOCATION_LENGTH = 100;

    private JobService jobService;
    private DepartmentService departmentService;
    private MatchingService matchingService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
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
            String keyword = validateSearchText(RequestUtil.text(request, "keyword"), "Từ khóa", MAX_KEYWORD_LENGTH);
            Integer departmentId = optionalPositiveInt(request, "departmentId", "Phòng ban");
            String location = validateSearchText(RequestUtil.text(request, "location"), "Địa điểm", MAX_LOCATION_LENGTH);
            String employmentType = optionalEmploymentType(RequestUtil.text(request, "employmentType"));
            String sort = allowedSort(RequestUtil.text(request, "sort"));
            int page = RequestUtil.page(request);
            int pageSize = RequestUtil.pageSize(request);

            // The service contract performs a published-only search; no general job query is used here.
            PageResult<Job> pageResult = jobService.searchPublishedJobs(
                    keyword, departmentId, location, employmentType, page, pageSize, sort);

            request.setAttribute("page", pageResult);
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("keyword", keyword);
            request.setAttribute("departmentId", departmentId);
            request.setAttribute("location", location);
            request.setAttribute("employmentType", employmentType);
            request.setAttribute("sort", sort);
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
            // This service method is intentionally public-only; it will not return draft, closed, or archived jobs.
            Job job = jobService.getPublishedJobById(jobId);
            if (job == null || !JobStatus.PUBLISHED.name().equals(job.getStatus())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            request.setAttribute("job", job);
            request.setAttribute("skills", jobService.getSkills(jobId));
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

    private Integer optionalPositiveInt(HttpServletRequest request, String parameter, String label)
            throws BusinessException {
        return RequestUtil.text(request, parameter).isEmpty()
                ? null
                : RequestUtil.requiredPositiveInt(request, parameter, label);
    }

    private String optionalEmploymentType(String rawEmploymentType) throws BusinessException {
        if (rawEmploymentType == null || rawEmploymentType.isEmpty()) {
            return null;
        }
        String normalized = rawEmploymentType.trim().toUpperCase(Locale.ROOT);
        try {
            return EmploymentType.valueOf(normalized).name();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Loại hình làm việc không hợp lệ.");
        }
    }

    private String allowedSort(String rawSort) {
        String normalized = rawSort == null ? "" : rawSort.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_SORTS.contains(normalized) ? normalized : "newest";
    }

    private String validateSearchText(String value, String label, int maxLength) throws BusinessException {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(label + " không được vượt quá " + maxLength + " ký tự.");
        }
        return normalized;
    }
}
