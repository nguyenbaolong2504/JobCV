package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.dao.CompanyDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobOwnershipServiceTest {
    @Test
    void hrCannotManageAnotherRecruitersJob() {
        JobService service = serviceFor(job(7, 2), user(11, "HR"), 1);

        assertThrows(BusinessException.class, () -> service.getJobForManagement(7, 11));
    }

    @Test
    void ownerCanManageOwnJob() throws BusinessException {
        JobService service = serviceFor(job(7, 1), user(11, "HR"), 1);

        assertEquals(7, service.getJobForManagement(7, 11).getId());
    }

    @Test
    void adminCanManageAnyJob() throws BusinessException {
        JobService service = serviceFor(job(7, 2), user(1, "ADMIN"), null);

        assertEquals(7, service.getJobForManagement(7, 1).getId());
    }

    private JobService serviceFor(Job job, User actor, Integer actorCompanyId) {
        return new JobService(new StubJobDAO(job), new StubUserDAO(actor), new StubCompanyDAO(actorCompanyId));
    }

    private Job job(int id, int companyId) {
        Job job = new Job();
        job.setId(id);
        job.setCompanyId(companyId);
        return job;
    }

    private static final class StubCompanyDAO extends CompanyDAO {
        private final Integer companyId;
        private StubCompanyDAO(Integer companyId) { this.companyId = companyId; }
        @Override public vn.edu.eaut.recruitflow.model.CompanyProfile findByUserId(int userId) {
            if (companyId == null) return null;
            var company = new vn.edu.eaut.recruitflow.model.CompanyProfile(); company.setId(companyId); return company;
        }
    }

    private User user(int id, String roleName) {
        User user = new User();
        user.setId(id);
        user.setRoleName(roleName);
        return user;
    }

    private static final class StubJobDAO extends JobDAO {
        private final Job job;

        private StubJobDAO(Job job) {
            this.job = job;
        }

        @Override
        public Job findById(int id) throws SQLException {
            return job != null && job.getId() == id ? job : null;
        }
    }

    private static final class StubUserDAO extends UserDAO {
        private final User user;

        private StubUserDAO(User user) {
            this.user = user;
        }

        @Override
        public User findById(int id) throws SQLException {
            return user != null && user.getId() == id ? user : null;
        }
    }
}
