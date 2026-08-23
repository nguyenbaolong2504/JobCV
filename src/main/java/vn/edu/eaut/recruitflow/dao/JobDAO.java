package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;
import vn.edu.eaut.recruitflow.model.JobSkill;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JobDAO extends DaoSupport {
    private static final String SELECT_JOB_LEGACY = "SELECT j.id, j.job_code, j.title, j.department_id, d.name AS department_name, "
            + "j.location, j.employment_type, j.number_of_positions, j.salary_min, j.salary_max, j.description, j.requirements, "
            + "j.experience_required, j.deadline, j.status, j.created_by, j.created_at, j.updated_at "
            + "FROM jobs j JOIN departments d ON d.id = j.department_id ";
    private static final String SELECT_JOB_WITH_CATEGORY = "SELECT j.id, j.job_code, j.title, j.department_id, d.name AS department_name, "
            + "j.category_id, c.name AS category_name, j.location, "
            + "j.employment_type, j.number_of_positions, j.salary_min, j.salary_max, j.description, j.requirements, "
            + "j.experience_required, j.deadline, j.status, j.created_by, j.created_at, j.updated_at "
            + "FROM jobs j JOIN departments d ON d.id = j.department_id "
            + "LEFT JOIN job_categories c ON c.id = j.category_id ";
    private static volatile Boolean categorySchemaAvailable;
    private final JobSkillDAO jobSkillDAO = new JobSkillDAO();

    public List<Job> findAll() throws SQLException {
        try (Connection connection = openConnection()) {
            boolean supportsCategories = supportsCategories(connection);
            String sql = selectJob(supportsCategories) + "ORDER BY j.created_at DESC, j.id DESC";
            try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet, supportsCategories);
            }
        }
    }

    public Job findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Job findById(Connection connection, int id) throws SQLException {
        boolean supportsCategories = supportsCategories(connection);
        try (PreparedStatement statement = connection.prepareStatement(selectJob(supportsCategories) + "WHERE j.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Job job = map(resultSet, supportsCategories);
                job.setSkills(jobSkillDAO.findByJobId(connection, id));
                return job;
            }
        }
    }

    /** Locks one job while a hiring-capacity decision is made in the caller's transaction. */
    public boolean lockById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM jobs WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** Returns a job only when candidates may still discover and apply to it. */
    public Job findOpenPublishedById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            boolean supportsCategories = supportsCategories(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    selectJob(supportsCategories) + "WHERE j.id = ? AND j.status = 'PUBLISHED' AND j.deadline >= CURRENT_DATE")) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    Job job = map(resultSet, supportsCategories);
                    job.setSkills(jobSkillDAO.findByJobId(connection, id));
                    return job;
                }
            }
        }
    }

    public List<Job> findPublishedJobs() throws SQLException {
        return searchPublished(new JobSearchCriteria(), "deadline_asc", 1, 100);
    }

    public List<Job> findPublishedJobs(int page, int pageSize) throws SQLException {
        return searchPublished(new JobSearchCriteria(), "deadline_asc", page, pageSize);
    }

    public List<Job> search(String keyword, Integer departmentId, String location, String employmentType, String status,
                            String sort, int page, int pageSize) throws SQLException {
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setDepartmentId(departmentId);
        criteria.setLocation(location);
        criteria.setEmploymentType(employmentType);
        return search(criteria, status, sort, page, pageSize, false);
    }

    public List<Job> searchPublished(JobSearchCriteria criteria, String sort, int page, int pageSize) throws SQLException {
        return search(criteria, "PUBLISHED", sort, page, pageSize, true);
    }

    private List<Job> search(JobSearchCriteria criteria, String status, String sort, int page, int pageSize,
                             boolean openOnly) throws SQLException {
        try (Connection connection = openConnection()) {
            boolean supportsCategories = supportsCategories(connection);
            StringBuilder sql = new StringBuilder(selectJob(supportsCategories) + "WHERE 1 = 1");
            List<Object> parameters = new ArrayList<>();
            appendFilters(sql, parameters, criteria, status, openOnly, supportsCategories);
            sql.append(orderBy(sort)).append(" LIMIT ? OFFSET ?");
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                bind(statement, parameters);
                int index = parameters.size() + 1;
                statement.setInt(index++, pageSize(pageSize));
                statement.setInt(index, offset(page, pageSize));
                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapList(resultSet, supportsCategories);
                }
            }
        }
    }

    public long count(String keyword, Integer departmentId, String location, String employmentType, String status) throws SQLException {
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setDepartmentId(departmentId);
        criteria.setLocation(location);
        criteria.setEmploymentType(employmentType);
        return count(criteria, status, false);
    }

    public long countPublished(JobSearchCriteria criteria) throws SQLException {
        return count(criteria, "PUBLISHED", true);
    }

    private long count(JobSearchCriteria criteria, String status, boolean openOnly) throws SQLException {
        try (Connection connection = openConnection()) {
            boolean supportsCategories = supportsCategories(connection);
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM jobs j WHERE 1 = 1");
            List<Object> parameters = new ArrayList<>();
            appendFilters(sql, parameters, criteria, status, openOnly, supportsCategories);
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                bind(statement, parameters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getLong(1);
                }
            }
        }
    }

    public long countActiveJobs() throws SQLException {
        return countPublished(new JobSearchCriteria());
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
        boolean supportsCategories = supportsCategories(connection);
        if (!supportsCategories && job.getCategoryId() != null) {
            throw categoryMigrationRequired();
        }
        String sql = supportsCategories
                ? "INSERT INTO jobs (job_code, title, department_id, category_id, location, employment_type, number_of_positions, salary_min, "
                + "salary_max, description, requirements, experience_required, deadline, status, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                : "INSERT INTO jobs (job_code, title, department_id, location, employment_type, number_of_positions, salary_min, "
                + "salary_max, description, requirements, experience_required, deadline, status, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindJob(statement, job, false, supportsCategories);
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
        boolean supportsCategories = supportsCategories(connection);
        if (!supportsCategories && job.getCategoryId() != null) {
            throw categoryMigrationRequired();
        }
        String sql = supportsCategories
                ? "UPDATE jobs SET job_code = ?, title = ?, department_id = ?, category_id = ?, location = ?, employment_type = ?, "
                + "number_of_positions = ?, salary_min = ?, salary_max = ?, description = ?, requirements = ?, "
                + "experience_required = ?, deadline = ? WHERE id = ?"
                : "UPDATE jobs SET job_code = ?, title = ?, department_id = ?, location = ?, employment_type = ?, "
                + "number_of_positions = ?, salary_min = ?, salary_max = ?, description = ?, requirements = ?, "
                + "experience_required = ?, deadline = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindJob(statement, job, true, supportsCategories);
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

    private void appendFilters(StringBuilder sql, List<Object> parameters, JobSearchCriteria criteria,
                               String status, boolean openOnly, boolean supportsCategories) throws SQLException {
        JobSearchCriteria safeCriteria = criteria == null ? new JobSearchCriteria() : criteria;
        String keyword = safeCriteria.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(j.title) LIKE ? OR LOWER(j.job_code) LIKE ? OR LOWER(j.description) LIKE ? "
                    + "OR LOWER(j.requirements) LIKE ? OR EXISTS (SELECT 1 FROM job_skills js "
                    + "WHERE js.job_id = j.id AND LOWER(js.skill_name) LIKE ?))");
            String value = likeValue(keyword);
            for (int index = 0; index < 5; index++) {
                parameters.add(value);
            }
        }
        if (safeCriteria.getTitle() != null && !safeCriteria.getTitle().isBlank()) {
            sql.append(" AND LOWER(j.title) LIKE ?");
            parameters.add(likeValue(safeCriteria.getTitle()));
        }
        if (safeCriteria.getDepartmentId() != null && safeCriteria.getDepartmentId() > 0) {
            sql.append(" AND j.department_id = ?");
            parameters.add(safeCriteria.getDepartmentId());
        }
        if (safeCriteria.getCategoryId() != null && safeCriteria.getCategoryId() > 0) {
            if (!supportsCategories) {
                throw categoryMigrationRequired();
            }
            // A top-level category includes jobs filed directly under it and jobs in one of its child specialisations.
            sql.append(" AND (j.category_id = ? OR j.category_id IN "
                    + "(SELECT child.id FROM job_categories child WHERE child.parent_id = ?))");
            parameters.add(safeCriteria.getCategoryId());
            parameters.add(safeCriteria.getCategoryId());
        }
        String location = safeCriteria.getLocation();
        if (location != null && !location.isBlank()) {
            sql.append(" AND LOWER(j.location) LIKE ?");
            parameters.add(likeValue(location));
        }
        String employmentType = safeCriteria.getEmploymentType();
        if (employmentType != null && !employmentType.isBlank()) {
            sql.append(" AND j.employment_type = ?");
            parameters.add(employmentType.trim().toUpperCase());
        }
        if (safeCriteria.getSalaryMin() != null) {
            sql.append(" AND j.salary_max >= ?");
            parameters.add(safeCriteria.getSalaryMin());
        }
        if (safeCriteria.getSalaryMax() != null) {
            sql.append(" AND j.salary_min <= ?");
            parameters.add(safeCriteria.getSalaryMax());
        }
        if (safeCriteria.getExperienceMin() != null) {
            sql.append(" AND j.experience_required >= ?");
            parameters.add(safeCriteria.getExperienceMin());
        }
        if (safeCriteria.getExperienceMax() != null) {
            sql.append(" AND j.experience_required <= ?");
            parameters.add(safeCriteria.getExperienceMax());
        }
        if (safeCriteria.getDeadlineFrom() != null) {
            sql.append(" AND j.deadline >= ?");
            parameters.add(Date.valueOf(safeCriteria.getDeadlineFrom()));
        }
        if (safeCriteria.getDeadlineTo() != null) {
            sql.append(" AND j.deadline <= ?");
            parameters.add(Date.valueOf(safeCriteria.getDeadlineTo()));
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND j.status = ?");
            parameters.add(status.trim().toUpperCase());
        }
        if (openOnly) {
            sql.append(" AND j.deadline >= CURRENT_DATE");
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
            case "experience_asc" -> " ORDER BY j.experience_required ASC, j.id DESC";
            case "experience_desc" -> " ORDER BY j.experience_required DESC, j.id DESC";
            case "title_asc" -> " ORDER BY j.title ASC, j.id DESC";
            default -> " ORDER BY j.created_at DESC, j.id DESC";
        };
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            if (value instanceof Integer integer) {
                statement.setInt(index + 1, integer);
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index + 1, decimal);
            } else if (value instanceof Date date) {
                statement.setDate(index + 1, date);
            } else {
                statement.setString(index + 1, (String) value);
            }
        }
    }

    private String likeValue(String value) {
        return '%' + value.trim().toLowerCase() + '%';
    }

    private void bindJob(PreparedStatement statement, Job job, boolean update, boolean supportsCategories) throws SQLException {
        int index = 1;
        statement.setString(index++, job.getJobCode());
        statement.setString(index++, job.getTitle());
        statement.setInt(index++, job.getDepartmentId());
        if (supportsCategories) {
            setNullableInt(statement, index++, job.getCategoryId());
        }
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

    private List<Job> mapList(ResultSet resultSet, boolean supportsCategories) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        while (resultSet.next()) {
            jobs.add(map(resultSet, supportsCategories));
        }
        return jobs;
    }

    private Job map(ResultSet resultSet, boolean supportsCategories) throws SQLException {
        Job job = new Job();
        job.setId(resultSet.getInt("id"));
        job.setJobCode(resultSet.getString("job_code"));
        job.setTitle(resultSet.getString("title"));
        job.setDepartmentId(resultSet.getInt("department_id"));
        job.setDepartmentName(resultSet.getString("department_name"));
        if (supportsCategories) {
            job.setCategoryId(getNullableInt(resultSet, "category_id"));
            job.setCategoryName(resultSet.getString("category_name"));
        }
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

    private String selectJob(boolean supportsCategories) {
        return supportsCategories ? SELECT_JOB_WITH_CATEGORY : SELECT_JOB_LEGACY;
    }

    private boolean supportsCategories(Connection connection) throws SQLException {
        Boolean cached = categorySchemaAvailable;
        if (Boolean.TRUE.equals(cached)) {
            return true;
        }
        boolean hasCategoryColumn = false;
        try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, null, "category_id")) {
            while (columns.next()) {
                String table = columns.getString("TABLE_NAME");
                if ("jobs".equalsIgnoreCase(table)) {
                    hasCategoryColumn = true;
                    break;
                }
            }
        }
        boolean hasCategoryTable = false;
        try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                if ("job_categories".equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                    hasCategoryTable = true;
                    break;
                }
            }
        }
        boolean available = hasCategoryColumn && hasCategoryTable;
        categorySchemaAvailable = available;
        return available;
    }

    private SQLException categoryMigrationRequired() {
        return new SQLException("Danh mục nghề nghiệp chưa được khởi tạo. Hãy chạy migration 20260823_job_categories.sql.");
    }
}
