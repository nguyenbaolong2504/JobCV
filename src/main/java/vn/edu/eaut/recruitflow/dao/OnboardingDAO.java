package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Onboarding;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OnboardingDAO extends DaoSupport {
    private static final String SELECT_ONBOARDING = "SELECT o.id, o.application_id, o.status, o.progress, o.created_at, o.updated_at, "
            + "a.candidate_id, candidate.full_name AS candidate_name, j.title AS job_title "
            + "FROM onboardings o JOIN applications a ON a.id = o.application_id JOIN users candidate ON candidate.id = a.candidate_id "
            + "JOIN jobs j ON j.id = a.job_id ";

    public Onboarding findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Onboarding findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ONBOARDING + "WHERE o.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Onboarding findByApplicationId(int applicationId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByApplicationId(connection, applicationId);
        }
    }

    public Onboarding findByApplicationId(Connection connection, int applicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ONBOARDING + "WHERE o.application_id = ?")) {
            statement.setInt(1, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Onboarding> findByCandidateId(int candidateId) throws SQLException {
        String sql = SELECT_ONBOARDING + "WHERE a.candidate_id = ? ORDER BY o.created_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Onboarding> findByStatus(String status) throws SQLException {
        String sql = SELECT_ONBOARDING + "WHERE o.status = ? ORDER BY o.created_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Onboarding> findAll() throws SQLException {
        String sql = SELECT_ONBOARDING + "ORDER BY o.created_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    public List<Onboarding> findByJobOwner(int jobOwnerId) throws SQLException {
        String sql = SELECT_ONBOARDING + "WHERE j.created_by = ? ORDER BY o.created_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobOwnerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Onboarding> search(String keyword, String status, int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ONBOARDING + "WHERE 1 = 1");
        List<String> values = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(candidate.full_name) LIKE ? OR LOWER(j.title) LIKE ?)");
            String value = '%' + keyword.trim().toLowerCase() + '%';
            values.add(value);
            values.add(value);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ?");
            values.add(status.trim().toUpperCase());
        }
        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (String value : values) {
                statement.setString(index++, value);
            }
            statement.setInt(index++, pageSize(pageSize));
            statement.setInt(index, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public boolean isOwnedByCandidate(int onboardingId, int candidateId) throws SQLException {
        String sql = "SELECT 1 FROM onboardings o JOIN applications a ON a.id = o.application_id WHERE o.id = ? AND a.candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, onboardingId);
            statement.setInt(2, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int insert(Onboarding onboarding) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, onboarding);
        }
    }

    public int insert(Connection connection, Onboarding onboarding) throws SQLException {
        String sql = "INSERT INTO onboardings (application_id, status, progress) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, onboarding.getApplicationId());
            statement.setString(2, onboarding.getStatus());
            statement.setBigDecimal(3, onboarding.getProgress());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    onboarding.setId(keys.getInt(1));
                    return onboarding.getId();
                }
            }
        }
        throw new SQLException("Creating onboarding did not return a generated id.");
    }

    public boolean updateProgressAndStatus(Connection connection, int onboardingId, BigDecimal progress, String status) throws SQLException {
        String sql = "UPDATE onboardings SET progress = ?, status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, progress);
            statement.setString(2, status);
            statement.setInt(3, onboardingId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(Connection connection, int onboardingId, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE onboardings SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, onboardingId);
            return statement.executeUpdate() == 1;
        }
    }

    public long countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM onboardings WHERE status = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private List<Onboarding> mapList(ResultSet resultSet) throws SQLException {
        List<Onboarding> onboardings = new ArrayList<>();
        while (resultSet.next()) {
            onboardings.add(map(resultSet));
        }
        return onboardings;
    }

    private Onboarding map(ResultSet resultSet) throws SQLException {
        Onboarding onboarding = new Onboarding();
        onboarding.setId(resultSet.getInt("id"));
        onboarding.setApplicationId(resultSet.getInt("application_id"));
        onboarding.setStatus(resultSet.getString("status"));
        onboarding.setProgress(resultSet.getBigDecimal("progress"));
        onboarding.setCreatedAt(resultSet.getTimestamp("created_at"));
        onboarding.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        onboarding.setCandidateId(resultSet.getInt("candidate_id"));
        onboarding.setCandidateName(resultSet.getString("candidate_name"));
        onboarding.setJobTitle(resultSet.getString("job_title"));
        return onboarding;
    }
}
