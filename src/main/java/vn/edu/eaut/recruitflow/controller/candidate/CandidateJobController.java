package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.JobMatchResult;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.service.SavedJobService;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.ResumeStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;

/** Published-job search annotated with the current candidate's CV match information. */
@WebServlet(name = "CandidateJobController", urlPatterns = "/candidate/jobs")
public class CandidateJobController extends CandidateBaseController {
    private static final Set<String> ALLOWED_SORTS = Set.of("newest", "deadline", "salary");

    private MatchingService matchingService;
    private DepartmentService departmentService;
    private SavedJobService savedJobService;
    private CompanyProfileService companyService;
    private ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        matchingService = new MatchingService();
        departmentService = new DepartmentService();
        savedJobService = new SavedJobService();
        companyService = new CompanyProfileService();
        resumeService = new ResumeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        int page = RequestUtil.page(request);
        int pageSize = RequestUtil.pageSize(request);
        request.setAttribute("page", new PageResult<JobMatchResult>(List.of(), page, pageSize, 0));
        request.setAttribute("departments", List.of());
        request.setAttribute("savedJobIds", Set.of());

        try {
            int candidateId = currentCandidateId(request);
            String keyword = boundedText(request, "keyword", "Từ khóa", 150);
            Integer departmentId = optionalPositiveInt(request, "departmentId", "Phòng ban");
            String location = boundedText(request, "location", "Địa điểm", 100);
            String employmentType = optionalEmploymentType(RequestUtil.text(request, "employmentType"));
            String sort = safeSort(request, ALLOWED_SORTS, "newest");

            request.setAttribute("page", matchingService.searchPublishedJobsForCandidate(
                    candidateId, keyword, departmentId, location, employmentType, page, pageSize, sort));
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("savedJobIds", savedJobService.getSavedJobIds(candidateId));
            request.setAttribute("companyById", companyService.getCompanies("").stream()
                    .collect(Collectors.toMap(company -> company.getId(), Function.identity(), (left, right) -> left)));
            request.setAttribute("hasCandidateResume",
                    !ResumeStorageUtil.availableResumes(getServletContext(), resumeService.getResumes(candidateId)).isEmpty());
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            try {
                request.setAttribute("departments", departmentService.getAllDepartments());
            } catch (BusinessException ignored) {
                // Keep the page usable with an empty department filter when the lookup is unavailable.
            }
        }

        view(request, response, "/WEB-INF/views/candidate/jobs.jsp", "Tìm việc làm | JobCV");
    }

    private String optionalEmploymentType(String rawEmploymentType) {
        return rawEmploymentType == null || rawEmploymentType.isEmpty()
                ? null
                : EmploymentType.fromValue(rawEmploymentType).name();
    }
}
