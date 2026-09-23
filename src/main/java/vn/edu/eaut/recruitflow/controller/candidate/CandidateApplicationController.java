package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Candidate application list, detail, apply, and withdrawal endpoints. */
@WebServlet(name = "CandidateApplicationController", urlPatterns = {
        "/candidate/applications",
        "/candidate/applications/apply",
        "/candidate/applications/detail",
        "/candidate/applications/withdraw"
})
public class CandidateApplicationController extends CandidateBaseController {
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        applicationService = new ApplicationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if ("/candidate/applications/detail".equals(request.getServletPath())) {
            showDetail(request, response);
            return;
        }
        if ("/candidate/applications".equals(request.getServletPath())) {
            showList(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        String path = request.getServletPath();
        if ("/candidate/applications/apply".equals(path)) {
            apply(request, response);
            return;
        }
        if ("/candidate/applications/withdraw".equals(path)) {
            withdraw(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = RequestUtil.page(request);
        int pageSize = RequestUtil.pageSize(request);
        request.setAttribute("page", new PageResult<Application>(List.of(), page, pageSize, 0));
        request.setAttribute("statuses", Arrays.asList(ApplicationStatus.values()));

        try {
            int candidateId = currentCandidateId(request);
            String keyword = boundedText(request, "keyword", "Từ khóa", 150);
            ApplicationStatus status = optionalStatus(RequestUtil.text(request, "status"));
            request.setAttribute("page", applicationService.searchForCandidate(
                    candidateId, keyword, status, page, pageSize));
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }

        view(request, response, "/WEB-INF/views/candidate/applications.jsp", "Đơn ứng tuyển | JobCV");
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int candidateId = currentCandidateId(request);
            int applicationId = RequestUtil.requiredPositiveInt(request, "id", "Đơn ứng tuyển");

            // Every service read is candidate-scoped; no controller-side ownership comparison is relied upon.
            request.setAttribute("application", applicationService.getForCandidate(applicationId, candidateId));
            request.setAttribute("history", applicationService.getHistoryForCandidate(candidateId, applicationId));
            request.setAttribute("interviews", applicationService.getInterviewsForCandidate(candidateId, applicationId));
            request.setAttribute("offer", applicationService.getOfferForCandidate(candidateId, applicationId));
            view(request, response, "/WEB-INF/views/candidate/application-detail.jsp", "Chi tiết đơn ứng tuyển | JobCV");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/applications", ex.getMessage());
        }
    }

    private void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            int jobId = RequestUtil.requiredPositiveInt(request, "jobId", "Tin tuyển dụng");
            String resumeValue = RequestUtil.text(request, "resumeId");
            Integer resumeId = resumeValue.isEmpty()
                    ? null
                    : RequestUtil.requiredPositiveInt(request, "resumeId", "CV ứng tuyển");
            String coverLetter = boundedText(request, "coverLetter", "Lời giới thiệu", 2000);
            applicationService.apply(candidateId, jobId, resumeId, coverLetter);
            redirectWithSuccess(request, response, "/candidate/applications", "Đã nộp đơn ứng tuyển thành công.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/jobs", ex.getMessage());
        }
    }

    private void withdraw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            int applicationId = RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển");
            applicationService.withdraw(applicationId, candidateId, null);
            redirectWithSuccess(request, response, "/candidate/applications", "Đã rút đơn ứng tuyển.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/applications", ex.getMessage());
        }
    }

    private ApplicationStatus optionalStatus(String rawStatus) {
        return rawStatus == null || rawStatus.isEmpty() ? null : ApplicationStatus.fromValue(rawStatus);
    }
}
