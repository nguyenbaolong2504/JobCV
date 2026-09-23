package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.model.HRDashboardStats;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobMatchResult;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Read-only career agent. It orchestrates the application's domain services and returns
 * evidence-backed suggestions, while every consequential action (apply/reject/hire/offer)
 * remains an explicit user operation in the corresponding screen.
 */
public class CareerAgentService {
    private final JobService jobService;
    private final MatchingService matchingService;
    private final CompanyProfileService companyService;
    private final DashboardService dashboardService;
    private final InterviewService interviewService;

    public CareerAgentService() {
        this(new JobService(), new MatchingService(), new CompanyProfileService(),
                new DashboardService(), new InterviewService());
    }

    CareerAgentService(JobService jobService, MatchingService matchingService,
                       CompanyProfileService companyService, DashboardService dashboardService,
                       InterviewService interviewService) {
        this.jobService = jobService;
        this.matchingService = matchingService;
        this.companyService = companyService;
        this.dashboardService = dashboardService;
        this.interviewService = interviewService;
    }

    public Map<String, Object> answer(String question, Integer userId, String role) throws BusinessException {
        String value = question == null ? "" : question.trim();
        if (value.isBlank()) throw new BusinessException("Vui lòng nhập câu hỏi.");
        String intent = normalize(value);
        String normalizedRole = role == null ? "GUEST" : role.trim().toUpperCase(Locale.ROOT);

        if ("HR".equals(normalizedRole) && containsAny(intent,
                "tinh hinh", "can xu ly", "tong hop", "tuyen dung", "ho so", "ung vien")) {
            return hrSummary(userId);
        }
        if ("INTERVIEWER".equals(normalizedRole) && containsAny(intent,
                "lich", "phong van", "can lam", "ung vien")) {
            return interviewerSchedule(userId);
        }
        if ("ADMIN".equals(normalizedRole) && containsAny(intent, "admin", "quan tri", "phan quyen")) {
            return response("Bạn có thể quản lý tài khoản, công ty, phân quyền và danh mục trong khu vực quản trị.",
                    action("Mở trang quản trị", "/admin/dashboard"));
        }

        if (containsAny(intent, "cong ty", "doanh nghiep")) {
            Map<String, Object> companyAnswer = companyResearch(value, userId, normalizedRole);
            if (companyAnswer != null) return companyAnswer;
        }
        if (containsAny(intent, "cv", "resume")) {
            return response("AI CV Coach có thể đọc CV, đối chiếu kỹ năng với công việc và chỉ ra phần còn thiếu. "
                            + "Bạn vẫn là người quyết định chỉnh sửa hoặc ứng tuyển.",
                    action("Mở AI CV Coach", "/candidate/resumes"),
                    action("Tạo CV theo mẫu", "/candidate/cv-builder"));
        }
        if (containsAny(intent, "luyen phong van", "chuan bi phong van", "mock interview")) {
            return response("Agent luyện phỏng vấn sẽ dựa trên CV và vị trí bạn đã ứng tuyển. "
                            + "Hãy mở lịch phỏng vấn hoặc đơn ứng tuyển để chọn đúng vị trí cần luyện.",
                    action("Xem lịch phỏng vấn", "/candidate/interviews"),
                    action("Xem đơn ứng tuyển", "/candidate/applications"));
        }
        if ("CANDIDATE".equals(normalizedRole)
                && containsAny(intent, "phu hop nhat", "goi y viec", "de xuat viec", "hop voi cv")) {
            return candidateRecommendations(userId);
        }
        if (containsAny(intent, "viec", "job", "tuyen", "intern", "fresher", "developer", "engineer")) {
            return searchJobs(value, userId, normalizedRole);
        }
        if (containsAny(intent, "don", "trang thai", "ung tuyen")) {
            return response("Bạn có thể xem trạng thái và lịch sử xử lý của từng đơn. Agent không tự gửi hoặc rút đơn thay bạn.",
                    action("Theo dõi đơn ứng tuyển", "/candidate/applications"));
        }
        if (containsAny(intent, "dang ky", "tai khoan")) {
            return response("Người tìm việc được kích hoạt ngay. Tài khoản Nhà tuyển dụng cần khai báo tổ chức và chờ Admin phê duyệt.",
                    action("Đăng ký JobCV", "/register"));
        }
        return response("Mình có thể tìm và xếp hạng việc đang tuyển, phân tích CV, nghiên cứu công ty, "
                        + "hỗ trợ chuẩn bị phỏng vấn hoặc tổng hợp công việc tuyển dụng theo đúng quyền của bạn.",
                action("Khám phá việc làm", "/jobs"), action("Tạo CV", "/candidate/cv-builder"));
    }

