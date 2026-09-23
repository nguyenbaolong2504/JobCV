package vn.edu.eaut.recruitflow.util;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** Resolves avatar files inside a server-owned directory, never from a browser-supplied path. */
public final class AvatarStorageUtil {
    private AvatarStorageUtil() {
    }

    public static Path resolveDirectory(ServletContext servletContext) throws BusinessException {
        try {
            String catalinaBase = System.getProperty("catalina.base");
            Path directory;
            if (catalinaBase != null && !catalinaBase.isBlank()) {
                directory = Path.of(catalinaBase, "recruitflow-uploads", "avatars");
            } else {
                String webInf = servletContext.getRealPath("/WEB-INF");
                if (webInf != null) {
                    directory = Path.of(webInf, "uploads", "avatars");
                } else {
                    Object tempDirectory = servletContext.getAttribute(ServletContext.TEMPDIR);
                    if (!(tempDirectory instanceof File)) {
                        throw new BusinessException("Chưa thể xác định thư mục lưu ảnh đại diện an toàn.");
                    }
                    directory = ((File) tempDirectory).toPath().resolve("recruitflow").resolve("avatars");
                }
            }
            Path normalized = directory.toAbsolutePath().normalize();
            Files.createDirectories(normalized);
            return normalized;
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new BusinessException("Không thể chuẩn bị thư mục lưu ảnh đại diện.", exception);
        }
    }

    public static Path resolveStored(Path uploadDirectory, String storedName) throws BusinessException {
        if (storedName == null || !storedName.matches("[A-Za-z0-9._-]{1,255}")) {
            throw new BusinessException("Không tìm thấy ảnh đại diện.");
        }
        try {
            Path normalizedRoot = uploadDirectory.toRealPath();
            Path candidateFile = normalizedRoot.resolve(storedName).normalize();
            Path realFile = candidateFile.toRealPath();
            if (!realFile.startsWith(normalizedRoot) || !Files.isRegularFile(realFile)) {
                throw new BusinessException("Không tìm thấy ảnh đại diện.");
            }
            return realFile;
        } catch (IOException | InvalidPathException exception) {
            throw new BusinessException("Không tìm thấy ảnh đại diện.", exception);
        }
    }

    public static void deleteStored(Path uploadDirectory, String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveStored(uploadDirectory, storedName));
        } catch (BusinessException | IOException ignored) {
            // An already-missing old image must not roll back a successful profile update.
        }
    }
}
