package vn.edu.eaut.recruitflow.controller.interviewer;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/interviewer/interviews/detail")
public class InterviewerInterviewDetailController extends BaseController {
    private InterviewService interviewService;
    private ApplicationService applicationService;
    private CandidateProfileService candidateProfileService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
        applicationService = new ApplicationService();
        candidateProfileService = new CandidateProfileService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int interviewId = RequestUtil.requiredPositiveInt(request, "id", "Buổi phỏng vấn");
            int interviewerId = RequestUtil.currentUserId(request);
            Interview interview = interviewService.getForInterviewer(interviewId, interviewerId);
            request.setAttribute("interview", interview);
            var application = applicationService.getForHr(interview.getApplicationId());
            request.setAttribute("application", application);
            try {
                request.setAttribute("profile", candidateProfileService.getProfile(application.getCandidateId()));
            } catch (BusinessException ignored) {
                request.setAttribute("profile", new CandidateProfile());
            }
            view(request, response, "/WEB-INF/views/interviewer/interview-detail.jsp", "Chi tiết phỏng vấn | RecruitFlow");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/interviewer/interviews", ex.getMessage());
        }
    }
}
