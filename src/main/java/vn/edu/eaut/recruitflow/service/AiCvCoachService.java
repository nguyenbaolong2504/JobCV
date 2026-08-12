package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.model.CvReviewResult;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.util.AiReviewException;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;

/**
 * Candidate-owned CV analysis use case. The controller never sees another candidate's resume,
 * and an external provider is called only after ownership plus explicit request consent succeed.
 */
public class AiCvCoachService {
    private final ResumeDAO resumeDAO;
    private final CvCoachProvider localProvider;
    private final CvCoachProvider externalProvider;
    private final String localNotice;

    public AiCvCoachService() {
        this(new ResumeDAO(), new LocalCvCoachProvider(), AiCoachConfiguration.load());
    }

    private AiCvCoachService(ResumeDAO resumeDAO, CvCoachProvider localProvider, AiCoachConfiguration configuration) {
        this(resumeDAO, localProvider,
                configuration.isExternalEnabled() ? new OpenAiCompatibleCvCoachProvider(configuration) : null,
                configuration.getDisabledNotice());
    }

    AiCvCoachService(ResumeDAO resumeDAO, CvCoachProvider localProvider, CvCoachProvider externalProvider,
                     String localNotice) {
        this.resumeDAO = resumeDAO;
        this.localProvider = localProvider;
        this.externalProvider = externalProvider;
        this.localNotice = localNotice == null ? "" : localNotice.trim();
    }

    /**
     * @param resumeId an owned resume id; null selects the candidate's default resume.
     * @param externalConsent true only after the candidate explicitly permits sending this CV to
     *                        the configured external provider for this one review.
     */
    public CvReviewResult reviewForCandidate(int candidateId, Integer resumeId, String reviewGoal, String targetRole,
                                             boolean externalConsent) throws BusinessException {
        if (candidateId <= 0) {
            throw new AiReviewException(403, "Phiên ứng viên không hợp lệ.");
        }
        Resume resume = loadOwnedResume(candidateId, resumeId);
        String extractedText = resume.getExtractedText() == null ? "" : resume.getExtractedText().trim();
        if (extractedText.isBlank()) {
            throw new AiReviewException(422,
                    "CV này chưa có nội dung có thể đọc được. Hãy tải lại một tệp PDF, DOC hoặc DOCX có văn bản rõ ràng.");
        }

        String safeGoal = bounded(reviewGoal, "Mục tiêu review", 220);
        String safeTargetRole = bounded(targetRole, "Vị trí mục tiêu", 150);
        CvReviewResult localReview;
        try {
            localReview = localProvider.review(extractedText, safeGoal, safeTargetRole);
        } catch (AiProviderException exception) {
            // The bundled provider is deterministic, but do not expose implementation details if it ever fails.
            throw new BusinessException("Không thể phân tích CV vào lúc này.", exception);
        }

        if (externalProvider == null) {
            return localReview.withSource("local", localNotice).withResumeName(resume.getFileName());
        }
        if (!externalConsent) {
            return localReview.withSource("local",
                    "Phân tích cục bộ đang được dùng. CV chỉ được gửi đến AI bên ngoài khi bạn đồng ý rõ ràng cho lần phân tích này.")
                    .withResumeName(resume.getFileName());
        }
        try {
            return externalProvider.review(extractedText, safeGoal, safeTargetRole)
                    .withSource("openai-compatible",
                            "Đã dùng nhà cung cấp AI được hệ thống cấu hình. Hãy kiểm tra lại mọi gợi ý trước khi sử dụng.")
                    .withResumeName(resume.getFileName());
        } catch (AiProviderException exception) {
            // A remote outage must not make the candidate flow unusable. The response remains useful and safe.
            return localReview.withSource("local-fallback",
                    "Dịch vụ AI bên ngoài tạm thời không phản hồi; hệ thống đã dùng phân tích cục bộ. CV không được gửi lại.")
                    .withResumeName(resume.getFileName());
        }
    }

    private Resume loadOwnedResume(int candidateId, Integer resumeId) throws BusinessException {
        try {
            Resume resume = resumeId == null || resumeId <= 0
                    ? resumeDAO.findDefaultByCandidateId(candidateId)
                    : resumeDAO.findById(resumeId);
            if (resume == null || resume.getCandidateId() != candidateId) {
                throw new AiReviewException(404, "Không tìm thấy CV thuộc tài khoản của bạn.");
            }
            return resume;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải CV để phân tích vào lúc này.", exception);
        }
    }

    private String bounded(String value, String label, int maximumLength) throws AiReviewException {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maximumLength) {
            throw new AiReviewException(400, label + " không được vượt quá " + maximumLength + " ký tự.");
        }
        return normalized;
    }
}
