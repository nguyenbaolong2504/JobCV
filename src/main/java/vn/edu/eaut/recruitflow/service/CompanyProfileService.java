package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.CompanyProfileDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.RecruiterProfileDAO;
import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.RecruiterProfile;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Employer directory backed by recruiter profiles and the jobs they actually own. */
public class CompanyProfileService {
    private static final Pattern PHONE = Pattern.compile("^[0-9+() .-]{6,30}$");
    private final CompanyProfileDAO companyDAO;
    private final RecruiterProfileDAO recruiterProfileDAO;
    private final JobDAO jobDAO;
    private final AuditLogService auditLogService;

    public CompanyProfileService() {
        this.companyDAO = new CompanyProfileDAO();
        this.recruiterProfileDAO = new RecruiterProfileDAO();
        this.jobDAO = new JobDAO();
        this.auditLogService = new AuditLogService();
    }

    public List<CompanyProfile> getCompanies(String keyword) throws BusinessException {
        return getCompanies(keyword, "jobs");
    }

    public List<CompanyProfile> getCompanies(String keyword, String sort) throws BusinessException {
        return searchCompanies(keyword, true, sort, 1, 24).getItems();
    }

    public PageResult<CompanyProfile> searchCompanies(String keyword, boolean publicOnly, String sort,
                                                       int page, int pageSize) throws BusinessException {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.length() > 100) throw new BusinessException("Từ khóa không được vượt quá 100 ký tự.");
        try {
            List<CompanyProfile> companies = companyDAO.search(normalized, publicOnly, sort, page, pageSize);
            companies.forEach(this::attachHighlights);
            return new PageResult<>(companies, page, pageSize, companyDAO.count(normalized, publicOnly));
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách doanh nghiệp.", exception);
        }
    }

    public List<CompanyProfile> getFeaturedCompanies(int limit) throws BusinessException {
        return searchCompanies("", true, "jobs", 1, 24).getItems().stream()
                .filter(company -> company.isVerified() && company.getOpenJobs() > 0)
                .limit(Math.max(1, Math.min(limit, 12)))
                .toList();
    }

    public CompanyProfile getCompany(int id) throws BusinessException {
        try {
            CompanyProfile company = companyDAO.findById(id, true);
            if (company == null) throw new BusinessException("Không tìm thấy doanh nghiệp.");
            attachHighlights(company);
            return company;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông tin doanh nghiệp.", exception);
        }
    }

    public CompanyProfile getCompanyByOwner(int ownerUserId) throws BusinessException {
        try {
            CompanyProfile company = companyDAO.findByOwner(ownerUserId);
            if (company == null) throw new BusinessException("Nhà tuyển dụng chưa hoàn thiện hồ sơ doanh nghiệp.");
            attachHighlights(company);
            return company;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông tin doanh nghiệp.", exception);
        }
    }

    public RecruiterProfile getRecruiterProfile(int ownerUserId) throws BusinessException {
        try {
            RecruiterProfile profile = recruiterProfileDAO.findByUserId(ownerUserId);
            if (profile == null) throw new BusinessException("Không tìm thấy hồ sơ doanh nghiệp.");
            return profile;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải hồ sơ doanh nghiệp.", exception);
        }
    }

    public void updateRecruiterProfile(int ownerUserId, RecruiterProfile profile) throws BusinessException {
        if (profile == null || profile.getUserId() != ownerUserId) throw new BusinessException("Hồ sơ doanh nghiệp không hợp lệ.");
        validate(profile);
        try {
            if (!recruiterProfileDAO.updateCompanyProfile(profile)) throw new BusinessException("Không thể cập nhật hồ sơ doanh nghiệp.");
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật hồ sơ doanh nghiệp.", exception);
        }
    }

    public void setVerified(int companyId, boolean verified, int actorId, String ipAddress) throws BusinessException {
        try {
            CompanyProfile company = companyDAO.findById(companyId, false);
            if (company == null) throw new BusinessException("Không tìm thấy doanh nghiệp.");
            if (verified && (company.getDescription() == null || company.getDescription().isBlank())) {
                throw new BusinessException("Doanh nghiệp phải hoàn thiện phần giới thiệu trước khi xác thực.");
            }
            if (!companyDAO.setVerified(companyId, verified)) throw new BusinessException("Không thể cập nhật trạng thái xác thực.");
            auditLogService.record(actorId, verified ? "COMPANY_VERIFIED" : "COMPANY_VERIFICATION_REVOKED",
                    "recruiter_profiles", companyId,
                    (verified ? "Verified company " : "Revoked verification for company ") + company.getName(), ipAddress);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật trạng thái xác thực doanh nghiệp.", exception);
        }
    }

    public List<Job> getOpenJobs(int companyId) throws BusinessException {
        CompanyProfile company = getCompany(companyId);
        try {
            return jobDAO.searchPublishedByOwner(company.getOwnerUserId(), new JobSearchCriteria(), "newest", 1, 24);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải việc làm của doanh nghiệp.", exception);
        }
    }

    private void validate(RecruiterProfile profile) throws BusinessException {
        requireLength(profile.getOrganizationName(), "Tên doanh nghiệp", 2, 150);
        requireLength(profile.getJobTitle(), "Chức danh người liên hệ", 2, 100);
        if (profile.getWorkPhone() == null || !PHONE.matcher(profile.getWorkPhone()).matches()) throw new BusinessException("Số điện thoại công việc không hợp lệ.");
        optionalLength(profile.getIndustry(), "Lĩnh vực", 120);
        optionalLength(profile.getCompanySize(), "Quy mô", 60);
        optionalLength(profile.getAddress(), "Địa chỉ", 255);
        optionalLength(profile.getWebsite(), "Website", 255);
        optionalLength(profile.getDescription(), "Giới thiệu doanh nghiệp", 5000);
        if (profile.getWebsite() != null && !profile.getWebsite().isBlank()
                && !(profile.getWebsite().startsWith("https://") || profile.getWebsite().startsWith("http://"))) {
            throw new BusinessException("Website phải bắt đầu bằng http:// hoặc https://.");
        }
    }

    private void requireLength(String value, String label, int min, int max) throws BusinessException {
        int length = value == null ? 0 : value.trim().length();
        if (length < min || length > max) throw new BusinessException(label + " phải có từ " + min + " đến " + max + " ký tự.");
    }

    private void optionalLength(String value, String label, int max) throws BusinessException {
        if (value != null && value.trim().length() > max) throw new BusinessException(label + " không được vượt quá " + max + " ký tự.");
    }

    private void attachHighlights(CompanyProfile company) {
        List<String> highlights = new ArrayList<>();
        if (company.isVerified()) highlights.add("Thông tin doanh nghiệp đã được xác thực");
        if (company.getOpenJobs() > 0) highlights.add(company.getOpenJobs() + " vị trí đang nhận hồ sơ");
        if (company.getIndustry() != null && !company.getIndustry().isBlank()) highlights.add("Lĩnh vực: " + company.getIndustry());
        company.setHighlights(highlights);
    }
}
