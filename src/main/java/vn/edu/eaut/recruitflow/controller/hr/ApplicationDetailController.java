package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.model.InterviewFeedback;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/hr/applications/detail")
public class ApplicationDetailController extends BaseController {
    private ApplicationService applicationService;
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        applicationService = new ApplicationService();
        interviewService = new InterviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int applicationId = RequestUtil.requiredPositiveInt(request, "id", "Đơn ứng tuyển");
            request.setAttribute("application", applicationService.getForHr(applicationId));
            request.setAttribute("history", applicationService.getHistory(applicationId));
            List<Interview> interviews = applicationService.getInterviews(applicationId);
            Map<Integer, InterviewFeedback> feedbackByInterview = new LinkedHashMap<>();
            for (Interview interview : interviews) {
                InterviewFeedback feedback = interviewService.getFeedback(interview.getId());
                if (feedback != null) {
                    feedbackByInterview.put(interview.getId(), feedback);
                }
            }
            request.setAttribute("interviews", interviews);
            request.setAttribute("feedbackByInterview", feedbackByInterview);
            var offer = applicationService.getOffer(applicationId);
            request.setAttribute("offers", offer == null ? List.of() : List.of(offer));
            view(request, response, "/WEB-INF/views/hr/application-detail.jsp", "Hồ sơ ứng viên | RecruitFlow");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/applications", ex.getMessage());
        }
    }
}
