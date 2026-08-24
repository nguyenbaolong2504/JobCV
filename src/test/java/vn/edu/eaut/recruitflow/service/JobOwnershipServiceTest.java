package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobOwnershipServiceTest {
    @Test
    void hrCannotManageAnotherRecruitersJob() {
        JobService service = serviceFor(job(7, 22), user(11, "HR"));

        assertThrows(BusinessException.class, () -> service.getJobForManagement(7, 11));
    }

    @Test
    void ownerCanManageOwnJob() throws BusinessException {
        JobService service = serviceFor(job(7, 11), user(11, "HR"));

        assertEquals(7, service.getJobForManagement(7, 11).getId());
    }

    @Test
    void adminCanManageAnyJob() throws BusinessException {
        JobService service = serviceFor(job(7, 22), user(1, "ADMIN"));

        assertEquals(7, service.getJobForManagement(7, 1).getId());
    }

    private JobService serviceFor(Job job, User actor) {
        return new JobService(new StubJobDAO(job), new StubUserDAO(actor));
    }

    private Job job(int id, int createdBy) {
        Job job = new Job();
        job.setId(id);
        job.setCreatedBy(createdBy);
        return job;
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
