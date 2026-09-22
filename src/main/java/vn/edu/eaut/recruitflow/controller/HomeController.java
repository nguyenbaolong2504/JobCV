package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.service.DashboardService;
import vn.edu.eaut.recruitflow.service.JobAlertService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.service.SavedJobService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.service.HomeBannerService;
import vn.edu.eaut.recruitflow.model.CandidateDashboardStats;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobMatchResult;
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

/** Public recruitment portal landing page. */
@WebServlet(name = "HomeController", urlPatterns = "/home")
public class HomeController extends BaseController {
    private static final int FEATURED_JOB_LIMIT = 12;
    private static final int POPULAR_JOB_LIMIT = 6;
    private static final int RECOMMENDED_JOB_LIMIT = 4;
    private static final int OTHER_JOB_LIMIT = 6;

    private JobService jobService;
    private DepartmentService departmentService;
    private DashboardService dashboardService;
    private MatchingService matchingService;
    private SavedJobService savedJobService;
    private JobAlertService jobAlertService;
    private ResumeService resumeService;
    private CompanyProfileService companyService;
    private HomeBannerService homeBannerService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        dashboardService = new DashboardService();
        matchingService = new MatchingService();
        savedJobService = new SavedJobService();
        jobAlertService = new JobAlertService();
        resumeService = new ResumeService();
        companyService = new CompanyProfileService();
        homeBannerService = new HomeBannerService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.setAttribute("candidateHome", false);
        request.setAttribute("candidateStats", new CandidateDashboardStats());
        request.setAttribute("candidateRecommendedJobs", List.of());
        request.setAttribute("candidateOtherJobs", List.of());
        request.setAttribute("candidateSavedJobIds", Set.of());
        request.setAttribute("candidateSavedJobCount", 0L);
        request.setAttribute("candidateJobAlertCount", 0L);
        request.setAttribute("candidateHasResume", false);
        request.setAttribute("publishedJobCount", 0L);
        request.setAttribute("employers", List.of());
        request.setAttribute("homeBanners", List.of());
        request.setAttribute("popularJobs", List.of());

        try {
            request.setAttribute("featuredJobs", jobService.getFeaturedPublishedJobs(FEATURED_JOB_LIMIT));
            request.setAttribute("popularJobs", jobService.getPopularPublishedJobs(POPULAR_JOB_LIMIT));
            request.setAttribute("publishedJobCount", jobService.countPublishedJobs());
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("employers", companyService.getCompanies(""));
            request.setAttribute("homeBanners", homeBannerService.getActive());
        } catch (BusinessException ex) {
            request.setAttribute("featuredJobs", List.of());
            request.setAttribute("departments", List.of());
            request.setAttribute("homeBanners", List.of());
            request.setAttribute("error", ex.getMessage());
        }
        attachCandidateHome(request);
        view(request, response, "/WEB-INF/views/public/home.jsp", "JobCV | Tuyển dụng và tiếp nhận nhân sự");
    }

    private void attachCandidateHome(HttpServletRequest request) {
        if (request.getSession(false) == null
                || !"CANDIDATE".equals(request.getSession(false).getAttribute("role"))) {
            return;
        }
        request.setAttribute("candidateHome", true);
        try {
            int candidateId = RequestUtil.currentUserId(request);
            request.setAttribute("candidateStats", dashboardService.getCandidateDashboardStats(candidateId));
            List<JobMatchResult> recommended = matchingService.recommend(candidateId, RECOMMENDED_JOB_LIMIT);
            request.setAttribute("candidateRecommendedJobs", recommended);
            Set<Integer> recommendedIds = recommended.stream().map(item -> item.getJob().getId()).collect(Collectors.toSet());
            List<Job> otherJobs = jobService.getFeaturedPublishedJobs(FEATURED_JOB_LIMIT).stream()
                    .filter(job -> !recommendedIds.contains(job.getId())).limit(OTHER_JOB_LIMIT).toList();
            request.setAttribute("candidateOtherJobs", otherJobs);
            request.setAttribute("candidateSavedJobIds", savedJobService.getSavedJobIds(candidateId));
            request.setAttribute("candidateSavedJobCount", savedJobService.count(candidateId));
            request.setAttribute("candidateJobAlertCount", jobAlertService.countActive(candidateId));
            request.setAttribute("candidateHasResume",
                    !ResumeStorageUtil.availableResumes(getServletContext(), resumeService.getResumes(candidateId)).isEmpty());
        } catch (BusinessException ex) {
            request.setAttribute("candidateHomeError", ex.getMessage());
        }
    }
}
