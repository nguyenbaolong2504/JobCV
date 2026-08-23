package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Application;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDAO extends DaoSupport {
    private static final String SELECT_APPLICATION = "SELECT a.id, a.job_id, a.candidate_id, a.resume_id, a.status, a.match_score, a.applied_at, a.updated_at, "
            + "j.job_code, j.title AS job_title, u.full_name AS candidate_name, u.email AS candidate_email, r.file_name AS resume_file_name "
            + "FROM applications a JOIN jobs j ON j.id = a.job_id JOIN users u ON u.id = a.candidate_id "
            + "JOIN resumes r ON r.id = a.resume_id ";

    public Application findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Application findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_APPLICATION + "WHERE a.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    /**
     * Serializes workflow operations for one application without taking locks on all of the
     * joined display tables in {@link #findById(Connection, int)}.
     */
    public boolean lockById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM applications WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Application findByCandidateAndJob(int candidateId, int jobId) throws SQLException {
        String sql = SELECT_APPLICATION + "WHERE a.candidate_id = ? AND a.job_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Application> findByCandidateId(int candidateId) throws SQLException {
        String sql = SELECT_APPLICATION + "WHERE a.candidate_id = ? ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Application> findByCandidate(int candidateId, int page, int pageSize) throws SQLException {
        String sql = SELECT_APPLICATION + "WHERE a.candidate_id = ? ORDER BY a.applied_at DESC, a.id DESC LIMIT ? OFFSET ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, pageSize(pageSize));
            statement.setInt(3, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Application> findByJobId(int jobId) throws SQLException {
        String sql = SELECT_APPLICATION + "WHERE a.job_id = ? ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Application> findByStatus(String status) throws SQLException {
        return search(null, null, status, null, 1, 100);
    }

    public List<Application> search(String keyword, Integer jobId, String status, BigDecimal minMatchScore,
                                    int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_APPLICATION + "WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, keyword, jobId, status, minMatchScore, null);
        sql.append(" ORDER BY a.applied_at DESC, a.id DESC LIMIT ? OFFSET ?");
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

    public long count(String keyword, Integer jobId, String status, BigDecimal minMatchScore) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM applications a JOIN jobs j ON j.id = a.job_id JOIN users u ON u.id = a.candidate_id WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, keyword, jobId, status, minMatchScore, "a");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countByCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    /** Counts accepted hires for a job while its job row is locked by the caller. */
    public long countByJobAndStatus(Connection connection, int jobId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE job_id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobId);
            statement.setString(2, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    /** A candidate may only enter onboarding for one accepted job in this single-employer system. */
    public boolean hasHiredApplicationForCandidate(Connection connection, int candidateId, int excludeApplicationId)
            throws SQLException {
        String sql = "SELECT 1 FROM applications WHERE candidate_id = ? AND status = 'HIRED' AND id <> ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, excludeApplicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public long countAll() throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM applications");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    /** Counts applications submitted inside an inclusive date range. */
    public long countAll(LocalDate fromDate, LocalDate toDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM applications WHERE 1 = 1");
        appendAppliedDateRange(sql, fromDate, toDate);
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindAppliedDateRange(statement, fromDate, toDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public Map<String, Long> countGroupedByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS total FROM applications GROUP BY status ORDER BY status";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            Map<String, Long> result = new LinkedHashMap<>();
            while (resultSet.next()) {
                result.put(resultSet.getString("status"), resultSet.getLong("total"));
            }
            return result;
        }
    }

    /** Groups applications submitted inside an inclusive date range by their current status. */
    public Map<String, Long> countGroupedByStatus(LocalDate fromDate, LocalDate toDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT status, COUNT(*) AS total FROM applications WHERE 1 = 1");
        appendAppliedDateRange(sql, fromDate, toDate);
        sql.append(" GROUP BY status ORDER BY status");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindAppliedDateRange(statement, fromDate, toDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, Long> result = new LinkedHashMap<>();
                while (resultSet.next()) {
                    result.put(resultSet.getString("status"), resultSet.getLong("total"));
                }
                return result;
            }
        }
    }

    public Map<String, Long> countByMonth(int numberOfMonths) throws SQLException {
        int months = Math.max(1, Math.min(36, numberOfMonths));
        String sql = "SELECT DATE_FORMAT(applied_at, '%Y-%m') AS month_key, COUNT(*) AS total FROM applications "
                + "WHERE applied_at >= DATE_SUB(CURRENT_DATE, INTERVAL ? MONTH) GROUP BY DATE_FORMAT(applied_at, '%Y-%m') ORDER BY month_key";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, months - 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, Long> result = new LinkedHashMap<>();
                while (resultSet.next()) {
                    result.put(resultSet.getString("month_key"), resultSet.getLong("total"));
                }
                return result;
            }
        }
    }

    /** Returns monthly application totals for the supplied inclusive range. */
    public Map<String, Long> countByMonth(LocalDate fromDate, LocalDate toDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT DATE_FORMAT(applied_at, '%Y-%m') AS month_key, COUNT(*) AS total "
                + "FROM applications WHERE 1 = 1");
        appendAppliedDateRange(sql, fromDate, toDate);
        sql.append(" GROUP BY DATE_FORMAT(applied_at, '%Y-%m') ORDER BY month_key");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindAppliedDateRange(statement, fromDate, toDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, Long> result = new LinkedHashMap<>();
                while (resultSet.next()) {
                    result.put(resultSet.getString("month_key"), resultSet.getLong("total"));
                }
                return result;
            }
        }
    }

    public int create(Application application) throws SQLException {
        try (Connection connection = openConnection()) {
            return create(connection, application);
        }
    }

    public int create(Connection connection, Application application) throws SQLException {
        String sql = "INSERT INTO applications (job_id, candidate_id, resume_id, status, match_score) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, application.getJobId());
            statement.setInt(2, application.getCandidateId());
            statement.setInt(3, application.getResumeId());
            statement.setString(4, application.getStatus());
            statement.setBigDecimal(5, application.getMatchScore());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    application.setId(keys.getInt(1));
                    return application.getId();
                }
            }
        }
        throw new SQLException("Creating application did not return a generated id.");
    }

    public boolean existsByCandidateAndJob(int candidateId, int jobId) throws SQLException {
        try (Connection connection = openConnection()) {
            return existsByCandidateAndJob(connection, candidateId, jobId);
        }
    }

    public boolean existsByCandidateAndJob(Connection connection, int candidateId, int jobId) throws SQLException {
        String sql = "SELECT 1 FROM applications WHERE candidate_id = ? AND job_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.setInt(2, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean updateStatus(int applicationId, String status) throws SQLException {
        try (Connection connection = openConnection()) {
            return updateStatus(connection, applicationId, status);
        }
    }

    public boolean updateStatus(Connection connection, int applicationId, String status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, applicationId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Prevents a stale workflow request from overwriting a status that another transaction
     * has already advanced.
     */
    public boolean updateStatusIfCurrent(Connection connection, int applicationId, String targetStatus,
                                         String expectedStatus) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, targetStatus);
            statement.setInt(2, applicationId);
            statement.setString(3, expectedStatus);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateMatchScore(Connection connection, int applicationId, BigDecimal matchScore) throws SQLException {
        String sql = "UPDATE applications SET match_score = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, matchScore);
            statement.setInt(2, applicationId);
            return statement.executeUpdate() == 1;
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> parameters, String keyword, Integer jobId, String status,
                               BigDecimal minMatchScore, String ignored) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(j.title) LIKE ?)");
            String value = '%' + keyword.trim().toLowerCase() + '%';
            parameters.add(value);
            parameters.add(value);
            parameters.add(value);
        }
        if (jobId != null && jobId > 0) {
            sql.append(" AND a.job_id = ?");
            parameters.add(jobId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND a.status = ?");
            parameters.add(status.trim().toUpperCase());
        }
        if (minMatchScore != null) {
            sql.append(" AND a.match_score >= ?");
            parameters.add(minMatchScore);
        }
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            if (value instanceof Integer integer) {
                statement.setInt(index + 1, integer);
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index + 1, decimal);
            } else {
                statement.setString(index + 1, (String) value);
            }
        }
    }

    private void appendAppliedDateRange(StringBuilder sql, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null) {
            sql.append(" AND applied_at >= ?");
        }
        if (toDate != null) {
            sql.append(" AND applied_at < ?");
        }
    }

    private void bindAppliedDateRange(PreparedStatement statement, LocalDate fromDate, LocalDate toDate) throws SQLException {
        int index = 1;
        if (fromDate != null) {
            statement.setTimestamp(index++, Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            statement.setTimestamp(index, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }
    }

    private List<Application> mapList(ResultSet resultSet) throws SQLException {
        List<Application> applications = new ArrayList<>();
        while (resultSet.next()) {
            applications.add(map(resultSet));
        }
        return applications;
    }

    private Application map(ResultSet resultSet) throws SQLException {
        Application application = new Application();
        application.setId(resultSet.getInt("id"));
        application.setJobId(resultSet.getInt("job_id"));
        application.setCandidateId(resultSet.getInt("candidate_id"));
        application.setResumeId(resultSet.getInt("resume_id"));
        application.setStatus(resultSet.getString("status"));
        application.setMatchScore(resultSet.getBigDecimal("match_score"));
        application.setAppliedAt(resultSet.getTimestamp("applied_at"));
        application.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        application.setJobCode(resultSet.getString("job_code"));
        application.setJobTitle(resultSet.getString("job_title"));
        application.setCandidateName(resultSet.getString("candidate_name"));
        application.setCandidateEmail(resultSet.getString("candidate_email"));
        application.setResumeFileName(resultSet.getString("resume_file_name"));
        return application;
    }
}
