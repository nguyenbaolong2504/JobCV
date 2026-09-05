package vn.edu.eaut.recruitflow.util;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Server-owned storage for public home banners; browser-provided paths are never used. */
public final class HomeBannerStorageUtil {
    private HomeBannerStorageUtil() {}
    public static Path resolveDirectory(ServletContext context) throws BusinessException {
        try {
            String catalinaBase = System.getProperty("catalina.base");
            Path directory;
            if (catalinaBase != null && !catalinaBase.isBlank()) directory = Path.of(catalinaBase, "recruitflow-uploads", "home-banners");
            else {
                Object temp = context.getAttribute(ServletContext.TEMPDIR);
                if (!(temp instanceof File)) throw new BusinessException("Chưa thể xác định thư mục lưu banner.");
                directory = ((File) temp).toPath().resolve("jobcv").resolve("home-banners");
            }
            directory = directory.toAbsolutePath().normalize(); Files.createDirectories(directory); return directory;
        } catch (IOException | RuntimeException exception) { throw new BusinessException("Không thể chuẩn bị thư mục lưu banner.", exception); }
    }
    public static Path resolveStored(Path root, String name) throws BusinessException {
        if (name == null || !name.matches("home_banner_[A-Za-z0-9._-]{1,245}")) throw new BusinessException("Không tìm thấy banner.");
        try {
            Path realRoot = root.toRealPath(); Path file = realRoot.resolve(name).normalize().toRealPath();
            if (!file.startsWith(realRoot) || !Files.isRegularFile(file)) throw new BusinessException("Không tìm thấy banner.");
            return file;
        } catch (IOException exception) { throw new BusinessException("Không tìm thấy banner.", exception); }
    }
    public static void deleteStored(Path root, String name) {
        if (name == null || !name.startsWith("home_banner_")) return;
        try { Files.deleteIfExists(resolveStored(root, name)); } catch (IOException | BusinessException ignored) { }
    }
}
