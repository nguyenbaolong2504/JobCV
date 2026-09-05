package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.CandidateDashboardStats;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.DashboardService;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.service.MatchingService;
import vn.edu.eaut.recruitflow.service.NotificationService;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Candidate overview with only data scoped to the current candidate. */
@WebServlet(name = "CandidateDashboardController", urlPatterns = "/candidate/dashboard")
public class CandidateDashboardController extends CandidateBaseController {
    private static final int DASHBOARD_LIMIT = 5;

    private DashboardService dashboardService;
    private MatchingService matchingService;
    private ApplicationService applicationService;
    private InterviewService interviewService;
    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        dashboardService = new DashboardService();
        matchingService = new MatchingService();
        applicationService = new ApplicationService();
        interviewService = new InterviewService();
        notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        applyEmptyDashboard(request);

        try {
            int candidateId = currentCandidateId(request);
            request.setAttribute("stats", dashboardService.getCandidateDashboardStats(candidateId));
            request.setAttribute("recommendedJobs", matchingService.recommend(candidateId, DASHBOARD_LIMIT));
            request.setAttribute("recentApplications", applicationService.searchForCandidate(
                    candidateId, "", (ApplicationStatus) null, 1, DASHBOARD_LIMIT).getItems());
            request.setAttribute("upcomingInterviews", interviewService.findUpcomingForCandidate(candidateId).stream()
                    .limit(DASHBOARD_LIMIT)
                    .toList());
            request.setAttribute("notifications", notificationService.getNotifications(
                    candidateId, 1, DASHBOARD_LIMIT).getItems());
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }

        view(request, response, "/WEB-INF/views/candidate/dashboard.jsp", "Bảng điều khiển ứng viên | JobCV");
    }

    private void applyEmptyDashboard(HttpServletRequest request) {
        request.setAttribute("stats", new CandidateDashboardStats());
        request.setAttribute("recommendedJobs", List.of());
        request.setAttribute("recentApplications", List.of());
        request.setAttribute("upcomingInterviews", List.of());
        request.setAttribute("notifications", List.of());
    }
}
