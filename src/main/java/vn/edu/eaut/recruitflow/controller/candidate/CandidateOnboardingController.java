package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.service.OnboardingService;
import vn.edu.eaut.recruitflow.model.Onboarding;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Candidate onboarding checklist and task completion action. */
@WebServlet(name = "CandidateOnboardingController", urlPatterns = {
        "/candidate/onboarding",
        "/candidate/onboarding/tasks"
})
public class CandidateOnboardingController extends CandidateBaseController {
    private OnboardingService onboardingService;

    @Override
    public void init() throws ServletException {
        onboardingService = new OnboardingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/onboarding".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("onboarding", null);
        request.setAttribute("tasks", List.of());

        try {
            int candidateId = currentCandidateId(request);
            Onboarding onboarding = onboardingService.getForCandidate(candidateId);
            request.setAttribute("onboarding", onboarding);
            if (onboarding != null) {
                request.setAttribute("tasks", onboardingService.getTasksForCandidate(onboarding.getId(), candidateId));
            }
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }

        view(request, response, "/WEB-INF/views/candidate/onboarding.jsp", "Tiếp nhận nhân sự | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        if (!"/candidate/onboarding/tasks".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            int candidateId = currentCandidateId(request);
            int taskId = RequestUtil.requiredPositiveInt(request, "taskId", "Đầu việc tiếp nhận");
            validateTaskCompletionRequest(RequestUtil.text(request, "status"));
            onboardingService.completeTask(candidateId, taskId);
            redirectWithSuccess(request, response, "/candidate/onboarding", "Đã cập nhật tiến độ tiếp nhận.");
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/candidate/onboarding", ex.getMessage());
        }
    }

    private void validateTaskCompletionRequest(String status) throws BusinessException {
        if (!"DONE".equals(status)) {
        throw new BusinessException("Trạng thái đầu việc tiếp nhận không hợp lệ.");
        }
    }
}
