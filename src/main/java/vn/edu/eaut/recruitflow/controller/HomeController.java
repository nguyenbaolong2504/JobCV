package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.service.DashboardService;
import vn.edu.eaut.recruitflow.service.JobAlertService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.service.SavedJobService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.service.NotificationService;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.model.CandidateDashboardStats;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Public recruitment portal landing page. */
@WebServlet(name = "HomeController", urlPatterns = "/home")
public class HomeController extends BaseController {
    private static final int FEATURED_JOB_LIMIT = 12;

    private JobService jobService;
    private DepartmentService departmentService;
    private DashboardService dashboardService;
    private MatchingService matchingService;
    private SavedJobService savedJobService;
    private JobAlertService jobAlertService;
    private ResumeService resumeService;
    private JobCategoryService categoryService;
    private NotificationService notificationService;
    private CandidateProfileService candidateProfileService;
    private CompanyProfileService companyProfileService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        dashboardService = new DashboardService();
        matchingService = new MatchingService();
        savedJobService = new SavedJobService();
        jobAlertService = new JobAlertService();
        resumeService = new ResumeService();
        categoryService = new JobCategoryService();
        notificationService = new NotificationService();
        candidateProfileService = new CandidateProfileService();
        companyProfileService = new CompanyProfileService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.setAttribute("candidateHome", false);
        request.setAttribute("candidateStats", new CandidateDashboardStats());
        request.setAttribute("candidateJobs", List.of());
        request.setAttribute("candidateSavedJobIds", Set.of());
        request.setAttribute("candidateSavedJobCount", 0L);
        request.setAttribute("candidateJobAlertCount", 0L);
        request.setAttribute("candidateHasResume", false);
        request.setAttribute("publishedJobCount", 0L);

        try {
            request.setAttribute("featuredJobs", jobService.getFeaturedPublishedJobs(FEATURED_JOB_LIMIT));
            request.setAttribute("publishedJobCount", jobService.countPublishedJobs());
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("departmentJobCounts", jobService.countPublishedJobsByDepartment());
            request.setAttribute("popularKeywords", jobService.getPopularKeywords(8));
            request.setAttribute("categories", categoryService.getPublicHierarchy());
            request.setAttribute("featuredCompanies", companyProfileService.getFeaturedCompanies(10));
        } catch (BusinessException ex) {
            request.setAttribute("featuredJobs", List.of());
            request.setAttribute("departments", List.of());
            request.setAttribute("popularKeywords", List.of());
            request.setAttribute("featuredCompanies", List.of());
            request.setAttribute("error", ex.getMessage());
        }
        request.setAttribute("pageDescription", "Tìm việc làm, quản lý CV và theo dõi hành trình ứng tuyển minh bạch trên RecruitFlow.");
        attachCandidateHome(request);
        view(request, response, "/WEB-INF/views/public/home.jsp", "RecruitFlow | Tuyển dụng và tiếp nhận nhân sự");
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
            request.setAttribute("candidateJobs", matchingService.searchPublishedJobsForCandidate(
                    candidateId, "", null, "", null, 1, FEATURED_JOB_LIMIT, "newest").getItems());
            request.setAttribute("candidateSavedJobIds", savedJobService.getSavedJobIds(candidateId));
            request.setAttribute("candidateSavedJobCount", savedJobService.count(candidateId));
            request.setAttribute("candidateJobAlertCount", jobAlertService.countActive(candidateId));
            request.setAttribute("candidateHasResume", !resumeService.getResumes(candidateId).isEmpty());
            request.setAttribute("unreadNotificationCount", notificationService.unreadCount(candidateId));
            request.setAttribute("layoutCandidateProfile", candidateProfileService.getProfile(candidateId));
        } catch (BusinessException ex) {
            request.setAttribute("candidateHomeError", ex.getMessage());
        }
    }
}
