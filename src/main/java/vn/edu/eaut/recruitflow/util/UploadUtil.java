package vn.edu.eaut.recruitflow.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class UploadUtil {
    public static final long MAX_RESUME_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> RESUME_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Map<String, Set<String>> RESUME_CONTENT_TYPES = Map.of(
            "pdf", Set.of("application/pdf"),
            "doc", Set.of("application/msword", "application/vnd.ms-word"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    );

    private UploadUtil() {
    }

    public static String extension(String submittedFileName) throws BusinessException {
        if (submittedFileName == null) {
            throw new BusinessException("Vui lòng chọn tệp CV.");
        }
        String cleanName = submittedFileName.replace('\\', '/');
        cleanName = cleanName.substring(cleanName.lastIndexOf('/') + 1);
        int dot = cleanName.lastIndexOf('.');
        if (dot < 1 || dot == cleanName.length() - 1) {
            throw new BusinessException("Tệp CV phải là PDF, DOC hoặc DOCX.");
        }
        String extension = cleanName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!RESUME_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Chỉ được tải lên CV PDF, DOC hoặc DOCX.");
        }
        return extension;
    }

    public static void validateResumePart(Part part) throws BusinessException {
        if (part == null || part.getSize() == 0) {
            throw new BusinessException("Vui lòng chọn tệp CV.");
        }
        if (part.getSize() > MAX_RESUME_SIZE) {
            throw new BusinessException("Kích thước CV không được vượt quá 5 MB.");
        }
        String extension = extension(part.getSubmittedFileName());
        String contentType = part.getContentType();
        String normalizedContentType = contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (!RESUME_CONTENT_TYPES.get(extension).contains(normalizedContentType)) {
            throw new BusinessException("Định dạng MIME của CV không hợp lệ.");
        }
    }

    public static Path storeResume(Part part, int candidateId, Path uploadDirectory) throws IOException, BusinessException {
        validateResumePart(part);
        String extension = extension(part.getSubmittedFileName());
        Files.createDirectories(uploadDirectory);
        String storedName = "resume_" + candidateId + "_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(storedName).normalize();
        if (!destination.startsWith(uploadDirectory.toAbsolutePath().normalize())) {
            throw new BusinessException("Đường dẫn tệp không hợp lệ.");
        }
        try (java.io.InputStream stream = part.getInputStream()) {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return destination;
    }
}
