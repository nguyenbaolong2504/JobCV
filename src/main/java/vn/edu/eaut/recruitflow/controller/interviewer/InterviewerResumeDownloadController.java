package vn.edu.eaut.recruitflow.controller.interviewer;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.ResumeDownloadUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Interviewers may only download the CV linked to an interview assigned to them. */
@WebServlet("/interviewer/resumes/download")
public class InterviewerResumeDownloadController extends BaseController {
    private InterviewService interviewService;
    private ApplicationService applicationService;
    private ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
        applicationService = new ApplicationService();
        resumeService = new ResumeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int interviewId = RequestUtil.requiredPositiveInt(request, "interviewId", "Buổi phỏng vấn");
            int interviewerId = RequestUtil.currentUserId(request);
            Interview interview = interviewService.getForInterviewer(interviewId, interviewerId);
            Application application = applicationService.getForInterviewer(
                    interview.getApplicationId(), interviewerId);
            Resume resume = resumeService.getResumeForStaff(application.getResumeId());
            ResumeDownloadUtil.stream(getServletContext(), resume, response);
        } catch (BusinessException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
