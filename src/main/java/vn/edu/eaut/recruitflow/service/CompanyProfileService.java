package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.dao.CompanyDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.List;

/** Public company profiles backed by independent company tenant records. */
public class CompanyProfileService {
    private static final int COMPANY_JOB_LIMIT = 100;
    private final CompanyDAO companyDAO;
    private final JobService jobService;

    public CompanyProfileService() {
        this(new CompanyDAO(), new JobService());
    }

    CompanyProfileService(CompanyDAO companyDAO, JobService jobService) {
        this.companyDAO = companyDAO;
        this.jobService = jobService;
    }

    public List<CompanyProfile> getCompanies(String keyword) throws BusinessException {
        try {
            return companyDAO.findActive(keyword);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách nhà tuyển dụng.", exception);
        }
    }

    public CompanyProfile getCompany(int id) throws BusinessException {
        try {
            CompanyProfile company = companyDAO.findActiveById(id);
            if (company == null) {
                throw new BusinessException("Không tìm thấy công ty đang hoạt động.");
            }
            return company;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông tin nhà tuyển dụng.", exception);
        }
    }

    public List<Job> getOpenJobs(int id) throws BusinessException {
        // Validate that the requested tenant is public before exposing its jobs.
        getCompany(id);
        return jobsFor(id).getItems();
    }

    public CompanyProfile getCompanyForMember(int userId) throws BusinessException {
        try {
            CompanyProfile company = companyDAO.findByUserId(userId);
            if (company == null) throw new BusinessException("Tài khoản chưa được liên kết với công ty.");
            return company;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải công ty của tài khoản.", exception);
        }
    }

    public void updateCompany(CompanyProfile submitted, int hrUserId) throws BusinessException {
        CompanyProfile company = getCompanyForMember(hrUserId);
        submitted.setId(company.getId());
        if (submitted.getName() == null || submitted.getName().isBlank()) throw new BusinessException("Tên công ty là bắt buộc.");
        try {
            if (!companyDAO.update(submitted)) throw new BusinessException("Không thể cập nhật thông tin công ty.");
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật thông tin công ty.", exception);
        }
    }

    private PageResult<Job> jobsFor(int companyId) throws BusinessException {
        return jobService.searchPublishedJobsByCompany(companyId, 1, COMPANY_JOB_LIMIT, "newest");
    }
}
