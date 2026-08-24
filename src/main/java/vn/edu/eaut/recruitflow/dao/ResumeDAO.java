package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Resume;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResumeDAO extends DaoSupport {
    private static final String SELECT_RESUME = "SELECT id, candidate_id, file_name, file_path, file_type, file_size, extracted_text, is_default, uploaded_at FROM resumes ";

    public Resume findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Resume findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_RESUME + "WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Resume> findByCandidateId(int candidateId) throws SQLException {
        String sql = SELECT_RESUME + "WHERE candidate_id = ? ORDER BY is_default DESC, uploaded_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Resume> resumes = new ArrayList<>();
                while (resultSet.next()) {
                    resumes.add(map(resultSet));
                }
                return resumes;
            }
        }
    }

    public Resume findDefaultByCandidateId(int candidateId) throws SQLException {
        String sql = SELECT_RESUME + "WHERE candidate_id = ? AND is_default = TRUE";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean belongsToCandidate(int resumeId, int candidateId) throws SQLException {
        String sql = "SELECT 1 FROM resumes WHERE id = ? AND candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resumeId);
            statement.setInt(2, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int insert(Resume resume) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, resume);
        }
    }

    public int insert(Connection connection, Resume resume) throws SQLException {
        String sql = "INSERT INTO resumes (candidate_id, file_name, file_path, file_type, file_size, extracted_text, is_default) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, resume.getCandidateId());
            statement.setString(2, resume.getFileName());
            statement.setString(3, resume.getFilePath());
            statement.setString(4, resume.getFileType());
            statement.setLong(5, resume.getFileSize());
            statement.setString(6, resume.getExtractedText());
            statement.setBoolean(7, resume.isDefaultResume());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    resume.setId(keys.getInt(1));
                    return resume.getId();
                }
            }
        }
        throw new SQLException("Creating resume did not return a generated id.");
    }

    public boolean updateExtractedText(int resumeId, String extractedText) throws SQLException {
        String sql = "UPDATE resumes SET extracted_text = ? WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, extractedText);
            statement.setInt(2, resumeId);
            return statement.executeUpdate() == 1;
        }
    }

    /** The caller owns the supplied connection/transaction. */
    public boolean setDefault(Connection connection, int candidateId, int resumeId) throws SQLException {
        clearDefault(connection, candidateId);
        String sql = "UPDATE resumes SET is_default = TRUE WHERE id = ? AND candidate_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resumeId);
            statement.setInt(2, candidateId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean setDefault(int candidateId, int resumeId) throws SQLException {
        try (Connection connection = openConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                boolean changed = setDefault(connection, candidateId, resumeId);
                if (changed) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
                return changed;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public boolean clearDefault(Connection connection, int candidateId) throws SQLException {
        String sql = "UPDATE resumes SET is_default = FALSE WHERE candidate_id = ? AND is_default = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            statement.executeUpdate();
            return true;
        }
    }

    public boolean delete(int resumeId, int candidateId) throws SQLException {
        try (Connection connection = openConnection()) {
            return delete(connection, resumeId, candidateId);
        }
    }

    public boolean delete(Connection connection, int resumeId, int candidateId) throws SQLException {
        String sql = "DELETE FROM resumes WHERE id = ? AND candidate_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resumeId);
            statement.setInt(2, candidateId);
            return statement.executeUpdate() == 1;
        }
    }

    public Resume findNewestByCandidateId(Connection connection, int candidateId) throws SQLException {
        String sql = SELECT_RESUME + "WHERE candidate_id = ? ORDER BY uploaded_at DESC, id DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public long countByCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM resumes WHERE candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private Resume map(ResultSet resultSet) throws SQLException {
        Resume resume = new Resume();
        resume.setId(resultSet.getInt("id"));
        resume.setCandidateId(resultSet.getInt("candidate_id"));
        resume.setFileName(resultSet.getString("file_name"));
        resume.setFilePath(resultSet.getString("file_path"));
        resume.setFileType(resultSet.getString("file_type"));
        resume.setFileSize(resultSet.getLong("file_size"));
        resume.setExtractedText(resultSet.getString("extracted_text"));
        resume.setDefaultResume(resultSet.getBoolean("is_default"));
        resume.setUploadedAt(resultSet.getTimestamp("uploaded_at"));
        return resume;
    }
}
