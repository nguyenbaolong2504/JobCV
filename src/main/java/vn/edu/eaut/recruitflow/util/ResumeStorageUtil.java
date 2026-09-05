package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.model.Resume;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/** Keeps resume paths portable across Tomcat deployments and rejects files outside the owned root. */
public final class ResumeStorageUtil {
    private ResumeStorageUtil() {
    }

    public static Path resolveUploadDirectory(ServletContext context) throws BusinessException {
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

    public static Path resolveStoredFile(Path uploadDirectory, String storedPath) throws BusinessException {
        if (storedPath == null || storedPath.isBlank()) {
            throw new BusinessException("Không tìm thấy tệp CV.");
        }
        try {
            Path normalizedRoot = uploadDirectory.toRealPath();
            Path databasePath = Path.of(storedPath);
            Path candidateFile = databasePath.isAbsolute()
                    ? databasePath.toAbsolutePath().normalize()
                    : normalizedRoot.resolve(databasePath).normalize();
            Path realFile = candidateFile.toRealPath();
            if (!realFile.startsWith(normalizedRoot) || !Files.isRegularFile(realFile)) {
                throw new BusinessException("Không tìm thấy tệp CV.");
            }
            return realFile;
        } catch (IOException | InvalidPathException exception) {
            throw new BusinessException("Không tìm thấy tệp CV.", exception);
        }
    }

    public static boolean isAvailable(Path uploadDirectory, Resume resume) {
        try {
            resolveStoredFile(uploadDirectory, resume == null ? null : resume.getFilePath());
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    public static void refreshAvailability(ServletContext context, List<Resume> resumes) {
        if (resumes == null || resumes.isEmpty()) {
            return;
        }
        try {
            Path uploadDirectory = resolveUploadDirectory(context);
            resumes.forEach(resume -> resume.setFileAvailable(isAvailable(uploadDirectory, resume)));
        } catch (BusinessException exception) {
            resumes.forEach(resume -> resume.setFileAvailable(false));
        }
    }

    public static List<Resume> availableResumes(ServletContext context, List<Resume> resumes) {
        refreshAvailability(context, resumes);
        return resumes == null ? List.of() : resumes.stream().filter(Resume::isFileAvailable).toList();
    }

    /** New database rows store only a server-generated filename, not a deployment-specific root. */
    public static String portableStoredPath(Path storedFile) {
        return storedFile.getFileName().toString();
    }
}
