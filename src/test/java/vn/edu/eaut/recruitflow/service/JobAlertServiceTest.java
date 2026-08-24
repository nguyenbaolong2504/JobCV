package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.dao.JobAlertDAO;
import vn.edu.eaut.recruitflow.model.JobAlert;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobAlertServiceTest {
    @Test
    void createRequiresAtLeastOneSearchCriterion() {
        JobAlertService service = service(new FakeJobAlertDAO());
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(2, "Việc mới", "", null, "", "", "DAILY"));
        assertEquals("Hãy chọn ít nhất một tiêu chí tìm việc.", exception.getMessage());
    }

    @Test
    void createRejectsUnknownFrequency() {
        JobAlertService service = service(new FakeJobAlertDAO());
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(2, "Java", "Java", null, "", "", "MONTHLY"));
        assertEquals("Tần suất thông báo không hợp lệ.", exception.getMessage());
    }

    @Test
    void createNormalizesAndPersistsAnAlert() throws BusinessException {
        FakeJobAlertDAO dao = new FakeJobAlertDAO();
        JobAlertService service = service(dao);
        int id = service.create(9, "  Java Hà Nội  ", "  Java  ", null, " Hà Nội ", "FULL_TIME", "weekly");
        assertEquals(41, id);
        assertEquals(9, dao.inserted.getCandidateId());
        assertEquals("Java Hà Nội", dao.inserted.getName());
        assertEquals("Java", dao.inserted.getKeyword());
        assertEquals("Hà Nội", dao.inserted.getLocation());
        assertEquals("FULL_TIME", dao.inserted.getEmploymentType());
        assertEquals("WEEKLY", dao.inserted.getFrequency());
    }

    private JobAlertService service(FakeJobAlertDAO dao) {
        return new JobAlertService(dao, new JobService(), new DepartmentService());
    }

    private static final class FakeJobAlertDAO extends JobAlertDAO {
        private JobAlert inserted;

        @Override
        public long countByCandidateId(int candidateId, boolean activeOnly) {
            return 0;
        }

        @Override
        public int insert(JobAlert alert) throws SQLException {
            inserted = alert;
            alert.setId(41);
            return 41;
        }
    }
}
