package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.InterviewFeedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InterviewFeedbackDAO extends DaoSupport {
    public InterviewFeedback findById(int id) throws SQLException {
        String sql = "SELECT id, interview_id, technical_score, communication_score, experience_score, attitude_score, overall_score, comment, recommendation, submitted_at "
                + "FROM interview_feedbacks WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public InterviewFeedback findByInterviewId(int interviewId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByInterviewId(connection, interviewId);
        }
    }

    public InterviewFeedback findByInterviewId(Connection connection, int interviewId) throws SQLException {
        String sql = "SELECT id, interview_id, technical_score, communication_score, experience_score, attitude_score, overall_score, comment, recommendation, submitted_at "
                + "FROM interview_feedbacks WHERE interview_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interviewId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public int insert(InterviewFeedback feedback) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, feedback);
        }
    }

    public int insert(Connection connection, InterviewFeedback feedback) throws SQLException {
        String sql = "INSERT INTO interview_feedbacks (interview_id, technical_score, communication_score, experience_score, attitude_score, "
                + "overall_score, comment, recommendation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, feedback, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    feedback.setId(keys.getInt(1));
                    return feedback.getId();
                }
            }
        }
        throw new SQLException("Creating interview feedback did not return a generated id.");
    }

    public boolean update(InterviewFeedback feedback) throws SQLException {
        String sql = "UPDATE interview_feedbacks SET technical_score = ?, communication_score = ?, experience_score = ?, attitude_score = ?, "
                + "overall_score = ?, comment = ?, recommendation = ? WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, feedback, true);
            return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, InterviewFeedback feedback, boolean update) throws SQLException {
        int index = 1;
        if (!update) {
            statement.setInt(index++, feedback.getInterviewId());
        }
        statement.setBigDecimal(index++, feedback.getTechnicalScore());
        statement.setBigDecimal(index++, feedback.getCommunicationScore());
        statement.setBigDecimal(index++, feedback.getExperienceScore());
        statement.setBigDecimal(index++, feedback.getAttitudeScore());
        statement.setBigDecimal(index++, feedback.getOverallScore());
        statement.setString(index++, feedback.getComment());
        statement.setString(index++, feedback.getRecommendation());
        if (update) {
            statement.setInt(index, feedback.getId());
        }
    }

    private InterviewFeedback map(ResultSet resultSet) throws SQLException {
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setId(resultSet.getInt("id"));
        feedback.setInterviewId(resultSet.getInt("interview_id"));
        feedback.setTechnicalScore(resultSet.getBigDecimal("technical_score"));
        feedback.setCommunicationScore(resultSet.getBigDecimal("communication_score"));
        feedback.setExperienceScore(resultSet.getBigDecimal("experience_score"));
        feedback.setAttitudeScore(resultSet.getBigDecimal("attitude_score"));
        feedback.setOverallScore(resultSet.getBigDecimal("overall_score"));
        feedback.setComment(resultSet.getString("comment"));
        feedback.setRecommendation(resultSet.getString("recommendation"));
        feedback.setSubmittedAt(resultSet.getTimestamp("submitted_at"));
        return feedback;
    }
}
