package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.model.Resume;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Streams a server-owned CV file after the caller has completed its authorization check. */
public final class ResumeDownloadUtil {
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private ResumeDownloadUtil() {
    }

    public static void stream(ServletContext context, Resume resume, HttpServletResponse response)
            throws BusinessException, IOException {
        if (resume == null) {
            throw new BusinessException("Không tìm thấy CV.");
        }
        String extension = safeExtension(resume.getFileType());
        Path root = ResumeStorageUtil.resolveUploadDirectory(context);
        Path file = ResumeStorageUtil.resolveStoredFile(root, resume.getFilePath());

        response.reset();
        response.setContentType(CONTENT_TYPES.get(extension));
        response.setContentLengthLong(Files.size(file));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Disposition", contentDisposition(resume.getFileName(), extension));
        Files.copy(file, response.getOutputStream());
    }

    private static String safeExtension(String fileType) throws BusinessException {
        String extension = fileType == null ? "" : fileType.trim().toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPES.containsKey(extension)) {
            throw new BusinessException("Định dạng CV không hợp lệ.");
        }
        return extension;
    }

    private static String contentDisposition(String fileName, String extension) {
        String safeName = fileName == null || fileName.isBlank() ? "resume." + extension : fileName;
        String encodedName = URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"resume." + extension + "\"; filename*=UTF-8''" + encodedName;
    }
}
