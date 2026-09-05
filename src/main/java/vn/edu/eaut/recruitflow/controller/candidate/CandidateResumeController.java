package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.ResumeDownloadUtil;
import vn.edu.eaut.recruitflow.util.ResumeStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.List;

/** Candidate-owned resume management, including safe streaming download. */
@WebServlet(name = "CandidateResumeController", urlPatterns = {
        "/candidate/resumes",
        "/candidate/resumes/upload",
        "/candidate/resumes/default",
        "/candidate/resumes/delete",
        "/candidate/resumes/download"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5L * 1024L * 1024L,
        maxRequestSize = 6L * 1024L * 1024L
)
public class CandidateResumeController extends CandidateBaseController {
    private ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        resumeService = new ResumeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if ("/candidate/resumes/download".equals(request.getServletPath())) {
            download(request, response);
            return;
        }
        if (!"/candidate/resumes".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        showResumes(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        String path = request.getServletPath();
        if ("/candidate/resumes/upload".equals(path)) {
            upload(request, response);
            return;
        }
        if ("/candidate/resumes/default".equals(path)) {
            setDefault(request, response);
            return;
        }
        if ("/candidate/resumes/delete".equals(path)) {
            delete(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showResumes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("resumes", List.of());
        try {
            List<Resume> resumes = resumeService.getResumes(currentCandidateId(request));
            ResumeStorageUtil.refreshAvailability(getServletContext(), resumes);
            request.setAttribute("resumes", resumes);
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/resumes.jsp", "CV của tôi | JobCV");
    }

    private void upload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            Part resumePart = request.getPart("resumeFile");
            resumeService.upload(candidateId, resumePart,
                    ResumeStorageUtil.resolveUploadDirectory(getServletContext()));
            redirectWithSuccess(request, response, "/candidate/resumes", "Đã tải CV lên thành công.");
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/candidate/resumes", ex.getMessage());
        } catch (IllegalStateException | ServletException ex) {
            redirectWithError(request, response, "/candidate/resumes",
                    "Tệp CV không hợp lệ hoặc vượt quá giới hạn 5 MB.");
        } catch (IOException ex) {
            redirectWithError(request, response, "/candidate/resumes",
                    "Không thể đọc tệp CV. Vui lòng thử tải lại.");
        }
    }

    private void setDefault(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            int resumeId = RequestUtil.requiredPositiveInt(request, "resumeId", "CV");
            resumeService.setDefault(candidateId, resumeId);
            redirectWithSuccess(request, response, "/candidate/resumes", "Đã đặt CV mặc định.");
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/candidate/resumes", ex.getMessage());
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            int resumeId = RequestUtil.requiredPositiveInt(request, "resumeId", "CV");
            resumeService.delete(candidateId, resumeId,
                    ResumeStorageUtil.resolveUploadDirectory(getServletContext()));
            redirectWithSuccess(request, response, "/candidate/resumes", "Đã xóa CV.");
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/candidate/resumes", ex.getMessage());
        }
    }

    private void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            int resumeId = RequestUtil.requiredPositiveInt(request, "id", "CV");
            Resume resume = resumeService.getResumeForCandidate(candidateId, resumeId);
            ResumeDownloadUtil.stream(getServletContext(), resume, response);
        } catch (BusinessException | IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