    private Map<String, Object> candidateRecommendations(Integer candidateId) throws BusinessException {
        if (candidateId == null) {
            return response("Hãy đăng nhập tài khoản Người tìm việc để Agent đọc CV mặc định và xếp hạng công việc phù hợp.",
                    action("Đăng nhập", "/login"));
        }
        List<JobMatchResult> results = matchingService.recommend(candidateId, 3);
        if (results.isEmpty()) {
            return response("Bạn chưa có CV mặc định hoặc hiện chưa có công việc còn chỗ phù hợp để xếp hạng.",
                    action("Quản lý CV", "/candidate/resumes"), action("Xem việc đang tuyển", "/jobs"));
        }
        StringBuilder text = new StringBuilder("Các vị trí phù hợp nhất theo CV mặc định của bạn:\n");
        List<Map<String, String>> actions = new ArrayList<>();
        int rank = 1;
        for (JobMatchResult result : results) {
            Job job = result.getJob();
            text.append(rank++).append(". ").append(job.getTitle()).append(" – ")
                    .append(job.getCompanyName()).append(" (phù hợp ")
                    .append(result.getMatchScore().stripTrailingZeros().toPlainString()).append("%, còn ")
                    .append(job.getRemainingPositions()).append(" vị trí)");
            if (!result.getMatchedSkills().isEmpty()) text.append("\n   Đã khớp: ").append(String.join(", ", result.getMatchedSkills()));
            if (!result.getMissingSkills().isEmpty()) text.append("\n   Nên bổ sung: ").append(String.join(", ", result.getMissingSkills()));
            text.append('\n');
            actions.add(action("Xem " + job.getTitle(), "/jobs/detail?id=" + job.getId()));
        }
        return response(text.toString().trim(), actions);
    }

    private Map<String, Object> searchJobs(String question, Integer userId, String role) throws BusinessException {
        String keyword = extractJobKeyword(question);
        if ("CANDIDATE".equals(role) && userId != null) {
            PageResult<JobMatchResult> page = matchingService.searchPublishedJobsForCandidate(
                    userId, keyword, null, null, null, 1, 3, "newest");
            if (!page.getItems().isEmpty()) {
                StringBuilder text = new StringBuilder("Mình tìm thấy các tin còn hạn và còn vị trí:\n");
                List<Map<String, String>> actions = new ArrayList<>();
                for (JobMatchResult item : page.getItems()) {
                    Job job = item.getJob();
                    text.append("• ").append(job.getTitle()).append(" – ").append(job.getCompanyName())
                            .append(" · còn ").append(job.getRemainingPositions()).append(" vị trí")
                            .append(" · khớp CV ").append(item.getMatchScore().stripTrailingZeros().toPlainString()).append("%\n");
                    actions.add(action("Xem " + job.getTitle(), "/jobs/detail?id=" + job.getId()));
                }
                return response(text.toString().trim(), actions);
            }
        } else {
            PageResult<Job> page = jobService.searchPublishedJobs(keyword, null, null, null, 1, 3, "newest");
            if (!page.getItems().isEmpty()) {
                StringBuilder text = new StringBuilder("Các tin đang tuyển và còn vị trí:\n");
                List<Map<String, String>> actions = new ArrayList<>();
                for (Job job : page.getItems()) {
                    text.append("• ").append(job.getTitle()).append(" – ").append(job.getCompanyName())
                            .append(" · còn ").append(job.getRemainingPositions()).append(" vị trí\n");
                    actions.add(action("Xem " + job.getTitle(), "/jobs/detail?id=" + job.getId()));
                }
                return response(text.toString().trim(), actions);
            }
        }
        String href = "/jobs" + (keyword.isBlank() ? "" : "?keyword=" + urlEncode(keyword));
        return response("Chưa có tin còn hạn và còn vị trí khớp “" + (keyword.isBlank() ? question : keyword) + "”.",
                action("Mở bộ lọc việc làm", href));
    }

