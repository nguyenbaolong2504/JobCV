package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.InterviewType;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.InterviewService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;

@WebServlet("/hr/interviews/create")
public class InterviewCreateController extends BaseController {
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
            int actorId = RequestUtil.currentUserId(request);
            String applicationId = RequestUtil.text(request, "applicationId");
            if (!applicationId.isEmpty()) {
                request.setAttribute("application", applicationService.getForHr(
                        RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển"), actorId));
            }
            request.setAttribute("applications", applicationService.findShortlisted(actorId));
            request.setAttribute("interviewers", interviewService.getInterviewers(RequestUtil.currentUserId(request)));
            view(request, response, "/WEB-INF/views/hr/interview-form.jsp", "Lên lịch phỏng vấn | JobCV");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/interviews", ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Interview interview = bindInterview(request);
            interviewService.create(interview, RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/hr/interviews", "Đã lên lịch phỏng vấn và gửi thông báo.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/interviews/create", ex.getMessage());
        }
    }

    static Interview bindInterview(HttpServletRequest request) throws BusinessException {
        Time startTime = requiredTime(request, "startTime", "Giờ bắt đầu");
        Time endTime = requiredTime(request, "endTime", "Giờ kết thúc");
        if (!startTime.before(endTime)) {
            throw new BusinessException("Giờ bắt đầu phải trước giờ kết thúc.");
        }
        Interview interview = new Interview();
        interview.setApplicationId(RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển"));
        interview.setInterviewerId(RequestUtil.requiredPositiveInt(request, "interviewerId", "Interviewer"));
        interview.setInterviewType(InterviewType.fromValue(RequestUtil.text(request, "interviewType")).name());
        interview.setInterviewDate(Date.valueOf(RequestUtil.date(request, "interviewDate", "Ngày phỏng vấn")));
        interview.setStartTime(startTime);
        interview.setEndTime(endTime);
        interview.setLocation(RequestUtil.text(request, "location"));
        interview.setMeetingUrl(RequestUtil.text(request, "meetingUrl"));
        interview.setNote(RequestUtil.text(request, "note"));
        return interview;
    }

    private static Time requiredTime(HttpServletRequest request, String field, String label) throws BusinessException {
        try {
            String value = RequestUtil.text(request, field);
            if (value.isEmpty()) {
                throw new BusinessException(label + " là bắt buộc.");
            }
            return Time.valueOf(LocalTime.parse(value));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(label + " không hợp lệ.", ex);
        }
    }
}
