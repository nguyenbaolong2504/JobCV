package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.ResumeParser;
import vn.edu.eaut.recruitflow.util.UploadUtil;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
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
            List<Resume> resumes = resumeDAO.findByCandidateId(candidateId);
            resumes.forEach(resume -> resume.setFileAvailable(isFileAvailable(resume)));
            return resumes;
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
            resume.setFileAvailable(isFileAvailable(resume));
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
            Resume resume = resumeDAO.findDefaultByCandidateId(candidateId);
            if (resume != null) resume.setFileAvailable(isFileAvailable(resume));
            return resume;
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
            resume.setFileName(storedFile.getFileName().toString());
            resume.setFilePath(storedFile.toAbsolutePath().toString());
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
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null || resume.getCandidateId() != candidateId) {
                throw new BusinessException("Không tìm thấy CV thuộc tài khoản của bạn.");
            }
            if (!isFileAvailable(resume)) {
                throw new BusinessException("Tệp của CV này không còn trên máy chủ. Vui lòng tải CV mới trước khi đặt mặc định.");
            }
            if (!resumeDAO.setDefault(candidateId, resumeId)) {
                throw new BusinessException("Không tìm thấy CV thuộc tài khoản của bạn.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đặt CV mặc định.", exception);
        }
    }

    public void delete(int candidateId, int resumeId) throws BusinessException {
        Resume resume = getResumeForCandidate(candidateId, resumeId);
        try {
            if (!resumeDAO.delete(resumeId, candidateId)) {
                throw new BusinessException("Không thể xóa CV.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("CV đã được dùng cho đơn ứng tuyển nên không thể xóa.", exception);
        }
        try {
            Files.deleteIfExists(Path.of(resume.getFilePath()));
        } catch (IOException ignored) {
            // Metadata has been deleted successfully; an administrator can clean an orphan file later.
        }
    }

    /**
     * A metadata row is not usable for applications/downloads when its managed file was lost.
     * This never follows a symlink or exposes a path; it only gives the UI a safe availability flag.
     */
    public boolean isFileAvailable(Resume resume) {
        if (resume == null || resume.getFilePath() == null || resume.getFilePath().isBlank()) return false;
        try {
            Path stored = Path.of(resume.getFilePath()).toAbsolutePath().normalize();
            return Files.isRegularFile(stored) && Files.isReadable(stored);
        } catch (InvalidPathException | SecurityException exception) {
            return false;
        }
    }
}
