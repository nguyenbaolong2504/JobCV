package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.ResumeDownloadUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** HR/Admin can download the CV attached to a specific application. */
@WebServlet("/hr/resumes/download")
public class HRResumeDownloadController extends BaseController {
    private ApplicationService applicationService;
    private ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        applicationService = new ApplicationService();
        resumeService = new ResumeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int applicationId = RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển");
            Application application = applicationService.getForHr(
                    applicationId, RequestUtil.currentUserId(request));
            Resume resume = resumeService.getResumeForStaff(application.getResumeId());
            ResumeDownloadUtil.stream(getServletContext(), resume, response);
        } catch (BusinessException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
