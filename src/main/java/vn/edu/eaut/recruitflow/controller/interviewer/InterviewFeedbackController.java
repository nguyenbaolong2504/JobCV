package vn.edu.eaut.recruitflow.controller.interviewer;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.Recommendation;
import vn.edu.eaut.recruitflow.model.InterviewFeedback;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/interviewer/interviews/feedback")
public class InterviewFeedbackController extends BaseController {
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int interviewId = RequestUtil.requiredPositiveInt(request, "interviewId", "Buổi phỏng vấn");
            InterviewFeedback feedback = new InterviewFeedback();
            feedback.setTechnicalScore(RequestUtil.decimal(request, "technicalScore", "Điểm kỹ thuật"));
            feedback.setCommunicationScore(RequestUtil.decimal(request, "communicationScore", "Điểm giao tiếp"));
            feedback.setExperienceScore(RequestUtil.decimal(request, "experienceScore", "Điểm kinh nghiệm"));
            feedback.setAttitudeScore(RequestUtil.decimal(request, "attitudeScore", "Điểm thái độ"));
            feedback.setComment(RequestUtil.text(request, "comment"));
            feedback.setRecommendation(Recommendation.fromValue(RequestUtil.text(request, "recommendation")).name());
            interviewService.submitFeedback(interviewId, RequestUtil.currentUserId(request), feedback);
            redirectWithSuccess(request, response, "/interviewer/interviews", "Đã gửi feedback phỏng vấn.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/interviewer/interviews", ex.getMessage());
        }
    }
}
