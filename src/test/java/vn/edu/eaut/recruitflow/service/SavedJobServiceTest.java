package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.dao.SavedJobDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedJobServiceTest {
    @Test
    void saveRequiresAValidCandidate() {
        SavedJobService service = new SavedJobService(new FakeSavedJobDAO(), new PublishedJobService());
        BusinessException exception = assertThrows(BusinessException.class, () -> service.save(0, 1));
        assertEquals("Tài khoản ứng viên không hợp lệ.", exception.getMessage());
    }

    @Test
    void saveRequiresAValidJob() {
        SavedJobService service = new SavedJobService(new FakeSavedJobDAO(), new PublishedJobService());
        BusinessException exception = assertThrows(BusinessException.class, () -> service.save(1, 0));
        assertEquals("Tin tuyển dụng không hợp lệ.", exception.getMessage());
    }

    @Test
    void saveChecksPublicationAndPersistsTheBookmark() throws BusinessException {
        FakeSavedJobDAO dao = new FakeSavedJobDAO();
        SavedJobService service = new SavedJobService(dao, new PublishedJobService());

        assertTrue(service.save(7, 12));
        assertEquals(7, dao.candidateId);
        assertEquals(12, dao.jobId);
    }

    private static final class FakeSavedJobDAO extends SavedJobDAO {
        private int candidateId;
        private int jobId;

        @Override
        public boolean save(int candidateId, int jobId) throws SQLException {
            this.candidateId = candidateId;
            this.jobId = jobId;
            return true;
        }
    }

    private static final class PublishedJobService extends JobService {
        @Override
        public Job getPublishedJobById(int id) {
            Job job = new Job();
            job.setId(id);
            job.setStatus("PUBLISHED");
            return job;
        }
    }
}
