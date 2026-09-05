package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.SavedJobService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Candidate-owned saved-job list and idempotent bookmark actions. */
@WebServlet(name = "CandidateSavedJobController", urlPatterns = {
        "/candidate/saved-jobs",
        "/candidate/saved-jobs/save",
        "/candidate/saved-jobs/remove"
})
public class CandidateSavedJobController extends CandidateBaseController {
    private SavedJobService savedJobService;

    @Override
    public void init() throws ServletException {
        savedJobService = new SavedJobService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/saved-jobs".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int page = RequestUtil.page(request);
        int pageSize = RequestUtil.pageSize(request);
        request.setAttribute("page", new PageResult<Job>(List.of(), page, pageSize, 0));
        try {
            int candidateId = currentCandidateId(request);
            request.setAttribute("page", savedJobService.getSavedJobs(candidateId, page, pageSize));
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/saved-jobs.jsp", "Việc làm đã lưu | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        String path = request.getServletPath();
        try {
            int candidateId = currentCandidateId(request);
            int jobId = RequestUtil.requiredPositiveInt(request, "jobId", "Tin tuyển dụng");
            String returnTo = safeReturnPath(RequestUtil.text(request, "returnTo"), jobId);
            if ("/candidate/saved-jobs/save".equals(path)) {
                boolean created = savedJobService.save(candidateId, jobId);
                redirectWithSuccess(request, response, returnTo,
                        created ? "Đã lưu việc làm để xem lại sau." : "Việc làm này đã có trong danh sách đã lưu.");
                return;
            }
            if ("/candidate/saved-jobs/remove".equals(path)) {
                savedJobService.remove(candidateId, jobId);
                redirectWithSuccess(request, response, returnTo, "Đã bỏ việc làm khỏi danh sách đã lưu.");
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/saved-jobs", ex.getMessage());
        }
    }

    private String safeReturnPath(String requestedPath, int jobId) {
        if ("/candidate/jobs".equals(requestedPath)
                || "/candidate/saved-jobs".equals(requestedPath)
                || "/candidate/dashboard".equals(requestedPath)
                || "/home".equals(requestedPath)) {
            return requestedPath;
        }
        String detailPath = "/jobs/detail?id=" + jobId;
        return detailPath.equals(requestedPath) ? detailPath : "/candidate/saved-jobs";
    }
}
