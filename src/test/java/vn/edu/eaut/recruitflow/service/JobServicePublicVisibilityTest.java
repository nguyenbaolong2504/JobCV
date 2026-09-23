package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobServicePublicVisibilityTest {
    @Test
    void publicDetailUsesOpenPublishedLookup() throws Exception {
        FakeJobDAO dao = new FakeJobDAO();
        Job expected = new Job();
        expected.setId(7);
        dao.detail = expected;

        JobService service = new JobService(dao, new UserDAO());

        assertSame(expected, service.getPublishedJobById(7));
        assertEquals(7, dao.requestedDetailId);
    }

    @Test
    void expiredOrClosedPublicDetailIsRejected() {
        JobService service = new JobService(new FakeJobDAO(), new UserDAO());

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.getPublishedJobById(6));

        assertTrue(error.getMessage().contains("hết hạn"));
    }

    @Test
    void publicSearchUsesOpenPublishedCountAndFilters() throws Exception {
        FakeJobDAO dao = new FakeJobDAO();
        Job job = new Job();
        job.setId(12);
        dao.searchResult = List.of(job);
        dao.total = 1;
        JobService service = new JobService(dao, new UserDAO());

        PageResult<Job> result = service.searchPublishedJobs(
                "Backend", 2, "Hà Nội", "INTERNSHIP", 1, 10, "newest");

        assertEquals(1, result.getTotalItems());
        assertEquals("Backend", dao.criteria.getKeyword());
        assertEquals(2, dao.criteria.getDepartmentId());
        assertEquals("Hà Nội", dao.criteria.getLocation());
        assertEquals("INTERNSHIP", dao.criteria.getEmploymentType());
        assertEquals("newest", dao.sort);
        assertTrue(dao.countCalled);
    }

    private static final class FakeJobDAO extends JobDAO {
        private Job detail;
        private int requestedDetailId;
        private List<Job> searchResult = List.of();
        private long total;
        private JobSearchCriteria criteria;
        private String sort;
        private boolean countCalled;

        @Override
        public Job findOpenPublishedById(int id) {
            requestedDetailId = id;
            return detail;
        }

        @Override
        public List<Job> searchPublished(JobSearchCriteria criteria, String sort, int page, int pageSize) {
            this.criteria = criteria;
            this.sort = sort;
            return searchResult;
        }

        @Override
        public long countPublished(JobSearchCriteria criteria) {
            countCalled = true;
            return total;
        }
    }
}
