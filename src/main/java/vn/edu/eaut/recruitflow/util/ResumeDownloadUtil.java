package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.model.Resume;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
        Path root = resolveUploadDirectory(context);
        Path file = safelyResolveStoredResume(root, resume.getFilePath());

        response.reset();
        response.setContentType(CONTENT_TYPES.get(extension));
        response.setContentLengthLong(Files.size(file));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Disposition", contentDisposition(resume.getFileName(), extension));
        Files.copy(file, response.getOutputStream());
    }

    private static Path resolveUploadDirectory(ServletContext context) throws BusinessException {
        try {
            String catalinaBase = System.getProperty("catalina.base");
            Path directory;
            if (catalinaBase != null && !catalinaBase.isBlank()) {
                directory = Path.of(catalinaBase, "recruitflow-uploads", "resumes");
            } else {
                String webInf = context.getRealPath("/WEB-INF");
                if (webInf != null) {
                    directory = Path.of(webInf, "uploads", "resumes");
                } else {
                    Object tempDirectory = context.getAttribute(ServletContext.TEMPDIR);
                    if (!(tempDirectory instanceof File)) {
                        throw new BusinessException("Chưa thể xác định thư mục lưu CV an toàn.");
                    }
                    directory = ((File) tempDirectory).toPath().resolve("recruitflow").resolve("resumes");
                }
            }
            Path normalized = directory.toAbsolutePath().normalize();
            Files.createDirectories(normalized);
            return normalized;
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new BusinessException("Không thể chuẩn bị thư mục lưu CV.", exception);
        }
    }

    private static Path safelyResolveStoredResume(Path uploadDirectory, String storedPath) throws BusinessException {
        if (storedPath == null || storedPath.isBlank()) {
            throw new BusinessException("Không tìm thấy tệp CV.");
        }
        try {
            Path normalizedRoot = uploadDirectory.toRealPath();
            Path realFile = Path.of(storedPath).toAbsolutePath().normalize().toRealPath();
            if (!realFile.startsWith(normalizedRoot) || !Files.isRegularFile(realFile)) {
                throw new BusinessException("Không tìm thấy tệp CV.");
            }
            return realFile;
        } catch (IOException | InvalidPathException exception) {
            throw new BusinessException("Không tìm thấy tệp CV.", exception);
        }
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
