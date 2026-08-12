package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Lists interview schedules belonging to the signed-in candidate. */
@WebServlet(name = "CandidateInterviewController", urlPatterns = "/candidate/interviews")
public class CandidateInterviewController extends CandidateBaseController {
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        request.setAttribute("interviews", List.of());
        try {
            request.setAttribute("interviews", interviewService.findForCandidate(currentCandidateId(request)));
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/interviews.jsp", "Lịch phỏng vấn | RecruitFlow");
    }
}
