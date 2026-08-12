package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.service.NotificationService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Candidate-owned notification list and read state updates. */
@WebServlet(name = "CandidateNotificationController", urlPatterns = {
        "/candidate/notifications",
        "/candidate/notifications/read",
        "/candidate/notifications/read-all"
})
public class CandidateNotificationController extends CandidateBaseController {
    private static final int NOTIFICATION_PAGE_SIZE = 50;

    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/notifications".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("notifications", List.of());
        try {
            int candidateId = currentCandidateId(request);
            request.setAttribute("notifications", notificationService.getNotifications(
                    candidateId, RequestUtil.page(request), NOTIFICATION_PAGE_SIZE).getItems());
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/notifications.jsp", "Thông báo | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        String path = request.getServletPath();
        try {
            int candidateId = currentCandidateId(request);
            if ("/candidate/notifications/read".equals(path)) {
                int notificationId = RequestUtil.requiredPositiveInt(request, "notificationId", "Thông báo");
                notificationService.markRead(candidateId, notificationId);
                redirectWithSuccess(request, response, "/candidate/notifications", "Đã đánh dấu thông báo là đã đọc.");
                return;
            }
            if ("/candidate/notifications/read-all".equals(path)) {
                notificationService.markAllRead(candidateId);
                redirectWithSuccess(request, response, "/candidate/notifications", "Đã đánh dấu tất cả thông báo là đã đọc.");
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/candidate/notifications", ex.getMessage());
        }
    }
}
