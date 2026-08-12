package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/interviews/edit")
public class InterviewEditController extends BaseController {
    private InterviewService interviewService;
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
        applicationService = new ApplicationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int interviewId = RequestUtil.requiredPositiveInt(request, "id", "Lịch phỏng vấn");
            request.setAttribute("interview", interviewService.getForHr(interviewId));
            request.setAttribute("applications", applicationService.findShortlisted());
            request.setAttribute("interviewers", interviewService.getInterviewers());
            view(request, response, "/WEB-INF/views/hr/interview-form.jsp", "Đổi lịch phỏng vấn | RecruitFlow");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/interviews", ex.getMessage());
        }
    }
}
