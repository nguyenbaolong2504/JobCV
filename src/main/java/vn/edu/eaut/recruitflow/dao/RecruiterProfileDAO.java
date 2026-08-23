package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.RecruiterProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persists organization data used to review self-registered recruiter accounts. */
public class RecruiterProfileDAO extends DaoSupport {
    public RecruiterProfile findByUserId(Connection connection, int userId) throws SQLException {
        String sql = "SELECT id, user_id, organization_name, job_title, work_phone, created_at, updated_at "
                + "FROM recruiter_profiles WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Map<Integer, RecruiterProfile> findByUserIds(List<Integer> userIds) throws SQLException {
        Map<Integer, RecruiterProfile> profiles = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return profiles;
        }
        String placeholders = String.join(", ", java.util.Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT id, user_id, organization_name, job_title, work_phone, created_at, updated_at "
                + "FROM recruiter_profiles WHERE user_id IN (" + placeholders + ") ORDER BY id";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < userIds.size(); index++) {
                statement.setInt(index + 1, userIds.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    RecruiterProfile profile = map(resultSet);
                    profiles.put(profile.getUserId(), profile);
                }
            }
        }
        return profiles;
    }

    public int insert(Connection connection, RecruiterProfile profile) throws SQLException {
        String sql = "INSERT INTO recruiter_profiles (user_id, organization_name, job_title, work_phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, profile.getUserId());
            statement.setString(2, profile.getOrganizationName());
            statement.setString(3, profile.getJobTitle());
            statement.setString(4, profile.getWorkPhone());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    profile.setId(keys.getInt(1));
                    return profile.getId();
                }
            }
        }
        throw new SQLException("Creating recruiter profile did not return a generated id.");
    }

    private RecruiterProfile map(ResultSet resultSet) throws SQLException {
        RecruiterProfile profile = new RecruiterProfile();
        profile.setId(resultSet.getInt("id"));
        profile.setUserId(resultSet.getInt("user_id"));
        profile.setOrganizationName(resultSet.getString("organization_name"));
        profile.setJobTitle(resultSet.getString("job_title"));
        profile.setWorkPhone(resultSet.getString("work_phone"));
        profile.setCreatedAt(resultSet.getTimestamp("created_at"));
        profile.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return profile;
    }
}
