package vn.edu.eaut.recruitflow.service;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.model.CvBuilderData;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.ResumeParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Generates an application-ready DOCX resume from safe, candidate-owned form data. */
public class CvBuilderService {
    public static final String TEMPLATE_MODERN = "MODERN";
    public static final String TEMPLATE_CLASSIC = "CLASSIC";
    public static final String TEMPLATE_MINIMAL = "MINIMAL";

    private static final Set<String> TEMPLATES = Set.of(TEMPLATE_MODERN, TEMPLATE_CLASSIC, TEMPLATE_MINIMAL);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ResumeDAO resumeDAO;

    public CvBuilderService() {
        this(new ResumeDAO());
    }

    CvBuilderService(ResumeDAO resumeDAO) {
        this.resumeDAO = resumeDAO;
    }

    /**
     * Creates a DOCX inside the same managed resume directory used by uploads, then records it in
     * the resumes table. If database persistence fails, the generated file is removed.
     */
    public Resume generate(int candidateId, CvBuilderData data, Path uploadDirectory) throws BusinessException {
        if (candidateId <= 0) {
            throw new BusinessException("Tài khoản ứng viên không hợp lệ.");
        }
        if (uploadDirectory == null) {
            throw new BusinessException("Không thể xác định nơi lưu CV.");
        }
        validateAndNormalize(data);

        Path normalizedDirectory;
        try {
            normalizedDirectory = uploadDirectory.toAbsolutePath().normalize();
            Files.createDirectories(normalizedDirectory);
        } catch (IOException | SecurityException exception) {
            throw new BusinessException("Không thể chuẩn bị thư mục lưu CV.", exception);
        }

        String storedName = "resume_" + candidateId + "_generated_"
                + FILE_TIMESTAMP.format(LocalDateTime.now()) + "_" + java.util.UUID.randomUUID() + ".docx";
        Path storedFile = normalizedDirectory.resolve(storedName).normalize();
        if (!storedFile.startsWith(normalizedDirectory)) {
            throw new BusinessException("Đường dẫn lưu CV không hợp lệ.");
        }

        try {
            writeDocument(storedFile, data);
            String extractedText;
            try (InputStream input = Files.newInputStream(storedFile)) {
                extractedText = ResumeParser.extractText(input, "docx");
            }
            Resume resume = persist(candidateId, data, storedFile, extractedText);
            return resume;
        } catch (IOException | SQLException exception) {
            deleteQuietly(storedFile);
            throw new BusinessException("Không thể tạo CV. Vui lòng thử lại.", exception);
        } catch (BusinessException exception) {
            deleteQuietly(storedFile);
            throw exception;
        }
    }

