package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/interviews/cancel")
public class InterviewCancelController extends BaseController {
    private InterviewService interviewService;

    @Override
    public void init() throws ServletException {
        interviewService = new InterviewService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int interviewId = RequestUtil.requiredPositiveInt(request, "id", "Lịch phỏng vấn");
            interviewService.cancel(interviewId, RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/hr/interviews", "Đã hủy lịch phỏng vấn.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/interviews", ex.getMessage());
        }
    }
}
