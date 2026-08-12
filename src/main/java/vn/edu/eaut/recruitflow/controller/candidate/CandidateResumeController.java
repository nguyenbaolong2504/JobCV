package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

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
            request.setAttribute("resumes", resumeService.getResumes(currentCandidateId(request)));
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/resumes.jsp", "CV của tôi | RecruitFlow");
    }

    private void upload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            Part resumePart = request.getPart("resumeFile");
            resumeService.upload(candidateId, resumePart, resolveUploadDirectory());
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
            resumeService.delete(candidateId, resumeId);
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
            Path uploadDirectory = resolveUploadDirectory();
            Path file = safelyResolveStoredResume(uploadDirectory, resume.getFilePath());
            String extension = safeExtension(resume.getFileType());

            response.reset();
            response.setContentType(CONTENT_TYPES.get(extension));
            response.setContentLengthLong(Files.size(file));
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Disposition", contentDisposition(resume.getFileName(), extension));
            Files.copy(file, response.getOutputStream());
        } catch (BusinessException | IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Uses server-owned directory roots only. A database path must resolve inside the configured
     * resume folder, including after symlink resolution, before it can be streamed to a browser.
     */
    private Path safelyResolveStoredResume(Path uploadDirectory, String storedPath) throws BusinessException {
        if (storedPath == null || storedPath.isBlank()) {
            throw new BusinessException("Không tìm thấy tệp CV.");
        }
        try {
            Path normalizedRoot = uploadDirectory.toRealPath();
            Path candidateFile = Path.of(storedPath).toAbsolutePath().normalize();
            Path realFile = candidateFile.toRealPath();
            if (!realFile.startsWith(normalizedRoot) || !Files.isRegularFile(realFile)) {
                throw new BusinessException("Không tìm thấy tệp CV.");
            }
            return realFile;
        } catch (IOException | InvalidPathException ex) {
            throw new BusinessException("Không tìm thấy tệp CV.", ex);
        }
    }

    private Path resolveUploadDirectory() throws BusinessException {
        try {
            String catalinaBase = System.getProperty("catalina.base");
            Path directory;
            if (catalinaBase != null && !catalinaBase.isBlank()) {
                directory = Path.of(catalinaBase, "recruitflow-uploads", "resumes");
            } else {
                String webInf = getServletContext().getRealPath("/WEB-INF");
                if (webInf != null) {
                    directory = Path.of(webInf, "uploads", "resumes");
                } else {
                    Object tempDirectory = getServletContext().getAttribute(ServletContext.TEMPDIR);
                    if (!(tempDirectory instanceof File)) {
                        throw new BusinessException("Chưa thể xác định thư mục lưu CV an toàn.");
                    }
                    directory = ((File) tempDirectory).toPath().resolve("recruitflow").resolve("resumes");
                }
            }
            Path normalized = directory.toAbsolutePath().normalize();
            Files.createDirectories(normalized);
            return normalized;
        } catch (IOException | SecurityException | IllegalArgumentException ex) {
            throw new BusinessException("Không thể chuẩn bị thư mục lưu CV.", ex);
        }
    }

    private String safeExtension(String fileType) throws BusinessException {
        String extension = fileType == null ? "" : fileType.trim().toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPES.containsKey(extension)) {
            throw new BusinessException("Định dạng CV không hợp lệ.");
        }
        return extension;
    }

    private String contentDisposition(String fileName, String extension) {
        String safeName = fileName == null || fileName.isBlank() ? "resume." + extension : fileName;
        String encodedName = URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"resume." + extension + "\"; filename*=UTF-8''" + encodedName;
    }
}
