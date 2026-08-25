package vn.edu.eaut.recruitflow.util;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** Resolves employer logos and covers inside a server-owned directory. */
public final class CompanyMediaStorageUtil {
    private CompanyMediaStorageUtil() {}

    public static Path resolveDirectory(ServletContext servletContext) throws BusinessException {
        try {
            String catalinaBase = System.getProperty("catalina.base");
            Path directory;
            if (catalinaBase != null && !catalinaBase.isBlank()) {
                directory = Path.of(catalinaBase, "recruitflow-uploads", "companies");
            } else {
                Object tempDirectory = servletContext.getAttribute(ServletContext.TEMPDIR);
                if (!(tempDirectory instanceof File)) throw new BusinessException("Chưa thể xác định thư mục lưu ảnh doanh nghiệp.");
                directory = ((File) tempDirectory).toPath().resolve("recruitflow").resolve("companies");
            }
            Path normalized = directory.toAbsolutePath().normalize();
            Files.createDirectories(normalized);
            return normalized;
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new BusinessException("Không thể chuẩn bị thư mục lưu ảnh doanh nghiệp.", exception);
        }
    }

    public static Path resolveStored(Path uploadDirectory, String storedName) throws BusinessException {
        if (storedName == null || !storedName.matches("company_[A-Za-z0-9._-]{1,245}")) throw new BusinessException("Không tìm thấy ảnh doanh nghiệp.");
        try {
            Path root = uploadDirectory.toRealPath();
            Path file = root.resolve(storedName).normalize().toRealPath();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) throw new BusinessException("Không tìm thấy ảnh doanh nghiệp.");
            return file;
        } catch (IOException | InvalidPathException exception) {
            throw new BusinessException("Không tìm thấy ảnh doanh nghiệp.", exception);
        }
    }

    public static void deleteStored(Path directory, String storedName) {
        if (storedName == null || !storedName.startsWith("company_")) return;
        try { Files.deleteIfExists(resolveStored(directory, storedName)); } catch (BusinessException | IOException ignored) { }
    }
}
