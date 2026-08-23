package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.JobMatchResult;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.JobSearchCriteriaFactory;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Published-job search annotated with the current candidate's CV match information. */
@WebServlet(name = "CandidateJobController", urlPatterns = "/candidate/jobs")
public class CandidateJobController extends CandidateBaseController {
    private static final Set<String> ALLOWED_SORTS = Set.of("newest", "deadline", "salary", "experience");

    private MatchingService matchingService;
    private DepartmentService departmentService;
    private JobCategoryService jobCategoryService;

    @Override
    public void init() throws ServletException {
        matchingService = new MatchingService();
        departmentService = new DepartmentService();
        jobCategoryService = new JobCategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        int page = RequestUtil.page(request);
        int pageSize = RequestUtil.pageSize(request);
        request.setAttribute("page", new PageResult<JobMatchResult>(List.of(), page, pageSize, 0));
        request.setAttribute("departments", List.of());
        request.setAttribute("jobCategories", List.of());

        try {
            int candidateId = currentCandidateId(request);
            JobSearchCriteria criteria = JobSearchCriteriaFactory.fromRequest(request);
            String sort = safeSort(request, ALLOWED_SORTS, "newest");

            request.setAttribute("page", matchingService.searchPublishedJobsForCandidate(
                    candidateId, criteria, page, pageSize, sort));
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("criteria", criteria);
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            try {
                request.setAttribute("departments", departmentService.getAllDepartments());
            } catch (BusinessException ignored) {
                // Keep the page usable with an empty department filter when the lookup is unavailable.
            }
        }

        try {
            request.setAttribute("jobCategories", jobCategoryService.getActiveLeafCategories());
        } catch (BusinessException ignored) {
            // Categories enrich the search form but should not remove core candidate job discovery.
        }

        view(request, response, "/WEB-INF/views/candidate/jobs.jsp", "Tìm việc làm | RecruitFlow");
    }
}
