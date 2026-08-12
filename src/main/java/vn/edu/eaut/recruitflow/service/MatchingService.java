package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.ResumeDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobMatchResult;
import vn.edu.eaut.recruitflow.model.JobSkill;
import vn.edu.eaut.recruitflow.model.MatchResult;
import vn.edu.eaut.recruitflow.model.Resume;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Strategy-friendly baseline weighted-skill matching implementation. */
public class MatchingService {
    private final JobDAO jobDAO;
    private final ResumeDAO resumeDAO;

    public MatchingService() {
        this(new JobDAO(), new ResumeDAO());
    }

    MatchingService(JobDAO jobDAO, ResumeDAO resumeDAO) {
        this.jobDAO = jobDAO;
        this.resumeDAO = resumeDAO;
    }

    public MatchResult calculate(Resume resume, List<JobSkill> skills) {
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        if (skills == null || skills.isEmpty()) {
            return new MatchResult(BigDecimal.ZERO, matched, missing);
        }
        String text = normalize(resume == null ? "" : resume.getExtractedText());
        int totalWeight = 0;
        int matchedWeight = 0;
        for (JobSkill skill : skills) {
            int weight = Math.max(1, skill.getWeight());
            totalWeight += weight;
            String normalizedSkill = normalize(skill.getSkillName());
            if (!normalizedSkill.isBlank() && text.contains(normalizedSkill)) {
                matchedWeight += weight;
                matched.add(skill.getSkillName());
            } else {
                missing.add(skill.getSkillName());
            }
        }
        BigDecimal score = totalWeight == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(matchedWeight * 100.0d / totalWeight).setScale(2, RoundingMode.HALF_UP);
        return new MatchResult(score, matched, missing);
    }

    public MatchResult calculateForCandidate(int candidateId, int jobId) throws BusinessException {
        try {
            Resume resume = resumeDAO.findDefaultByCandidateId(candidateId);
            if (resume == null) {
                return new MatchResult(BigDecimal.ZERO, List.of(), List.of());
            }
            Job job = jobDAO.findById(jobId);
            if (job == null) {
                throw new BusinessException("Không tìm thấy tin tuyển dụng.");
            }
            return calculate(resume, job.getSkills());
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tính mức độ phù hợp CV.", exception);
        }
    }

    public List<JobMatchResult> recommend(int candidateId, int limit) throws BusinessException {
        try {
            Resume resume = resumeDAO.findDefaultByCandidateId(candidateId);
            if (resume == null) {
                return List.of();
            }
            List<Job> jobs = jobDAO.findPublishedJobs(1, 100);
            List<JobMatchResult> results = new ArrayList<>();
            for (Job summary : jobs) {
                Job job = jobDAO.findById(summary.getId());
                if (job != null) {
                    results.add(new JobMatchResult(job, calculate(resume, job.getSkills())));
                }
            }
            results.sort(Comparator.comparing(JobMatchResult::getMatchScore).reversed()
                    .thenComparing(item -> item.getJob().getDeadline()));
            return results.stream().limit(Math.max(1, limit)).toList();
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đề xuất tin tuyển dụng.", exception);
        }
    }

    public PageResult<JobMatchResult> searchPublishedJobsForCandidate(int candidateId, String keyword, Integer departmentId,
                                                                       String location, String employmentType, int page,
                                                                       int pageSize, String sort) throws BusinessException {
        try {
            Resume resume = resumeDAO.findDefaultByCandidateId(candidateId);
            List<Job> summaries = jobDAO.search(keyword, departmentId, location, employmentType, "PUBLISHED",
                    publicSort(sort), page, pageSize);
            long total = jobDAO.count(keyword, departmentId, location, employmentType, "PUBLISHED");
            List<JobMatchResult> results = new ArrayList<>();
            for (Job summary : summaries) {
                Job detailed = jobDAO.findById(summary.getId());
                results.add(new JobMatchResult(detailed == null ? summary : detailed,
                        detailed == null ? new MatchResult(BigDecimal.ZERO, List.of(), List.of()) : calculate(resume, detailed.getSkills())));
            }
            return new PageResult<>(results, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tìm kiếm việc làm phù hợp.", exception);
        }
    }

    private String normalize(String source) {
        String value = source == null ? "" : source.toLowerCase(Locale.ROOT);
        value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return value.replaceAll("[^\\p{L}\\p{N}+#.]+", " ").replaceAll("\\s+", " ").trim();
    }

    private String publicSort(String sort) {
        if (sort == null) {
            return "newest";
        }
        return switch (sort) {
            case "deadline" -> "deadline_asc";
            case "salary" -> "salary_desc";
            default -> sort;
        };
    }
}