    private Map<String, Object> companyResearch(String question, Integer candidateId, String role) throws BusinessException {
        String normalizedQuestion = normalize(question);
        for (CompanyProfile company : companyService.getCompanies("")) {
            if (!normalizedQuestion.contains(normalize(company.getName()))) continue;
            List<Job> jobs = companyService.getOpenJobs(company.getId());
            StringBuilder text = new StringBuilder(company.getName()).append(" hiện có ")
                    .append(jobs.size()).append(" tin còn chỗ đang tuyển.");
            List<Map<String, String>> actions = new ArrayList<>();
            actions.add(action("Xem công ty", "/companies/detail?id=" + company.getId()));
            for (Job job : jobs.stream().limit(3).toList()) {
                text.append("\n• ").append(job.getTitle()).append(" – còn ")
                        .append(job.getRemainingPositions()).append(" vị trí");
                if ("CANDIDATE".equals(role) && candidateId != null) {
                    text.append(" · khớp CV ").append(matchingService.calculateForCandidate(candidateId, job.getId())
                            .getMatchScore().stripTrailingZeros().toPlainString()).append("%");
                }
                actions.add(action("Xem " + job.getTitle(), "/jobs/detail?id=" + job.getId()));
            }
            return response(text.toString(), actions);
        }
        return null;
    }

    private Map<String, Object> hrSummary(Integer hrId) throws BusinessException {
        if (hrId == null) return response("Vui lòng đăng nhập tài khoản HR.", action("Đăng nhập", "/login"));
        HRDashboardStats stats = dashboardService.getHrDashboardStats(hrId);
        String text = "Tổng hợp tuyển dụng của công ty bạn:\n"
                + "• " + stats.getActiveJobs() + " tin đang mở\n"
                + "• " + stats.getNewApplications() + " hồ sơ mới cần xem\n"
                + "• " + stats.getShortlistedCandidates() + " ứng viên shortlist\n"
                + "• " + stats.getUpcomingInterviews() + " lịch phỏng vấn sắp tới\n"
                + "• " + stats.getDraftOffers() + " offer nháp\n"
                + "Ưu tiên hồ sơ mới và lịch sắp tới. Agent chỉ tổng hợp, không tự loại, tuyển hay gửi offer.";
        return response(text, action("Xử lý ứng viên", "/hr/applications"),
                action("Xem lịch phỏng vấn", "/hr/interviews"), action("Quản lý tin", "/hr/jobs"));
    }

    private Map<String, Object> interviewerSchedule(Integer interviewerId) throws BusinessException {
        if (interviewerId == null) return response("Vui lòng đăng nhập tài khoản Interviewer.", action("Đăng nhập", "/login"));
        List<Interview> interviews = interviewService.findUpcomingForInterviewer(interviewerId);
        if (interviews.isEmpty()) {
            return response("Bạn chưa có lịch phỏng vấn sắp tới được phân công.",
                    action("Mở lịch của tôi", "/interviewer/interviews"));
        }
        StringBuilder text = new StringBuilder("Lịch phỏng vấn được phân công cho chính bạn:\n");
        for (Interview interview : interviews.stream().limit(5).toList()) {
            text.append("• ").append(interview.getInterviewDate()).append(' ')
                    .append(interview.getStartTime()).append(" – ").append(interview.getCandidateName())
                    .append(" / ").append(interview.getJobTitle()).append('\n');
        }
        return response(text.toString().trim(), action("Mở lịch của tôi", "/interviewer/interviews"));
    }

    private String extractJobKeyword(String question) {
        String value = question == null ? "" : question.trim();
        value = value.replaceFirst("(?iu)^(tôi|mình|em|cho tôi)?\\s*(muốn|cần)?\\s*(tìm|kiếm|xem|gợi ý)?\\s*(việc làm|công việc|việc|job)?\\s*", "");
        value = value.replaceFirst("(?iu)\\s*(phù hợp nhất|phù hợp|đang tuyển|cho tôi|với cv.*)$", "").trim();
        return value.length() > 100 ? value.substring(0, 100) : value;
    }

    private boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) if (value.contains(fragment)) return true;
        return false;
    }

    private String normalize(String source) {
        String value = source == null ? "" : source.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replaceAll("[^a-z0-9+#.]+", " ").replaceAll("\\s+", " ").trim();
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    @SafeVarargs
    private Map<String, Object> response(String text, Map<String, String>... actions) {
        return response(text, List.of(actions));
    }

    private Map<String, Object> response(String text, List<Map<String, String>> actions) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("text", text);
        result.put("actions", actions);
        return result;
    }

    private Map<String, String> action(String label, String href) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("label", label);
        result.put("href", href);
        return result;
    }
}
