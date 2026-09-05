package vn.edu.eaut.recruitflow.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class UploadUtil {
    public static final long MAX_RESUME_SIZE = 5L * 1024L * 1024L;
    public static final long MAX_AVATAR_SIZE = 2L * 1024L * 1024L;
    private static final Set<String> RESUME_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Set<String> AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final int PDF_HEADER_SCAN_LIMIT = 1024;
    private static final Map<String, Set<String>> RESUME_CONTENT_TYPES = Map.of(
            "pdf", Set.of("application/pdf"),
            "doc", Set.of("application/msword", "application/vnd.ms-word"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    );
    private static final Map<String, Set<String>> AVATAR_CONTENT_TYPES = Map.of(
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png", Set.of("image/png"),
            "webp", Set.of("image/webp")
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
        try (InputStream input = part.getInputStream()) {
            byte[] header = input.readNBytes("pdf".equals(extension) ? PDF_HEADER_SCAN_LIMIT : 8);
            if (!hasExpectedSignature(extension, header)) {
                throw new BusinessException("Nội dung tệp không khớp với định dạng CV đã chọn.");
            }
        } catch (IOException exception) {
            throw new BusinessException("Không thể kiểm tra nội dung tệp CV.", exception);
        }
    }

    public static String avatarExtension(String submittedFileName) throws BusinessException {
        if (submittedFileName == null) {
            throw new BusinessException("Vui lòng chọn ảnh đại diện.");
        }
        String cleanName = submittedFileName.replace('\\', '/');
        cleanName = cleanName.substring(cleanName.lastIndexOf('/') + 1);
        int dot = cleanName.lastIndexOf('.');
        if (dot < 1 || dot == cleanName.length() - 1) {
            throw new BusinessException("Ảnh đại diện phải là JPG, PNG hoặc WEBP.");
        }
        String extension = cleanName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!AVATAR_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Chỉ được tải lên ảnh JPG, PNG hoặc WEBP.");
        }
        return extension;
    }

    public static void validateAvatarPart(Part part) throws BusinessException {
        if (part == null || part.getSize() == 0) {
            throw new BusinessException("Vui lòng chọn ảnh đại diện.");
        }
        if (part.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("Ảnh đại diện không được vượt quá 2 MB.");
        }
        String extension = avatarExtension(part.getSubmittedFileName());
        String contentType = part.getContentType();
        String normalizedContentType = contentType == null
                ? ""
                : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (!AVATAR_CONTENT_TYPES.get(extension).contains(normalizedContentType)) {
            throw new BusinessException("Định dạng MIME của ảnh đại diện không hợp lệ.");
        }
        try (InputStream input = part.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (!hasExpectedAvatarSignature(extension, header)) {
                throw new BusinessException("Nội dung tệp không khớp với định dạng ảnh đã chọn.");
            }
        } catch (IOException exception) {
            throw new BusinessException("Không thể kiểm tra nội dung ảnh đại diện.", exception);
        }
    }

    private static boolean hasExpectedSignature(String extension, byte[] header) {
        if ("pdf".equals(extension)) {
            return hasPdfHeaderAfterSafePrefix(header);
        }
        if ("doc".equals(extension)) {
            byte[] ole = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
            return java.util.Arrays.equals(header, ole);
        }
        return "docx".equals(extension) && header.length >= 4 && header[0] == 'P' && header[1] == 'K'
                && (header[2] == 3 || header[2] == 5 || header[2] == 7)
                && (header[3] == 4 || header[3] == 6 || header[3] == 8);
    }

    /**
     * PDF readers are required in practice to locate the header near the beginning of the file.
     * Some valid exporters prepend a line break (or an UTF-8 BOM), so requiring byte zero to be
     * '%' rejects readable CVs. Only harmless leading whitespace/BOM is accepted here; arbitrary
     * executable or HTML prefixes remain rejected to avoid accepting polyglot uploads.
     */
    private static boolean hasPdfHeaderAfterSafePrefix(byte[] bytes) {
        int index = 0;
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            index = 3;
        }
        while (index < bytes.length && isAsciiWhitespace(bytes[index])) {
            index++;
        }
        return index + 5 <= bytes.length
                && bytes[index] == '%'
                && bytes[index + 1] == 'P'
                && bytes[index + 2] == 'D'
                && bytes[index + 3] == 'F'
                && bytes[index + 4] == '-';
    }

    private static boolean isAsciiWhitespace(byte value) {
        return value == 0x00 || value == 0x09 || value == 0x0A || value == 0x0C || value == 0x0D || value == 0x20;
    }

    private static boolean hasExpectedAvatarSignature(String extension, byte[] header) {
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
        }
        if ("png".equals(extension)) {
            byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            return header.length >= png.length
                    && java.util.Arrays.equals(java.util.Arrays.copyOf(header, png.length), png);
        }
        return "webp".equals(extension) && header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
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

    public static Path storeAvatar(Part part, int candidateId, Path uploadDirectory)
            throws IOException, BusinessException {
        validateAvatarPart(part);
        String extension = avatarExtension(part.getSubmittedFileName());
        Path normalizedDirectory = uploadDirectory.toAbsolutePath().normalize();
        Files.createDirectories(normalizedDirectory);
        String storedName = "avatar_" + candidateId + "_" + System.currentTimeMillis() + "_"
                + java.util.UUID.randomUUID() + "." + extension;
        Path destination = normalizedDirectory.resolve(storedName).normalize();
        if (!destination.startsWith(normalizedDirectory)) {
            throw new BusinessException("Đường dẫn ảnh đại diện không hợp lệ.");
        }
        try (InputStream stream = part.getInputStream()) {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
        return destination;
    }

    public static Path storeCompanyLogo(Part part, int companyId, Path uploadDirectory)
            throws IOException, BusinessException {
        validateAvatarPart(part);
        String extension = avatarExtension(part.getSubmittedFileName());
        Path normalizedDirectory = uploadDirectory.toAbsolutePath().normalize();
        Files.createDirectories(normalizedDirectory);
        String storedName = "company_" + companyId + "_" + System.currentTimeMillis() + "_"
                + java.util.UUID.randomUUID() + "." + extension;
        Path destination = normalizedDirectory.resolve(storedName).normalize();
        if (!destination.startsWith(normalizedDirectory)) throw new BusinessException("Đường dẫn logo không hợp lệ.");
        try (InputStream stream = part.getInputStream()) {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Files.deleteIfExists(destination); throw exception;
        }
        return destination;
    }

    /** Stores a campaign/banner image after the same signature validation used for avatars and logos. */
    public static Path storeHomeBanner(Part part, Path uploadDirectory) throws IOException, BusinessException {
        validateAvatarPart(part);
        String extension = avatarExtension(part.getSubmittedFileName());
        Path normalizedDirectory = uploadDirectory.toAbsolutePath().normalize();
        Files.createDirectories(normalizedDirectory);
        String storedName = "home_banner_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID() + "." + extension;
        Path destination = normalizedDirectory.resolve(storedName).normalize();
        if (!destination.startsWith(normalizedDirectory)) throw new BusinessException("Đường dẫn banner không hợp lệ.");
        try (InputStream stream = part.getInputStream()) {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
        return destination;
    }
}
