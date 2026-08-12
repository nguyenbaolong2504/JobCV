package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.HRDashboardStats;
import vn.edu.eaut.recruitflow.service.DashboardService;
import vn.edu.eaut.recruitflow.service.InterviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/dashboard")
public class HRDashboardController extends BaseController {
    private DashboardService dashboardService;
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        dashboardService = new DashboardService();
        interviewService = new InterviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HRDashboardStats stats = dashboardService.getHrDashboardStats();
        request.setAttribute("hrStats", stats);
        request.setAttribute("funnel", stats.getRecruitmentFunnel());
        request.setAttribute("interviews", interviewService.findUpcomingForHr(5));
        view(request, response, "/WEB-INF/views/hr/dashboard.jsp", "HR Dashboard | RecruitFlow");
    }
}
