package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/applications/status")
public class ApplicationStatusController extends BaseController {
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        applicationService = new ApplicationService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String backTo = "/hr/applications";
        try {
            int applicationId = RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển");
            ApplicationStatus targetStatus = ApplicationStatus.fromValue(RequestUtil.text(request, "status"));
            int actorId = RequestUtil.currentUserId(request);
            applicationService.transitionStatus(
                    applicationId,
                    targetStatus.name(),
                    RequestUtil.text(request, "remarks"),
                    actorId
            );
            String detailId = RequestUtil.text(request, "returnDetail");
            backTo = detailId.isEmpty() ? backTo : "/hr/applications/detail?id=" + applicationId;
            redirectWithSuccess(request, response, backTo, "Đã cập nhật trạng thái đơn ứng tuyển.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, backTo, ex.getMessage());
        }
    }
}
