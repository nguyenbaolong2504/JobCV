package vn.edu.eaut.recruitflow.controller.interviewer;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/interviewer/interviews")
public class InterviewerInterviewController extends BaseController {
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("interviews",
                    interviewService.findForInterviewer(RequestUtil.currentUserId(request)));
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/login", ex.getMessage());
            return;
        }
        view(request, response, "/WEB-INF/views/interviewer/interviews.jsp", "Lịch phỏng vấn của tôi | JobCV");
    }
}
