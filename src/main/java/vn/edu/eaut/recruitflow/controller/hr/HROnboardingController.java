package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.OnboardingService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/onboarding")
public class HROnboardingController extends BaseController {
    private OnboardingService onboardingService;

    @Override
    public void init() throws ServletException {
        onboardingService = new OnboardingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int actorId = RequestUtil.currentUserId(request);
            request.setAttribute("onboardings", onboardingService.findForHr(actorId));
            String onboardingId = RequestUtil.text(request, "id");
            if (!onboardingId.isEmpty()) {
                request.setAttribute("tasks", onboardingService.getTasksForHr(
                        RequestUtil.requiredPositiveInt(request, "id", "Quy trình tiếp nhận"), actorId));
            }
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/onboarding.jsp", "Tiếp nhận nhân sự | RecruitFlow");
    }
}
