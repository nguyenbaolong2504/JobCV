package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.ResumeParser;
import vn.edu.eaut.recruitflow.util.UploadUtil;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.ResumeStorageUtil;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;

/** Keeps file-system and database resume metadata consistent as far as possible. */
public class ResumeService {
    private final ResumeDAO resumeDAO;

    public ResumeService() {
        this(new ResumeDAO());
    }

    ResumeService(ResumeDAO resumeDAO) {
        this.resumeDAO = resumeDAO;
    }

    public List<Resume> getResumes(int candidateId) throws BusinessException {
        try {
            return resumeDAO.findByCandidateId(candidateId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách CV.", exception);
        }
    }

    public Resume getResumeForCandidate(int candidateId, int resumeId) throws BusinessException {
        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null || resume.getCandidateId() != candidateId) {
                throw new BusinessException("Không tìm thấy CV thuộc tài khoản của bạn.");
            }
            return resume;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải CV.", exception);
        }
    }

    /** The caller must already have verified an HR/Admin/interviewer assignment scope. */
    public Resume getResumeForStaff(int resumeId) throws BusinessException {
        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                throw new BusinessException("Không tìm thấy CV.");
            }
            return resume;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải CV.", exception);
        }
    }

    public Resume getDefaultResume(int candidateId) throws BusinessException {
        try {
            return resumeDAO.findDefaultByCandidateId(candidateId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải CV mặc định.", exception);
        }
    }

    public Resume upload(int candidateId, Part part, Path uploadDirectory) throws BusinessException {
        UploadUtil.validateResumePart(part);
        String extension = UploadUtil.extension(part.getSubmittedFileName());
        String extractedText;
        try {
            extractedText = ResumeParser.extractText(part.getInputStream(), extension);
        } catch (IOException exception) {
            throw new BusinessException("Không thể đọc nội dung CV. Vui lòng kiểm tra lại tệp.", exception);
        }

        Path storedFile = null;
        try {
            storedFile = UploadUtil.storeResume(part, candidateId, uploadDirectory);
            Resume resume = new Resume();
            resume.setCandidateId(candidateId);
            resume.setFileName(originalFileName(part.getSubmittedFileName(), extension));
            resume.setFilePath(ResumeStorageUtil.portableStoredPath(storedFile));
            resume.setFileType(extension);
            resume.setFileSize(part.getSize());
            resume.setExtractedText(extractedText);
            resume.setDefaultResume(resumeDAO.countByCandidateId(candidateId) == 0);
            resumeDAO.insert(resume);
            return resume;
        } catch (IOException | SQLException exception) {
            if (storedFile != null) {
                try {
                    Files.deleteIfExists(storedFile);
                } catch (IOException ignored) {
                    // The orphan will be safe to remove manually; the database row was not inserted.
                }
            }
            throw new BusinessException("Không thể lưu CV. Vui lòng thử lại.", exception);
        }
    }

    public void setDefault(int candidateId, int resumeId) throws BusinessException {
        try {
            if (!resumeDAO.setDefault(candidateId, resumeId)) {
                throw new BusinessException("Không tìm thấy CV thuộc tài khoản của bạn.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đặt CV mặc định.", exception);
        }
    }

    public void delete(int candidateId, int resumeId, Path uploadDirectory) throws BusinessException {
        Resume resume = getResumeForCandidate(candidateId, resumeId);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (!resumeDAO.delete(connection, resumeId, candidateId)) {
                    throw new BusinessException("Không thể xóa CV.");
                }
                if (resume.isDefaultResume()) {
                    Resume replacement = resumeDAO.findNewestByCandidateId(connection, candidateId);
                    if (replacement != null && !resumeDAO.setDefault(connection, candidateId, replacement.getId())) {
                        throw new BusinessException("Không thể chọn CV mặc định thay thế.");
                    }
                }
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("CV đã được dùng cho đơn ứng tuyển nên không thể xóa.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("CV đã được dùng cho đơn ứng tuyển nên không thể xóa.", exception);
        }
        try {
            Files.deleteIfExists(ResumeStorageUtil.resolveStoredFile(uploadDirectory, resume.getFilePath()));
        } catch (IOException | BusinessException ignored) {
            // Metadata has been deleted successfully; an administrator can clean an orphan file later.
        }
    }

    private String originalFileName(String submittedFileName, String extension) {
        String value = submittedFileName == null ? "" : submittedFileName.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\t]", "_").trim();
        if (value.isBlank()) {
            return "resume." + extension;
        }
        return value.length() <= 255 ? value : value.substring(value.length() - 255);
    }
}
