package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.service.NotificationService;
import vn.edu.eaut.recruitflow.service.SavedJobService;
import vn.edu.eaut.recruitflow.service.JobAlertService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/** Common request/session safeguards for the candidate-only controller area. */
abstract class CandidateBaseController extends BaseController {
    private final NotificationService layoutNotificationService = new NotificationService();
    private final CandidateProfileService layoutProfileService = new CandidateProfileService();
    private final SavedJobService layoutSavedJobService = new SavedJobService();
    private final JobAlertService layoutJobAlertService = new JobAlertService();

    protected int currentCandidateId(HttpServletRequest request) throws BusinessException {
        HttpSession session = request.getSession(false);
        if (session == null || !"CANDIDATE".equals(session.getAttribute("role"))) {
            throw new BusinessException("Phiên đăng nhập ứng viên không hợp lệ.");
        }
        return RequestUtil.currentUserId(request);
    }

    protected void useUtf8(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
    }

    protected Integer optionalPositiveInt(HttpServletRequest request, String field, String label)
            throws BusinessException {
        return RequestUtil.text(request, field).isEmpty()
                ? null
                : RequestUtil.requiredPositiveInt(request, field, label);
    }

    protected String boundedText(HttpServletRequest request, String field, String label, int maximumLength)
            throws BusinessException {
        String value = RequestUtil.text(request, field);
        if (value.length() > maximumLength) {
            throw new BusinessException(label + " không được vượt quá " + maximumLength + " ký tự.");
        }
        return value;
    }

    protected String safeSort(HttpServletRequest request, Set<String> allowedValues, String defaultValue) {
        String requested = RequestUtil.text(request, "sort").toLowerCase(Locale.ROOT);
        return allowedValues.contains(requested) ? requested : defaultValue;
    }

    @Override
    protected void view(HttpServletRequest request, HttpServletResponse response, String view, String pageTitle)
            throws ServletException, IOException {
        request.setAttribute("unreadNotificationCount", 0L);
        request.setAttribute("savedJobCount", 0L);
        request.setAttribute("jobAlertCount", 0L);
        request.setAttribute("layoutCandidateProfile", null);
        HttpSession session = request.getSession(false);
        if (session != null && "CANDIDATE".equals(session.getAttribute("role"))) {
            try {
                int candidateId = RequestUtil.currentUserId(request);
                try {
                    request.setAttribute("unreadNotificationCount", layoutNotificationService.unreadCount(candidateId));
                } catch (BusinessException ignored) {
                    // The notification badge is supplemental.
                }
                try {
                    request.setAttribute("savedJobCount", layoutSavedJobService.count(candidateId));
                } catch (BusinessException ignored) {
                    // The saved-job badge is supplemental.
                }
                try {
                    request.setAttribute("jobAlertCount", layoutJobAlertService.countActive(candidateId));
                } catch (BusinessException ignored) {
                    // The job-alert badge is supplemental.
                }
                try {
                    Object requestedProfile = request.getAttribute("profile");
                    CandidateProfile layoutProfile = requestedProfile instanceof CandidateProfile
                            && ((CandidateProfile) requestedProfile).getUserId() == candidateId
                            ? (CandidateProfile) requestedProfile
                            : layoutProfileService.getProfile(candidateId);
                    request.setAttribute("layoutCandidateProfile", layoutProfile);
                } catch (BusinessException ignored) {
                    // The avatar is supplemental.
                }
            } catch (BusinessException ignored) {
                // Layout decorations are supplemental and must never block the requested page.
            }
        }
        super.view(request, response, view, pageTitle);
    }
}
