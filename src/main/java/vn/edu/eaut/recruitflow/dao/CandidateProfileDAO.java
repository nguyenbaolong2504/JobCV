package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.CandidateProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CandidateProfileDAO extends DaoSupport {
    private static final String SELECT_PROFILE = "SELECT p.id, p.user_id, p.date_of_birth, p.gender, p.address, p.university, p.major, "
            + "p.experience_years, p.skills, p.summary, p.avatar_path, p.phone, p.target_position, p.target_location, "
            + "p.expected_salary, p.career_goal, p.certificates, p.created_at, p.updated_at, u.full_name, u.email "
            + "FROM candidate_profiles p JOIN users u ON u.id = p.user_id ";

    public CandidateProfile findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public CandidateProfile findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PROFILE + "WHERE p.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public CandidateProfile findByUserId(int userId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByUserId(connection, userId);
        }
    }

    public CandidateProfile findByUserId(Connection connection, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PROFILE + "WHERE p.user_id = ?")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public int insert(CandidateProfile profile) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, profile);
        }
    }

    public int insert(Connection connection, CandidateProfile profile) throws SQLException {
        String sql = "INSERT INTO candidate_profiles (user_id, date_of_birth, gender, address, university, major, experience_years, skills, summary, phone, target_position, target_location, expected_salary, career_goal, certificates) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, profile, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    profile.setId(keys.getInt(1));
                    return profile.getId();
                }
            }
        }
        throw new SQLException("Creating candidate profile did not return a generated id.");
    }

    public boolean update(CandidateProfile profile) throws SQLException {
        try (Connection connection = openConnection()) {
            return update(connection, profile);
        }
    }

    public boolean update(Connection connection, CandidateProfile profile) throws SQLException {
        String sql = "UPDATE candidate_profiles SET date_of_birth = ?, gender = ?, address = ?, university = ?, major = ?, "
                + "experience_years = ?, skills = ?, summary = ?, phone = ?, target_position = ?, target_location = ?, "
                + "expected_salary = ?, career_goal = ?, certificates = ? WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, profile, true);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateAvatar(int userId, String avatarPath) throws SQLException {
        String sql = "UPDATE candidate_profiles SET avatar_path = ? WHERE user_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, avatarPath);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, CandidateProfile profile, boolean update) throws SQLException {
        int index = 1;
        if (!update) {
            statement.setInt(index++, profile.getUserId());
        }
        statement.setDate(index++, profile.getDateOfBirth());
        statement.setString(index++, profile.getGender());
        statement.setString(index++, profile.getAddress());
        statement.setString(index++, profile.getUniversity());
        statement.setString(index++, profile.getMajor());
        statement.setInt(index++, profile.getExperienceYears());
        statement.setString(index++, profile.getSkills());
        statement.setString(index++, profile.getSummary());
        statement.setString(index++, profile.getPhone());
        statement.setString(index++, profile.getTargetPosition());
        statement.setString(index++, profile.getTargetLocation());
        statement.setBigDecimal(index++, profile.getExpectedSalary());
        statement.setString(index++, profile.getCareerGoal());
        statement.setString(index++, profile.getCertificates());
        if (update) {
            statement.setInt(index, profile.getUserId());
        }
    }

    private CandidateProfile map(ResultSet resultSet) throws SQLException {
        CandidateProfile profile = new CandidateProfile();
        profile.setId(resultSet.getInt("id"));
        profile.setUserId(resultSet.getInt("user_id"));
        profile.setDateOfBirth(resultSet.getDate("date_of_birth"));
        profile.setGender(resultSet.getString("gender"));
        profile.setAddress(resultSet.getString("address"));
        profile.setUniversity(resultSet.getString("university"));
        profile.setMajor(resultSet.getString("major"));
        profile.setExperienceYears(resultSet.getInt("experience_years"));
        profile.setSkills(resultSet.getString("skills"));
        profile.setSummary(resultSet.getString("summary"));
        profile.setAvatarPath(resultSet.getString("avatar_path"));
        profile.setPhone(resultSet.getString("phone"));
        profile.setTargetPosition(resultSet.getString("target_position"));
        profile.setTargetLocation(resultSet.getString("target_location"));
        profile.setExpectedSalary(resultSet.getBigDecimal("expected_salary"));
        profile.setCareerGoal(resultSet.getString("career_goal"));
        profile.setCertificates(resultSet.getString("certificates"));
        profile.setCreatedAt(resultSet.getTimestamp("created_at"));
        profile.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        profile.setFullName(resultSet.getString("full_name"));
        profile.setEmail(resultSet.getString("email"));
        return profile;
    }
}
