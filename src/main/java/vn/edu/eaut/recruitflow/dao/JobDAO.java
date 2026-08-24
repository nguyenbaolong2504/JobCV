package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSkill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JobDAO extends DaoSupport {
    private static final String SELECT_JOB = "SELECT j.id, j.job_code, j.title, j.department_id, d.name AS department_name, j.location, "
            + "j.employment_type, j.number_of_positions, j.salary_min, j.salary_max, j.description, j.requirements, "
            + "j.experience_required, j.deadline, j.status, j.created_by, j.created_at, j.updated_at "
            + "FROM jobs j JOIN departments d ON d.id = j.department_id ";
    private final JobSkillDAO jobSkillDAO = new JobSkillDAO();

    public List<Job> findAll() throws SQLException {
        String sql = SELECT_JOB + "ORDER BY j.created_at DESC, j.id DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    public Job findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Job findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_JOB + "WHERE j.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Job job = map(resultSet);
                job.setSkills(jobSkillDAO.findByJobId(connection, id));
                return job;
            }
        }
    }

    public List<Job> findPublishedJobs() throws SQLException {
        return search(null, null, null, null, "PUBLISHED", "deadline_asc", 1, 100);
    }

    public List<Job> findPublishedJobs(int page, int pageSize) throws SQLException {
        return search(null, null, null, null, "PUBLISHED", "deadline_asc", page, pageSize);
    }

    public List<Job> search(String keyword, Integer departmentId, String location, String employmentType, String status,
                            String sort, int page, int pageSize) throws SQLException {
        return search(keyword, departmentId, location, employmentType, status, null, sort, page, pageSize);
    }

    public List<Job> search(String keyword, Integer departmentId, String location, String employmentType, String status,
                            Integer createdBy, String sort, int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_JOB + "WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, keyword, departmentId, location, employmentType, status, createdBy);
        sql.append(orderBy(sort)).append(" LIMIT ? OFFSET ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            int index = parameters.size() + 1;
            statement.setInt(index++, pageSize(pageSize));
            statement.setInt(index, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public long count(String keyword, Integer departmentId, String location, String employmentType, String status) throws SQLException {
        return count(keyword, departmentId, location, employmentType, status, null);
    }

    public long count(String keyword, Integer departmentId, String location, String employmentType, String status,
                      Integer createdBy) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM jobs j JOIN departments d ON d.id = j.department_id WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, keyword, departmentId, location, employmentType, status, createdBy);
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countActiveJobs() throws SQLException {
        return count(null, null, null, null, "PUBLISHED");
    }

    public long countByStatus(String status) throws SQLException {
        return count(null, null, null, null, status);
    }

    public long countAll() throws SQLException {
        return count(null, null, null, null, null);
    }

    public int insert(Job job) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, job);
        }
    }

    public int insert(Connection connection, Job job) throws SQLException {
        String sql = "INSERT INTO jobs (job_code, title, department_id, location, employment_type, number_of_positions, salary_min, "
                + "salary_max, description, requirements, experience_required, deadline, status, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindJob(statement, job, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    job.setId(keys.getInt(1));
                    return job.getId();
                }
            }
        }
        throw new SQLException("Creating job did not return a generated id.");
    }

    public boolean update(Job job) throws SQLException {
        try (Connection connection = openConnection()) {
            return update(connection, job);
        }
    }

    public boolean update(Connection connection, Job job) throws SQLException {
        String sql = "UPDATE jobs SET job_code = ?, title = ?, department_id = ?, location = ?, employment_type = ?, "
                + "number_of_positions = ?, salary_min = ?, salary_max = ?, description = ?, requirements = ?, "
                + "experience_required = ?, deadline = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindJob(statement, job, true);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(int jobId, String status) throws SQLException {
        try (Connection connection = openConnection()) {
            return updateStatus(connection, jobId, status);
        }
    }

    public boolean updateStatus(Connection connection, int jobId, String status) throws SQLException {
        String sql = "UPDATE jobs SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, jobId);
            return statement.executeUpdate() == 1;
        }
    }

    public void replaceSkills(Connection connection, int jobId, List<JobSkill> skills) throws SQLException {
        jobSkillDAO.replaceSkills(connection, jobId, skills);
    }

    public void replaceSkills(int jobId, List<JobSkill> skills) throws SQLException {
        jobSkillDAO.replaceSkills(jobId, skills);
    }

    public boolean hasApplications(int jobId) throws SQLException {
        String sql = "SELECT 1 FROM applications WHERE job_id = ? LIMIT 1";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> parameters, String keyword, Integer departmentId,
                               String location, String employmentType, String status, Integer createdBy) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(j.title) LIKE ? OR LOWER(j.job_code) LIKE ?"
                    + " OR LOWER(d.name) LIKE ? OR LOWER(d.description) LIKE ?"
                    + " OR LOWER(j.description) LIKE ? OR LOWER(j.requirements) LIKE ?"
                    + " OR EXISTS (SELECT 1 FROM job_skills keyword_skill"
                    + " WHERE keyword_skill.job_id = j.id AND LOWER(keyword_skill.skill_name) LIKE ?))");
            String value = '%' + keyword.trim().toLowerCase() + '%';
            for (int index = 0; index < 7; index++) {
                parameters.add(value);
            }
        }
        if (departmentId != null && departmentId > 0) {
            sql.append(" AND j.department_id = ?");
            parameters.add(departmentId);
        }
        if (location != null && !location.isBlank()) {
            sql.append(" AND LOWER(j.location) LIKE ?");
            parameters.add('%' + location.trim().toLowerCase() + '%');
        }
        if (employmentType != null && !employmentType.isBlank()) {
            sql.append(" AND j.employment_type = ?");
            parameters.add(employmentType.trim().toUpperCase());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND j.status = ?");
            parameters.add(status.trim().toUpperCase());
        }
        if (createdBy != null && createdBy > 0) {
            sql.append(" AND j.created_by = ?");
            parameters.add(createdBy);
        }
    }

    private String orderBy(String sort) {
        if (sort == null) {
            return " ORDER BY j.created_at DESC, j.id DESC";
        }
        return switch (sort) {
            case "deadline_asc" -> " ORDER BY j.deadline ASC, j.id DESC";
            case "salary_asc" -> " ORDER BY j.salary_min ASC, j.id DESC";
            case "salary_desc" -> " ORDER BY j.salary_max DESC, j.id DESC";
            case "title_asc" -> " ORDER BY j.title ASC, j.id DESC";
            default -> " ORDER BY j.created_at DESC, j.id DESC";
        };
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            if (value instanceof Integer integer) {
                statement.setInt(index + 1, integer);
            } else {
                statement.setString(index + 1, (String) value);
            }
        }
    }

    private void bindJob(PreparedStatement statement, Job job, boolean update) throws SQLException {
        int index = 1;
        statement.setString(index++, job.getJobCode());
        statement.setString(index++, job.getTitle());
        statement.setInt(index++, job.getDepartmentId());
        statement.setString(index++, job.getLocation());
        statement.setString(index++, job.getEmploymentType());
        statement.setInt(index++, job.getNumberOfPositions());
        statement.setBigDecimal(index++, job.getSalaryMin());
        statement.setBigDecimal(index++, job.getSalaryMax());
        statement.setString(index++, job.getDescription());
        statement.setString(index++, job.getRequirements());
        statement.setInt(index++, job.getExperienceRequired());
        statement.setDate(index++, job.getDeadline());
        if (update) {
            statement.setInt(index, job.getId());
        } else {
            statement.setString(index++, job.getStatus());
            statement.setInt(index, job.getCreatedBy());
        }
    }

    private List<Job> mapList(ResultSet resultSet) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        while (resultSet.next()) {
            jobs.add(map(resultSet));
        }
        return jobs;
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
        job.setExperienceRequired(resultSet.getInt("experience_required"));
        job.setDeadline(resultSet.getDate("deadline"));
        job.setStatus(resultSet.getString("status"));
        job.setCreatedBy(resultSet.getInt("created_by"));
        job.setCreatedAt(resultSet.getTimestamp("created_at"));
        job.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return job;
    }
}
