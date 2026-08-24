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

@WebServlet("/hr/onboarding/tasks/create")
public class OnboardingTaskCreateController extends BaseController {
    private OnboardingService onboardingService;

    @Override
    public void init() throws ServletException {
        onboardingService = new OnboardingService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String backTo = "/hr/onboarding";
        try {
            int onboardingId = RequestUtil.requiredPositiveInt(request, "onboardingId", "Quy trình tiếp nhận");
            String taskName = RequestUtil.text(request, "taskName");
            if (taskName.isEmpty()) {
                throw new BusinessException("Tên đầu việc tiếp nhận là bắt buộc.");
            }
            boolean required = "true".equalsIgnoreCase(RequestUtil.text(request, "required"))
                    || "on".equalsIgnoreCase(RequestUtil.text(request, "required"));
            onboardingService.addTask(onboardingId, taskName, required, RequestUtil.currentUserId(request));
            backTo += "?id=" + onboardingId;
            redirectWithSuccess(request, response, backTo, "Đã thêm đầu việc tiếp nhận.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, backTo, ex.getMessage());
        }
    }
}
