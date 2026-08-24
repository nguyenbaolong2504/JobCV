package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.InterviewStatus;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/hr/interviews")
public class HRInterviewController extends BaseController {
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Date date = RequestUtil.text(request, "date").isEmpty()
                    ? null : Date.valueOf(RequestUtil.date(request, "date", "Ngày phỏng vấn"));
            String status = RequestUtil.text(request, "status").isEmpty()
                    ? null : InterviewStatus.fromValue(RequestUtil.text(request, "status")).name();
            request.setAttribute("interviews", interviewService.searchForHr(
                    RequestUtil.text(request, "keyword"), date, status,
                    RequestUtil.currentUserId(request)));
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/interviews.jsp", "Lịch phỏng vấn | RecruitFlow");
    }
}
