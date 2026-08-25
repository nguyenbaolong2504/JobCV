package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.service.SavedJobService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.JobSearchCriteriaFactory;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.SimpleJson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.Locale;

/** Serves only publicly visible, published job advertisements. */
@WebServlet(name = "PublicJobController", urlPatterns = {"/jobs", "/jobs/detail", "/jobs/suggestions"})
public class PublicJobController extends BaseController {
    private static final Set<String> ALLOWED_SORTS = Set.of("newest", "deadline", "salary", "experience");

    private JobService jobService;
    private DepartmentService departmentService;
    private MatchingService matchingService;
    private SavedJobService savedJobService;
    private ResumeService resumeService;
    private ApplicationService applicationService;
    private CompanyProfileService companyService;
    private JobCategoryService categoryService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        matchingService = new MatchingService();
        savedJobService = new SavedJobService();
        resumeService = new ResumeService();
        applicationService = new ApplicationService();
        companyService = new CompanyProfileService();
        categoryService = new JobCategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if ("/jobs/suggestions".equals(request.getServletPath())) {
            showSuggestions(request, response);
            return;
        }
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

            PageResult<Job> pageResult = jobService.searchPublishedJobs(criteria, page, pageSize, sort);

            request.setAttribute("page", pageResult);
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("categories", categoryService.getPublicHierarchy());
            request.setAttribute("criteria", criteria);
            request.setAttribute("keyword", criteria.getKeyword());
            request.setAttribute("title", criteria.getTitle());
            request.setAttribute("departmentId", criteria.getDepartmentId());
            request.setAttribute("categoryId", criteria.getCategoryId());
            request.setAttribute("location", criteria.getLocation());
            request.setAttribute("employmentType", criteria.getEmploymentType());
            request.setAttribute("salaryMin", criteria.getSalaryMin());
            request.setAttribute("salaryMax", criteria.getSalaryMax());
            request.setAttribute("experienceMin", criteria.getExperienceMin());
            request.setAttribute("experienceMax", criteria.getExperienceMax());
            request.setAttribute("sort", sort);
            request.setAttribute("savedJobIds", currentCandidateSavedJobIds(request));
            request.setAttribute("pageDescription", "Tìm kiếm việc làm theo vị trí, kỹ năng, địa điểm, mức lương và kinh nghiệm trên RecruitFlow.");
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
            request.setAttribute("jobExpired", job.getDeadline() != null
                    && job.getDeadline().toLocalDate().isBefore(LocalDate.now()));
            request.setAttribute("skills", jobService.getSkills(jobId));
            var company = companyService.getCompanyByOwner(job.getCreatedBy());
            request.setAttribute("company", company);
            List<Job> relatedJobs = jobService.searchPublishedJobs(null, job.getDepartmentId(), null, null, 1, 5, "newest").getItems();
            request.setAttribute("relatedJobs", relatedJobs.stream().filter(item -> item.getId() != jobId).limit(4).toList());
            request.setAttribute("sameCompanyJobs", companyService.getOpenJobs(company.getId()).stream()
                    .filter(item -> item.getId() != jobId).limit(4).toList());
            request.setAttribute("jobSaved", false);
            attachCandidateMatchWhenAvailable(request, jobId);
            request.setAttribute("pageDescription", "Ứng tuyển " + job.getTitle() + " tại " + company.getName()
                    + ". Xem mức lương, yêu cầu, địa điểm và hạn nộp hồ sơ.");
            view(request, response, "/WEB-INF/views/public/job-detail.jsp", job.getTitle() + " | RecruitFlow");
        } catch (BusinessException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showSuggestions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "private, max-age=60");
        try {
            List<Map<String, Object>> suggestions = jobService
                    .getSearchSuggestions(RequestUtil.text(request, "q"), 8).stream()
                    .map(label -> Map.<String, Object>of("label", label, "value", label))
                    .toList();
            response.getWriter().write(SimpleJson.stringify(Map.of("suggestions", suggestions)));
        } catch (BusinessException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(SimpleJson.stringify(Map.of("suggestions", List.of())));
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
            request.setAttribute("jobSaved", savedJobService.isSaved(candidateId, jobId));
            request.setAttribute("candidateResumes", resumeService.getResumes(candidateId));
            request.setAttribute("alreadyApplied", applicationService.hasApplied(candidateId, jobId));
        } catch (BusinessException ignored) {
            // A public job page remains usable even when a session expires between filter and controller.
        }
    }

    private Set<Integer> currentCandidateSavedJobIds(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !"CANDIDATE".equals(session.getAttribute("role"))) {
            return Set.of();
        }
        try {
            return savedJobService.getSavedJobIds(RequestUtil.currentUserId(request));
        } catch (BusinessException exception) {
            return Set.of();
        }
    }

    private String allowedSort(String rawSort) {
        String normalized = rawSort == null ? "" : rawSort.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_SORTS.contains(normalized) ? normalized : "newest";
    }

}
