package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.Notification;
import vn.edu.eaut.recruitflow.service.NotificationService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/** Notification inbox for HR (and Admin in the HR workspace) and assigned interviewers. */
@WebServlet(name = "StaffNotificationController", urlPatterns = {
        "/hr/notifications", "/hr/notifications/read", "/hr/notifications/read-all",
        "/interviewer/notifications", "/interviewer/notifications/read", "/interviewer/notifications/read-all"
})
public class StaffNotificationController extends BaseController {
    private static final int PAGE_SIZE = 20;
    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Portal portal = portalFor(request, response);
        if (portal == null) {
            return;
        }
        if (!portal.listPath.equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("notifications", List.of());
        try {
            PageResult<Notification> page = notificationService.getNotifications(
                    RequestUtil.currentUserId(request), RequestUtil.page(request), PAGE_SIZE);
            request.setAttribute("notifications", page.getItems());
            request.setAttribute("page", page);
        } catch (BusinessException exception) {
            request.setAttribute("error", exception.getMessage());
        }
        request.setAttribute("notificationPortal", portal.name());
        view(request, response, "/WEB-INF/views/common/staff-notifications.jsp", "Thông báo | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Portal portal = portalFor(request, response);
        if (portal == null) {
            return;
        }
        try {
            int userId = RequestUtil.currentUserId(request);
            String path = request.getServletPath();
            if (portal.readPath.equals(path)) {
                notificationService.markRead(userId,
                        RequestUtil.requiredPositiveInt(request, "notificationId", "Thông báo"));
                redirectWithSuccess(request, response, portal.listPath, "Đã đánh dấu thông báo là đã đọc.");
                return;
            }
            if (portal.readAllPath.equals(path)) {
                notificationService.markAllRead(userId);
                redirectWithSuccess(request, response, portal.listPath, "Đã đánh dấu tất cả thông báo là đã đọc.");
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (BusinessException exception) {
            redirectWithError(request, response, portal.listPath, exception.getMessage());
        }
    }

    private Portal portalFor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String role = session != null && session.getAttribute("role") instanceof String
                ? (String) session.getAttribute("role") : "";
        String path = request.getServletPath();
        if (path.startsWith("/hr/") && ("HR".equals(role) || "ADMIN".equals(role))) {
            return Portal.HR;
        }
        if (path.startsWith("/interviewer/") && "INTERVIEWER".equals(role)) {
            return Portal.INTERVIEWER;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }

    private enum Portal {
        HR("/hr/notifications", "/hr/notifications/read", "/hr/notifications/read-all"),
        INTERVIEWER("/interviewer/notifications", "/interviewer/notifications/read", "/interviewer/notifications/read-all");

        private final String listPath;
        private final String readPath;
        private final String readAllPath;

        Portal(String listPath, String readPath, String readAllPath) {
            this.listPath = listPath;
            this.readPath = readPath;
            this.readAllPath = readAllPath;
        }
    }
}
