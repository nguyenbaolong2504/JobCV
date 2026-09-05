package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Interview;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAO extends DaoSupport {
    private static final String SELECT_INTERVIEW = "SELECT i.id, i.application_id, i.interviewer_id, i.interview_type, i.interview_date, i.start_time, i.end_time, "
            + "i.location, i.meeting_url, i.status, i.note, i.created_at, i.updated_at, interviewer.full_name AS interviewer_name, "
            + "candidate.full_name AS candidate_name, j.title AS job_title "
            + "FROM interviews i JOIN applications a ON a.id = i.application_id JOIN users interviewer ON interviewer.id = i.interviewer_id "
            + "JOIN users candidate ON candidate.id = a.candidate_id JOIN jobs j ON j.id = a.job_id ";

    public Interview findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Interview findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_INTERVIEW + "WHERE i.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Interview> findByApplication(int applicationId) throws SQLException {
        String sql = SELECT_INTERVIEW + "WHERE i.application_id = ? ORDER BY i.interview_date DESC, i.start_time DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Interview> findByInterviewer(int interviewerId) throws SQLException {
        String sql = SELECT_INTERVIEW + "WHERE i.interviewer_id = ? ORDER BY i.interview_date DESC, i.start_time DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interviewerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Interview> findUpcoming(int interviewerId) throws SQLException {
        String sql = SELECT_INTERVIEW + "WHERE i.interviewer_id = ? AND i.interview_date >= CURRENT_DATE "
                + "AND i.status IN ('SCHEDULED', 'RESCHEDULED') ORDER BY i.interview_date, i.start_time";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interviewerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Interview> findUpcoming() throws SQLException {
        return findUpcoming(null);
    }

    public List<Interview> findUpcoming(Integer jobOwnerId) throws SQLException {
        String sql = SELECT_INTERVIEW + "WHERE i.interview_date >= CURRENT_DATE AND i.status IN ('SCHEDULED', 'RESCHEDULED') "
                + (jobOwnerId == null ? "" : "AND j.company_id = ? ")
                + "ORDER BY i.interview_date, i.start_time";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (jobOwnerId != null) {
                statement.setInt(1, jobOwnerId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Interview> search(String keyword, Date interviewDate, String status) throws SQLException {
        return search(keyword, interviewDate, status, null);
    }

    public List<Interview> search(String keyword, Date interviewDate, String status, Integer jobOwnerId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_INTERVIEW + "WHERE 1 = 1");
        List<String> parameters = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(candidate.full_name) LIKE ? OR LOWER(j.title) LIKE ? OR LOWER(interviewer.full_name) LIKE ?)");
            String value = '%' + keyword.trim().toLowerCase() + '%';
            parameters.add(value);
            parameters.add(value);
            parameters.add(value);
        }
        if (interviewDate != null) {
            sql.append(" AND i.interview_date = ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND i.status = ?");
        }
        if (jobOwnerId != null && jobOwnerId > 0) {
            sql.append(" AND j.company_id = ?");
        }
        sql.append(" ORDER BY i.interview_date DESC, i.start_time DESC");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (String parameter : parameters) {
                statement.setString(index++, parameter);
            }
            if (interviewDate != null) {
                statement.setDate(index++, interviewDate);
            }
            if (status != null && !status.isBlank()) {
                statement.setString(index++, status.trim().toUpperCase());
            }
            if (jobOwnerId != null && jobOwnerId > 0) {
                statement.setInt(index, jobOwnerId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public boolean isAssignedToInterviewer(int interviewId, int interviewerId) throws SQLException {
        String sql = "SELECT 1 FROM interviews WHERE id = ? AND interviewer_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interviewId);
            statement.setInt(2, interviewerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean checkScheduleConflict(int interviewerId, Date interviewDate, Time startTime, Time endTime, Integer excludeInterviewId) throws SQLException {
        try (Connection connection = openConnection()) {
            return checkScheduleConflict(connection, interviewerId, interviewDate, startTime, endTime, excludeInterviewId);
        }
    }

    public boolean checkScheduleConflict(Connection connection, int interviewerId, Date interviewDate, Time startTime, Time endTime, Integer excludeInterviewId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM interviews WHERE interviewer_id = ? AND interview_date = ? "
                + "AND status IN ('SCHEDULED', 'RESCHEDULED') AND start_time < ? AND end_time > ?");
        if (excludeInterviewId != null) {
            sql.append(" AND id <> ?");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, interviewerId);
            statement.setDate(2, interviewDate);
            statement.setTime(3, endTime);
            statement.setTime(4, startTime);
            if (excludeInterviewId != null) {
                statement.setInt(5, excludeInterviewId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** Locks only the interview row before a workflow update. */
    public boolean lockById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM interviews WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean hasActiveInterviewForApplication(Connection connection, int applicationId,
                                                     Integer excludeInterviewId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM interviews WHERE application_id = ? "
                + "AND status IN ('SCHEDULED', 'RESCHEDULED')");
        if (excludeInterviewId != null) {
            sql.append(" AND id <> ?");
        }
        sql.append(" LIMIT 1");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, applicationId);
            if (excludeInterviewId != null) {
                statement.setInt(2, excludeInterviewId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int insert(Interview interview) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, interview);
        }
    }

    public int insert(Connection connection, Interview interview) throws SQLException {
        String sql = "INSERT INTO interviews (application_id, interviewer_id, interview_type, interview_date, start_time, end_time, "
                + "location, meeting_url, status, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindInterview(statement, interview, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    interview.setId(keys.getInt(1));
                    return interview.getId();
                }
            }
        }
        throw new SQLException("Creating interview did not return a generated id.");
    }

    public boolean update(Interview interview) throws SQLException {
        try (Connection connection = openConnection()) {
            return update(connection, interview);
        }
    }

    public boolean update(Connection connection, Interview interview) throws SQLException {
        String sql = "UPDATE interviews SET interviewer_id = ?, interview_type = ?, interview_date = ?, start_time = ?, end_time = ?, "
                + "location = ?, meeting_url = ?, status = ?, note = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindInterview(statement, interview, true);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateIfActive(Connection connection, Interview interview) throws SQLException {
        String sql = "UPDATE interviews SET interviewer_id = ?, interview_type = ?, interview_date = ?, start_time = ?, end_time = ?, "
                + "location = ?, meeting_url = ?, status = ?, note = ? "
                + "WHERE id = ? AND status IN ('SCHEDULED', 'RESCHEDULED')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindInterview(statement, interview, true);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(int interviewId, String status) throws SQLException {
        try (Connection connection = openConnection()) {
            return updateStatus(connection, interviewId, status);
        }
    }

    public boolean updateStatus(Connection connection, int interviewId, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE interviews SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, interviewId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatusIfCurrent(Connection connection, int interviewId, String targetStatus,
                                         String expectedStatus) throws SQLException {
        String sql = "UPDATE interviews SET status = ? WHERE id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, targetStatus);
            statement.setInt(2, interviewId);
            statement.setString(3, expectedStatus);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean cancel(Connection connection, int interviewId) throws SQLException {
        return updateStatus(connection, interviewId, "CANCELLED");
    }

    public boolean cancelIfActive(Connection connection, int interviewId) throws SQLException {
        String sql = "UPDATE interviews SET status = 'CANCELLED' WHERE id = ? "
                + "AND status IN ('SCHEDULED', 'RESCHEDULED')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interviewId);
            return statement.executeUpdate() == 1;
        }
    }

    public long countUpcoming() throws SQLException {
        return countUpcoming(null);
    }

    public long countUpcoming(Integer jobOwnerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM interviews i"
                + (jobOwnerId == null ? "" : " JOIN applications a ON a.id = i.application_id JOIN jobs j ON j.id = a.job_id")
                + " WHERE i.interview_date >= CURRENT_DATE AND i.status IN ('SCHEDULED', 'RESCHEDULED')"
                + (jobOwnerId == null ? "" : " AND j.company_id = ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (jobOwnerId != null) {
                statement.setInt(1, jobOwnerId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countUpcomingByCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM interviews i JOIN applications a ON a.id = i.application_id "
                + "WHERE a.candidate_id = ? AND i.interview_date >= CURRENT_DATE AND i.status IN ('SCHEDULED', 'RESCHEDULED')";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private void bindInterview(PreparedStatement statement, Interview interview, boolean update) throws SQLException {
        int index = 1;
        if (!update) {
            statement.setInt(index++, interview.getApplicationId());
        }
        statement.setInt(index++, interview.getInterviewerId());
        statement.setString(index++, interview.getInterviewType());
        statement.setDate(index++, interview.getInterviewDate());
        statement.setTime(index++, interview.getStartTime());
        statement.setTime(index++, interview.getEndTime());
        statement.setString(index++, interview.getLocation());
        statement.setString(index++, interview.getMeetingUrl());
        statement.setString(index++, interview.getStatus());
        statement.setString(index++, interview.getNote());
        if (update) {
            statement.setInt(index, interview.getId());
        }
    }

    private List<Interview> mapList(ResultSet resultSet) throws SQLException {
        List<Interview> interviews = new ArrayList<>();
        while (resultSet.next()) {
            interviews.add(map(resultSet));
        }
        return interviews;
    }

    private Interview map(ResultSet resultSet) throws SQLException {
        Interview interview = new Interview();
        interview.setId(resultSet.getInt("id"));
        interview.setApplicationId(resultSet.getInt("application_id"));
        interview.setInterviewerId(resultSet.getInt("interviewer_id"));
        interview.setInterviewType(resultSet.getString("interview_type"));
        interview.setInterviewDate(resultSet.getDate("interview_date"));
        interview.setStartTime(resultSet.getTime("start_time"));
        interview.setEndTime(resultSet.getTime("end_time"));
        interview.setLocation(resultSet.getString("location"));
        interview.setMeetingUrl(resultSet.getString("meeting_url"));
        interview.setStatus(resultSet.getString("status"));
        interview.setNote(resultSet.getString("note"));
        interview.setCreatedAt(resultSet.getTimestamp("created_at"));
        interview.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        interview.setInterviewerName(resultSet.getString("interviewer_name"));
        interview.setCandidateName(resultSet.getString("candidate_name"));
        interview.setJobTitle(resultSet.getString("job_title"));
        return interview;
    }
}
