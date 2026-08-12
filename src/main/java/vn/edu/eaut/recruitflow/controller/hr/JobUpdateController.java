package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/jobs/update")
public class JobUpdateController extends BaseController {
    private JobService jobService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int jobId = RequestUtil.requiredPositiveInt(request, "id", "Tin tuyển dụng");
            int actorId = RequestUtil.currentUserId(request);
            Job job = JobCreateController.bindJob(request);
            job.setId(jobId);
            jobService.updateJob(job, RequestUtil.text(request, "skills"), actorId);
            redirectWithSuccess(request, response, "/hr/jobs", "Đã cập nhật tin tuyển dụng.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/jobs", ex.getMessage());
        }
    }
}
