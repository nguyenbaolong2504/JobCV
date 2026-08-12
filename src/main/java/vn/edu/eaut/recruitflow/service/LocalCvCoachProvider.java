package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.model.CvReviewResult;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic offline CV review. It is intentionally conservative: every suggestion is a
 * template or an omission found in the extracted text, never a fabricated achievement.
 */
public class LocalCvCoachProvider implements CvCoachProvider {
    private static final String DISCLAIMER = "Gợi ý được tạo tự động. Chỉ giữ lại thông tin đúng với "
            + "kinh nghiệm thực tế; không thêm kỹ năng hoặc thành tích không có thật.";
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[\\w.+-]+@[\\w.-]+\\.[a-z]{2,}\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(?:\\+?84|0)(?:[ .()-]?\\d){8,10}(?!\\d)");
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N}+#.-]*");
    private static final Pattern METRIC = Pattern.compile("(?i)(?:\\d+\\s*%|\\d+\\s*(?:nguoi|khach hang|du an|thang|ngay|gio))");

    @Override
    public CvReviewResult review(String resumeText, String reviewGoal, String targetRole) {
        String normalized = normalize(resumeText);
        int wordCount = countWords(resumeText);
        boolean hasEmail = EMAIL.matcher(resumeText).find();
        boolean hasPhone = PHONE.matcher(resumeText).find();
        boolean hasSummary = containsAny(normalized, "tom tat", "gioi thieu", "muc tieu", "summary", "profile",
                "objective", "about me");
        boolean hasExperience = containsAny(normalized, "kinh nghiem", "experience", "work history", "employment");
        boolean hasEducation = containsAny(normalized, "hoc van", "education", "university", "dai hoc", "cao dang");
        boolean hasSkills = containsAny(normalized, "ky nang", "skills", "technical skills", "cong nghe");
        boolean hasProjects = containsAny(normalized, "du an", "projects", "project experience");
        boolean hasCertificates = containsAny(normalized, "chung chi", "certification", "certificate", "certificates");
        boolean hasMetrics = METRIC.matcher(normalized).find();

        int score = 0;
        score += hasEmail ? 10 : 0;
        score += hasPhone ? 5 : 0;
        score += hasSummary ? 10 : 0;
        score += hasExperience ? 20 : 0;
        score += hasEducation ? 13 : 0;
        score += hasSkills ? 15 : 0;
        score += hasProjects ? 12 : 0;
        score += hasCertificates ? 5 : 0;
        score += wordCount >= 200 && wordCount <= 1200 ? 10 : wordCount >= 100 ? 5 : 0;

        List<String> strengths = new ArrayList<>();
        if (hasEmail) {
            strengths.add("Có địa chỉ email để nhà tuyển dụng liên hệ.");
        }
        if (hasSkills) {
            strengths.add("CV đã có mục kỹ năng/công nghệ để hệ thống ATS dễ nhận diện.");
        }
        if (hasExperience) {
            strengths.add("CV đã có phần kinh nghiệm làm việc.");
        }
        if (hasProjects) {
            strengths.add("CV đã có phần dự án để minh hoạ năng lực thực tế.");
        }
        if (hasMetrics) {
            strengths.add("CV đã có dấu hiệu sử dụng số liệu hoặc kết quả đo được.");
        }
        if (wordCount >= 200 && wordCount <= 1200) {
            strengths.add("Độ dài nội dung hiện phù hợp để bắt đầu tối ưu.");
        }
        if (strengths.isEmpty()) {
            strengths.add("Đã có nội dung trích xuất từ CV để bắt đầu hoàn thiện theo từng mục.");
        }

        List<String> missingSections = new ArrayList<>();
        if (!hasEmail) {
            missingSections.add("Email liên hệ");
        }
        if (!hasPhone) {
            missingSections.add("Số điện thoại liên hệ");
        }
        if (!hasSummary) {
            missingSections.add("Tóm tắt nghề nghiệp hoặc mục tiêu");
        }
        if (!hasExperience) {
            missingSections.add("Kinh nghiệm làm việc");
        }
        if (!hasEducation) {
            missingSections.add("Học vấn");
        }
        if (!hasSkills) {
            missingSections.add("Kỹ năng chuyên môn");
        }
        if (!hasProjects) {
            missingSections.add("Dự án tiêu biểu");
        }

        List<String> improvements = new ArrayList<>();
        for (String missing : missingSections.stream().limit(3).toList()) {
            improvements.add("Bổ sung mục “" + missing + "” với thông tin ngắn gọn, có thể kiểm chứng.");
        }
        if (!hasMetrics) {
            improvements.add("Mỗi kinh nghiệm hoặc dự án nên có ít nhất một kết quả đo được (thời gian, tỷ lệ, số người dùng...), nếu đúng thực tế.");
        }
        if (wordCount < 180) {
            improvements.add("Nội dung còn ngắn; hãy bổ sung nhiệm vụ, công cụ đã dùng và kết quả thật của từng kinh nghiệm/dự án.");
        } else if (wordCount > 1200) {
            improvements.add("Nội dung khá dài; ưu tiên các kinh nghiệm gần vị trí ứng tuyển và rút gọn thông tin lặp lại.");
        }
        if (improvements.isEmpty()) {
            improvements.add("Tùy chỉnh từ khóa và thứ tự kinh nghiệm cho từng vị trí ứng tuyển thay vì dùng một CV cho mọi công việc.");
        }

        List<String> keywordSuggestions = missingKeywords(normalized, targetRole);
        List<String> suggestedBullets = suggestedBullets(hasExperience, hasProjects, reviewGoal, targetRole);
        String summary = buildSummary(wordCount, missingSections, targetRole);
        String rewrittenSummary = summaryTemplate(targetRole);

        return new CvReviewResult("", score, summary, strengths, improvements, missingSections, keywordSuggestions,
                suggestedBullets, rewrittenSummary, getName(),
                "Phân tích cục bộ đang được dùng; CV chưa được gửi đến dịch vụ AI bên ngoài.", DISCLAIMER);
    }

    @Override
    public String getName() {
        return "local";
    }

    private List<String> missingKeywords(String normalizedResume, String targetRole) {
        Set<String> candidates = new LinkedHashSet<>();
        String role = normalize(targetRole);
        if (containsAny(role, "backend", "java", "lap trinh vien", "software developer")) {
            candidates.addAll(List.of("Java", "SQL", "REST API", "Git", "Unit test"));
        } else if (containsAny(role, "frontend", "front end", "web", "ui")) {
            candidates.addAll(List.of("JavaScript", "HTML/CSS", "REST API", "Git", "Responsive design"));
        } else if (containsAny(role, "tester", "qa", "kiem thu")) {
            candidates.addAll(List.of("Test case", "SQL", "API testing", "Bug tracking", "Regression test"));
        } else if (containsAny(role, "business analyst", "ba", "phan tich nghiep vu")) {
            candidates.addAll(List.of("Requirement analysis", "BPMN/UML", "SQL", "User story", "Stakeholder"));
        } else {
            candidates.addAll(List.of("Kỹ năng/công cụ đúng với vị trí ứng tuyển", "Kết quả định lượng", "Công cụ cộng tác (ví dụ Git)",
                    "Từ khóa trong mô tả công việc"));
        }
        List<String> result = new ArrayList<>();
        for (String keyword : candidates) {
            if (!containsKeyword(normalizedResume, keyword)) {
                result.add(keyword);
            }
            if (result.size() == 5) {
                break;
            }
        }
        return result;
    }

    private List<String> suggestedBullets(boolean hasExperience, boolean hasProjects, String reviewGoal, String targetRole) {
        List<String> result = new ArrayList<>();
        String roleSuffix = targetRole == null || targetRole.isBlank() ? "vị trí ứng tuyển"
                : "vị trí " + targetRole.trim();
        if (!hasExperience) {
            result.add("Mẫu kinh nghiệm: “Thực hiện [nhiệm vụ thật] bằng [công cụ đã dùng], giúp [kết quả đo được] cho [nhóm/người dùng].”");
        } else {
            result.add("Viết lại từng kinh nghiệm theo mẫu: “Hành động + công cụ + kết quả đo được”, chỉ dùng số liệu có thể kiểm chứng.");
        }
        if (!hasProjects) {
            result.add("Mẫu dự án: “Xây dựng [tính năng thật] cho " + roleSuffix
                    + "; phụ trách [phần việc]; sử dụng [công nghệ thực tế]; kết quả [đo được nếu có].”");
        } else {
            result.add("Với mỗi dự án, nêu rõ vai trò, phạm vi công việc, công nghệ đã dùng và kết quả thực tế.");
        }
        if (normalize(reviewGoal).contains("ats")) {
            result.add("Giữ tiêu đề mục đơn giản và lặp lại từ khóa đúng với mô tả công việc ở nơi phản ánh kinh nghiệm thật.");
        }
        return result;
    }

    private String buildSummary(int wordCount, List<String> missingSections, String targetRole) {
        String role = targetRole == null || targetRole.isBlank() ? "vị trí ứng tuyển" : targetRole.trim();
        if (missingSections.isEmpty()) {
            return "CV có " + wordCount + " từ và đã có các mục cơ bản. Hãy ưu tiên tinh chỉnh từ khóa, kết quả và thứ tự nội dung cho " + role + ".";
        }
        String priority = String.join(", ", missingSections.stream().limit(2).toList());
        return "CV có " + wordCount + " từ. Ưu tiên bổ sung " + priority + " trước khi tối ưu cho " + role + ".";
    }

    private String summaryTemplate(String targetRole) {
        String role = targetRole == null || targetRole.isBlank() ? "[vị trí ứng tuyển]" : targetRole.trim();
        return "Ứng viên định hướng " + role
                + ", có kinh nghiệm thực tế về [lĩnh vực] và thế mạnh ở [2–3 kỹ năng đúng sự thật]. "
                + "Mong muốn đóng góp bằng [giá trị/kết quả đã từng đạt được].";
    }

    private boolean containsKeyword(String normalizedResume, String keyword) {
        String normalizedKeyword = normalize(keyword).replace('/', ' ');
        return normalizedResume.contains(normalizedKeyword)
                || (normalizedKeyword.equals("html css") && normalizedResume.contains("html") && normalizedResume.contains("css"));
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(normalize(term))) {
                return true;
            }
        }
        return false;
    }

    private int countWords(String source) {
        Matcher matcher = WORD.matcher(source == null ? "" : source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private String normalize(String source) {
        String value = source == null ? "" : source.toLowerCase(Locale.ROOT);
        value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return value.replaceAll("\\s+", " ").trim();
    }
}
