package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Candidate-scoped persistence for bookmarked, currently published jobs. */
public class SavedJobDAO extends DaoSupport {
    private static final String SELECT_SAVED_JOBS = "SELECT j.id, j.job_code, j.title, j.department_id, "
            + "d.name AS department_name, j.location, j.employment_type, j.number_of_positions, "
            + "j.salary_min, j.salary_max, j.description, j.requirements, j.benefits, j.experience_required, "
            + "j.deadline, j.status, (SELECT COUNT(*) FROM applications a WHERE a.job_id=j.id) AS application_count, j.created_by, j.company_id, c.name AS company_name, c.logo_path AS company_logo_path, j.created_at, j.updated_at "
            + "FROM saved_jobs s JOIN jobs j ON j.id = s.job_id "
            + "JOIN departments d ON d.id = j.department_id JOIN companies c ON c.id = j.company_id ";

    public boolean save(int candidateId, int jobId) throws SQLException {
        String sql = "INSERT IGNORE INTO saved_jobs (candidate_id, job_id) VALUES (?, ?)";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, jobId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean remove(int candidateId, int jobId) throws SQLException {
        String sql = "DELETE FROM saved_jobs WHERE candidate_id = ? AND job_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, jobId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean exists(int candidateId, int jobId) throws SQLException {
        String sql = "SELECT 1 FROM saved_jobs WHERE candidate_id = ? AND job_id = ? LIMIT 1";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Set<Integer> findJobIds(int candidateId) throws SQLException {
        String sql = "SELECT job_id FROM saved_jobs WHERE candidate_id = ? ORDER BY saved_at DESC";
        Set<Integer> result = new LinkedHashSet<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getInt("job_id"));
                }
            }
        }
        return result;
    }

    public List<Job> findForCandidate(int candidateId, int page, int pageSize) throws SQLException {
        String sql = SELECT_SAVED_JOBS
                + "WHERE s.candidate_id = ? AND j.status = 'PUBLISHED' "
                + "ORDER BY s.saved_at DESC, j.id DESC LIMIT ? OFFSET ?";
        List<Job> result = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, pageSize(pageSize));
            statement.setInt(3, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(map(resultSet));
                }
            }
        }
        return result;
    }

    public long countForCandidate(int candidateId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM saved_jobs s JOIN jobs j ON j.id = s.job_id "
                + "WHERE s.candidate_id = ? AND j.status = 'PUBLISHED'";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private Job map(ResultSet resultSet) throws SQLException {
        Job job = new Job();
        job.setId(resultSet.getInt("id"));
        job.setJobCode(resultSet.getString("job_code"));
        job.setTitle(resultSet.getString("title"));
        job.setDepartmentId(resultSet.getInt("department_id"));
        job.setDepartmentName(resultSet.getString("department_name"));
        job.setLocation(resultSet.getString("location"));
        job.setEmploymentType(resultSet.getString("employment_type"));
        job.setNumberOfPositions(resultSet.getInt("number_of_positions"));
        job.setSalaryMin(resultSet.getBigDecimal("salary_min"));
        job.setSalaryMax(resultSet.getBigDecimal("salary_max"));
        job.setDescription(resultSet.getString("description"));
        job.setRequirements(resultSet.getString("requirements"));
        job.setBenefits(resultSet.getString("benefits"));
        job.setExperienceRequired(resultSet.getInt("experience_required"));
        job.setDeadline(resultSet.getDate("deadline"));
        job.setStatus(resultSet.getString("status"));
        job.setApplicationCount(resultSet.getInt("application_count"));
        job.setCreatedBy(resultSet.getInt("created_by"));
        job.setCompanyId(resultSet.getInt("company_id"));
        job.setCompanyName(resultSet.getString("company_name"));
        job.setCompanyLogoPath(resultSet.getString("company_logo_path"));
        job.setCreatedAt(resultSet.getTimestamp("created_at"));
        job.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return job;
    }
}