    private Resume persist(int candidateId, CvBuilderData data, Path storedFile, String extractedText)
            throws SQLException, BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                boolean firstResume = resumeDAO.countByCandidateId(connection, candidateId) == 0;
                Resume resume = new Resume();
                resume.setCandidateId(candidateId);
                resume.setFileName("CV_" + safeFilenamePart(data.getFullName()) + ".docx");
                resume.setFilePath(storedFile.toAbsolutePath().toString());
                resume.setFileType("docx");
                resume.setFileSize(Files.size(storedFile));
                resume.setExtractedText(extractedText);
                // Avoid violating the one-default-CV unique key before setDefault clears an
                // existing default inside this transaction.
                resume.setDefaultResume(firstResume);
                resumeDAO.insert(connection, resume);
                if (data.isMakeDefault() && !firstResume) {
                    resumeDAO.setDefault(connection, candidateId, resume.getId());
                    resume.setDefaultResume(true);
                }
                connection.commit();
                return resume;
            } catch (BusinessException | SQLException | IOException exception) {
                connection.rollback();
                if (exception instanceof BusinessException businessException) {
                    throw businessException;
                }
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new BusinessException("Không thể lưu CV đã tạo.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void writeDocument(Path output, CvBuilderData data) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); OutputStream stream = Files.newOutputStream(output)) {
            String accent = switch (data.getTemplate()) {
                case TEMPLATE_CLASSIC -> "1F2937";
                case TEMPLATE_MINIMAL -> "374151";
                default -> "2563EB";
            };

            addHeader(document, data, accent);
            addSection(document, "GIỚI THIỆU", data.getSummary(), accent, false);
            addSection(document, "KINH NGHIỆM LÀM VIỆC", data.getExperience(), accent, true);
            addSection(document, "HỌC VẤN", data.getEducation(), accent, true);
            addSection(document, "KỸ NĂNG", data.getSkills(), accent, true);
            addSection(document, "DỰ ÁN NỔI BẬT", data.getProjects(), accent, true);
            addSection(document, "CHỨNG CHỈ & HOẠT ĐỘNG", data.getCertifications(), accent, true);
            document.write(stream);
        }
    }

    private void addHeader(XWPFDocument document, CvBuilderData data, String accent) {
        XWPFParagraph name = document.createParagraph();
        name.setAlignment(TEMPLATE_MINIMAL.equals(data.getTemplate()) ? ParagraphAlignment.LEFT : ParagraphAlignment.CENTER);
        name.setSpacingAfter(60);
        XWPFRun nameRun = name.createRun();
        nameRun.setText(data.getFullName().toUpperCase(Locale.ROOT));
        nameRun.setBold(true);
        nameRun.setFontFamily("Aptos Display");
        nameRun.setFontSize(22);
        nameRun.setColor(accent);

        XWPFParagraph role = document.createParagraph();
        role.setAlignment(name.getAlignment());
        role.setSpacingAfter(110);
        XWPFRun roleRun = role.createRun();
        roleRun.setText(data.getTargetRole());
        roleRun.setBold(true);
        roleRun.setFontFamily("Aptos");
        roleRun.setFontSize(11);

        XWPFParagraph contact = document.createParagraph();
        contact.setAlignment(name.getAlignment());
        contact.setSpacingAfter(240);
        XWPFRun contactRun = contact.createRun();
        contactRun.setText(joinNonBlank("  |  ", data.getEmail(), data.getPhone(), data.getLocation()));
        contactRun.setFontFamily("Aptos");
        contactRun.setFontSize(9);
        contactRun.setColor("4B5563");
    }

    private void addSection(XWPFDocument document, String title, String content, String accent, boolean lineSeparated) {
        if (content == null || content.isBlank()) {
            return;
        }
        XWPFParagraph heading = document.createParagraph();
        heading.setSpacingBefore(150);
        heading.setSpacingAfter(70);
        XWPFRun headingRun = heading.createRun();
        headingRun.setText(title);
        headingRun.setBold(true);
        headingRun.setFontFamily("Aptos");
        headingRun.setFontSize(11);
        headingRun.setColor(accent);

        String[] lines = content.split("\\R+");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                continue;
            }
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setSpacingAfter(45);
            XWPFRun run = paragraph.createRun();
            if (lineSeparated) {
                run.setText("• " + line);
            } else {
                run.setText(line);
            }
            run.setFontFamily("Aptos");
            run.setFontSize(10);
            run.setColor("1F2937");
        }
    }

    private void validateAndNormalize(CvBuilderData data) throws BusinessException {
        if (data == null) {
            throw new BusinessException("Thông tin tạo CV không hợp lệ.");
        }
        String template = normalized(data.getTemplate()).toUpperCase(Locale.ROOT);
        if (!TEMPLATES.contains(template)) {
            throw new BusinessException("Mẫu CV không hợp lệ.");
        }
        data.setTemplate(template);
        data.setFullName(required(data.getFullName(), "Họ và tên", 100));
        data.setTargetRole(required(data.getTargetRole(), "Vị trí mục tiêu", 120));
        data.setEmail(optional(data.getEmail(), "Email", 254));
        if (!data.getEmail().isBlank() && !EMAIL_PATTERN.matcher(data.getEmail()).matches()) {
            throw new BusinessException("Email trên CV không hợp lệ.");
        }
        data.setPhone(optional(data.getPhone(), "Số điện thoại", 30));
        data.setLocation(optional(data.getLocation(), "Địa điểm", 120));
        data.setSummary(required(data.getSummary(), "Giới thiệu", 2000));
        data.setSkills(optional(data.getSkills(), "Kỹ năng", 2000));
        data.setExperience(optional(data.getExperience(), "Kinh nghiệm", 6000));
        data.setEducation(optional(data.getEducation(), "Học vấn", 4000));
        data.setProjects(optional(data.getProjects(), "Dự án", 5000));
        data.setCertifications(optional(data.getCertifications(), "Chứng chỉ", 3000));
        if (data.getExperience().isBlank() && data.getEducation().isBlank()) {
            throw new BusinessException("CV cần có ít nhất kinh nghiệm làm việc hoặc học vấn.");
        }
    }

    private String required(String value, String label, int max) throws BusinessException {
        String normalized = optional(value, label, max);
        if (normalized.isBlank()) {
            throw new BusinessException(label + " là bắt buộc.");
        }
        return normalized;
    }

    private String optional(String value, String label, int max) throws BusinessException {
        String normalized = normalized(value);
        if (normalized.length() > max) {
            throw new BusinessException(label + " không được vượt quá " + max + " ký tự.");
        }
        return normalized;
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim().replace("\u0000", "");
    }

    private String safeFilenamePart(String value) {
        String safe = value.replaceAll("[^\\p{L}\\p{N}]+", "_").replaceAll("^_+|_+$", "");
        return safe.isBlank() ? "Ung_vien" : safe.substring(0, Math.min(safe.length(), 70));
    }

    private String joinNonBlank(String delimiter, String... values) {
        return java.util.Arrays.stream(values).filter(value -> value != null && !value.isBlank()).collect(java.util.stream.Collectors.joining(delimiter));
    }

    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // The file is inside the managed resume directory and can be safely cleaned by an operator.
        }
    }
}
