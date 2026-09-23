package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.CvReviewResult;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.service.AiCvCoachService;
import vn.edu.eaut.recruitflow.service.ResumeService;
import vn.edu.eaut.recruitflow.util.AiReviewException;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.SimpleJson;
import vn.edu.eaut.recruitflow.util.ResumeStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Candidate-only endpoint for a one-time CV Coach review. It does not modify the stored CV. */
@WebServlet(name = "CandidateAiResumeReviewController", urlPatterns = "/candidate/resumes/ai-review")
public class CandidateAiResumeReviewController extends CandidateBaseController {
    private static final int JSON_REQUEST_LIMIT = 8_192;

    private AiCvCoachService aiCvCoachService;
    private ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        aiCvCoachService = new AiCvCoachService();
        resumeService = new ResumeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        if (wantsJson(request)) {
            response.setHeader("Allow", "POST");
            writeError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "method_not_allowed",
                    "Chỉ hỗ trợ phương thức POST.");
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        boolean json = wantsJson(request);
        try {
            ReviewRequest payload = reviewRequest(request);
            int candidateId = currentCandidateId(request);
            CvReviewResult review = aiCvCoachService.reviewForCandidate(candidateId, payload.resumeId(),
                    payload.reviewGoal(), payload.targetRole(), payload.externalConsent());
            if (json) {
                writeReview(response, review);
                return;
            }

            // No state was changed, so forwarding preserves the review for non-JavaScript users.
            List<Resume> resumes = resumeService.getResumes(candidateId);
            ResumeStorageUtil.refreshAvailability(getServletContext(), resumes);
            request.setAttribute("resumes", resumes);
            request.setAttribute("aiReview", review);
            view(request, response, "/WEB-INF/views/candidate/resumes.jsp", "CV của tôi | JobCV");
        } catch (AiReviewException exception) {
            handleKnownError(request, response, json, exception.getHttpStatus(), "invalid_review_request",
                    exception.getMessage());
        } catch (BusinessException exception) {
            handleKnownError(request, response, json, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "review_unavailable",
                    "Chưa thể phân tích CV lúc này. Vui lòng thử lại sau.");
        }
    }

    private ReviewRequest reviewRequest(HttpServletRequest request) throws IOException, AiReviewException {
        if (isJsonRequest(request)) {
            return jsonReviewRequest(request);
        }
        ReviewRequest payload = new ReviewRequest(optionalResumeId(RequestUtil.text(request, "resumeId")),
                RequestUtil.text(request, "reviewGoal"), RequestUtil.text(request, "targetRole"),
                booleanValue(RequestUtil.text(request, "aiReviewConsent")),
                booleanValue(RequestUtil.text(request, "externalConsent")));
        requireAnalysisConsent(payload);
        return payload;
    }

    private ReviewRequest jsonReviewRequest(HttpServletRequest request) throws IOException, AiReviewException {
        String source = readJsonRequest(request);
        try {
            Object parsed = SimpleJson.parse(source);
            if (!(parsed instanceof Map<?, ?> data)) {
                throw new AiReviewException(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ.");
            }
            ReviewRequest payload = new ReviewRequest(optionalResumeId(text(data.get("resumeId"))), text(data.get("reviewGoal")),
                    text(data.get("targetRole")), booleanValue(text(data.get("aiReviewConsent"))),
                    booleanValue(text(data.get("externalConsent"))));
            requireAnalysisConsent(payload);
            return payload;
        } catch (SimpleJson.JsonParseException exception) {
            throw new AiReviewException(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu JSON không hợp lệ.");
        }
    }

    private Integer optionalResumeId(String rawValue) throws AiReviewException {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(rawValue.trim());
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new AiReviewException(HttpServletResponse.SC_BAD_REQUEST, "CV được chọn không hợp lệ.");
        }
    }

    private String readJsonRequest(HttpServletRequest request) throws IOException, AiReviewException {
        byte[] source = request.getInputStream().readNBytes(JSON_REQUEST_LIMIT + 1);
        if (source.length > JSON_REQUEST_LIMIT) {
            throw new AiReviewException(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu gửi lên quá lớn.");
        }
        return new String(source, StandardCharsets.UTF_8);
    }

    private void writeReview(HttpServletResponse response, CvReviewResult review) throws IOException {
        Map<String, Object> reviewData = new LinkedHashMap<>();
        reviewData.put("resumeName", review.getResumeName());
        reviewData.put("overallScore", review.getOverallScore());
        reviewData.put("score", review.getScore());
        reviewData.put("summary", review.getSummary());
        reviewData.put("strengths", review.getStrengths());
        reviewData.put("improvements", review.getImprovements());
        reviewData.put("missingSections", review.getMissingSections());
        reviewData.put("keywordSuggestions", review.getKeywordSuggestions());
        reviewData.put("suggestedBullets", review.getSuggestedBullets());
        reviewData.put("rewrittenSummary", review.getRewrittenSummary());
        reviewData.put("provider", review.getProvider());
        reviewData.put("notice", review.getNotice());
        reviewData.put("disclaimer", review.getDisclaimer());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("overallScore", review.getOverallScore());
        payload.put("score", review.getScore());
        payload.put("provider", review.getProvider());
        payload.put("notice", review.getNotice());
        payload.put("review", reviewData);
        writeJson(response, HttpServletResponse.SC_OK, payload);
    }

    private void handleKnownError(HttpServletRequest request, HttpServletResponse response, boolean json, int status,
                                  String code, String message) throws IOException {
        if (json) {
            writeError(response, status, code, message);
            return;
        }
        redirectWithError(request, response, "/candidate/resumes", message);
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", false);
        payload.put("code", code);
        payload.put("message", message);
        payload.put("error", error);
        writeJson(response, status, payload);
    }

    private void writeJson(HttpServletResponse response, int status, Map<String, Object> payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(SimpleJson.stringify(payload));
    }

    private boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("application/json");
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("application/json");
    }

    private boolean booleanValue(String value) {
        return "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private void requireAnalysisConsent(ReviewRequest payload) throws AiReviewException {
        if (!payload.analysisConsent()) {
            throw new AiReviewException(HttpServletResponse.SC_BAD_REQUEST,
                    "Bạn cần đồng ý để hệ thống phân tích nội dung CV đã chọn.");
        }
    }

    private record ReviewRequest(Integer resumeId, String reviewGoal, String targetRole, boolean analysisConsent,
                                 boolean externalConsent) {
    }
}
