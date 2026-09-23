package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.dao.CompanyDAO;
import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyProfileServiceTest {
    @Test
    void publicCompanyUsesIndependentCompanyIdentityAndOpenJobs() throws Exception {
        CompanyProfile source = new CompanyProfile();
        source.setId(22); source.setName("Công ty Sao Việt"); source.setIndustry("Công nghệ thông tin");
        source.setLocation("Hà Nội"); source.setOpenJobs(1); source.setVerified(true);
        Job job = new Job();
        job.setDepartmentName("Công nghệ thông tin");
        job.setLocation("Hà Nội");

        CompanyProfileService service = new CompanyProfileService(
                new FakeCompanyDAO(source), new FakeJobService(job));

        CompanyProfile company = service.getCompanies("").get(0);
        assertEquals(22, company.getId());
        assertEquals("Công ty Sao Việt", company.getName());
        assertEquals("Công nghệ thông tin", company.getIndustry());
        assertEquals("Hà Nội", company.getLocation());
        assertEquals(1, company.getOpenJobs());
        assertTrue(company.isVerified());
    }

    private static final class FakeCompanyDAO extends CompanyDAO {
        private final CompanyProfile company;
        private FakeCompanyDAO(CompanyProfile company) {
            this.company = company;
        }
        @Override public List<CompanyProfile> findActive(String keyword) { return List.of(company); }
        @Override public CompanyProfile findActiveById(int id) { return company.getId() == id ? company : null; }
    }

    private static final class FakeJobService extends JobService {
        private final Job job;

        private FakeJobService(Job job) {
            this.job = job;
        }

        @Override
        public PageResult<Job> searchPublishedJobsByCompany(int companyId, int page, int pageSize, String sort) {
            return new PageResult<>(List.of(job), page, pageSize, 1);
        }
    }
}
