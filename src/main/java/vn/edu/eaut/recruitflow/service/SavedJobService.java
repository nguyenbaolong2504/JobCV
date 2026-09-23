package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.SavedJobDAO;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.Set;

/** Candidate bookmark workflow with published-job and ownership boundaries. */
public class SavedJobService {
    private final SavedJobDAO savedJobDAO;
    private final JobService jobService;

    public SavedJobService() {
        this(new SavedJobDAO(), new JobService());
    }

    SavedJobService(SavedJobDAO savedJobDAO, JobService jobService) {
        this.savedJobDAO = savedJobDAO;
        this.jobService = jobService;
    }

    public boolean save(int candidateId, int jobId) throws BusinessException {
        requireIds(candidateId, jobId);
        jobService.getPublishedJobById(jobId);
        try {
            return savedJobDAO.save(candidateId, jobId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể lưu việc làm lúc này.", exception);
        }
    }

    public boolean remove(int candidateId, int jobId) throws BusinessException {
        requireIds(candidateId, jobId);
        try {
            return savedJobDAO.remove(candidateId, jobId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể bỏ lưu việc làm lúc này.", exception);
        }
    }

    public boolean isSaved(int candidateId, int jobId) throws BusinessException {
        requireIds(candidateId, jobId);
        try {
            return savedJobDAO.exists(candidateId, jobId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kiểm tra trạng thái việc làm đã lưu.", exception);
        }
    }

    public Set<Integer> getSavedJobIds(int candidateId) throws BusinessException {
        requireCandidate(candidateId);
        try {
            return savedJobDAO.findJobIds(candidateId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải việc làm đã lưu.", exception);
        }
    }

    public PageResult<Job> getSavedJobs(int candidateId, int page, int pageSize) throws BusinessException {
        requireCandidate(candidateId);
        try {
            return new PageResult<>(savedJobDAO.findForCandidate(candidateId, page, pageSize), page, pageSize,
                    savedJobDAO.countForCandidate(candidateId));
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách việc làm đã lưu.", exception);
        }
    }

    public long count(int candidateId) throws BusinessException {
        requireCandidate(candidateId);
        try {
            return savedJobDAO.countForCandidate(candidateId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đếm việc làm đã lưu.", exception);
        }
    }

    private void requireIds(int candidateId, int jobId) throws BusinessException {
        requireCandidate(candidateId);
        if (jobId <= 0) {
            throw new BusinessException("Tin tuyển dụng không hợp lệ.");
        }
    }

    private void requireCandidate(int candidateId) throws BusinessException {
        if (candidateId <= 0) {
            throw new BusinessException("Tài khoản ứng viên không hợp lệ.");
        }
    }
}
