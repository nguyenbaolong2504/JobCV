package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Offer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OfferDAO extends DaoSupport {
    private static final String SELECT_OFFER = "SELECT o.id, o.application_id, o.salary, o.start_date, o.probation_months, o.location, o.expiry_date, "
            + "o.status, o.note, o.created_at, o.updated_at, a.candidate_id, candidate.full_name AS candidate_name, j.title AS job_title, d.name AS department_name "
            + "FROM offers o JOIN applications a ON a.id = o.application_id JOIN users candidate ON candidate.id = a.candidate_id "
            + "JOIN jobs j ON j.id = a.job_id JOIN departments d ON d.id = j.department_id ";

    public Offer findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public Offer findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_OFFER + "WHERE o.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    /** Locks the offer row until the caller completes its transaction. */
    public Offer findByIdForUpdate(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_OFFER + "WHERE o.id = ? FOR UPDATE")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Offer findByApplicationId(int applicationId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByApplicationId(connection, applicationId);
        }
    }

    public Offer findByApplicationId(Connection connection, int applicationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_OFFER + "WHERE o.application_id = ?")) {
            statement.setInt(1, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Offer> findByCandidateId(int candidateId) throws SQLException {
        String sql = SELECT_OFFER + "WHERE a.candidate_id = ? ORDER BY o.created_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Offer> findByStatus(String status) throws SQLException {
        String sql = SELECT_OFFER + "WHERE o.status = ? ORDER BY o.created_at DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Offer> search(String keyword, String status, java.sql.Date expiryDate) throws SQLException {
        return search(keyword, status, expiryDate, null);
    }

    public List<Offer> search(String keyword, String status, java.sql.Date expiryDate,
                              Integer jobOwnerId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_OFFER + "WHERE 1 = 1");
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
        if (expiryDate != null) {
            sql.append(" AND o.expiry_date = ?");
        }
        if (jobOwnerId != null && jobOwnerId > 0) {
            sql.append(" AND j.created_by = ?");
        }
        sql.append(" ORDER BY o.expiry_date ASC, o.created_at DESC");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (String value : values) {
                statement.setString(index++, value);
            }
            if (expiryDate != null) {
                statement.setDate(index++, expiryDate);
            }
            if (jobOwnerId != null && jobOwnerId > 0) {
                statement.setInt(index, jobOwnerId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public boolean isOwnedByCandidate(int offerId, int candidateId) throws SQLException {
        String sql = "SELECT 1 FROM offers o JOIN applications a ON a.id = o.application_id WHERE o.id = ? AND a.candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, offerId);
            statement.setInt(2, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int insert(Offer offer) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, offer);
        }
    }

    public int insert(Connection connection, Offer offer) throws SQLException {
        String sql = "INSERT INTO offers (application_id, salary, start_date, probation_months, location, expiry_date, status, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, offer, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    offer.setId(keys.getInt(1));
                    return offer.getId();
                }
            }
        }
        throw new SQLException("Creating offer did not return a generated id.");
    }

    public boolean update(Offer offer) throws SQLException {
        try (Connection connection = openConnection()) {
            return update(connection, offer);
        }
    }

    public boolean update(Connection connection, Offer offer) throws SQLException {
        String sql = "UPDATE offers SET salary = ?, start_date = ?, probation_months = ?, location = ?, expiry_date = ?, note = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, offer.getSalary());
            statement.setDate(2, offer.getStartDate());
            statement.setInt(3, offer.getProbationMonths());
            statement.setString(4, offer.getLocation());
            statement.setDate(5, offer.getExpiryDate());
            statement.setString(6, offer.getNote());
            statement.setInt(7, offer.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateStatus(int offerId, String status) throws SQLException {
        try (Connection connection = openConnection()) {
            return updateStatus(connection, offerId, status);
        }
    }

    public boolean updateStatus(Connection connection, int offerId, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE offers SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, offerId);
            return statement.executeUpdate() == 1;
        }
    }

    public long countByStatus(String status) throws SQLException {
        return countByStatus(status, null);
    }

    public long countByStatus(String status, Integer jobOwnerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM offers o"
                + (jobOwnerId == null ? "" : " JOIN applications a ON a.id = o.application_id JOIN jobs j ON j.id = a.job_id")
                + " WHERE o.status = ?" + (jobOwnerId == null ? "" : " AND j.created_by = ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            if (jobOwnerId != null) {
                statement.setInt(2, jobOwnerId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countByCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM offers o JOIN applications a ON a.id = o.application_id WHERE a.candidate_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, candidateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countAll() throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM offers");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private void bind(PreparedStatement statement, Offer offer, boolean update) throws SQLException {
        int index = 1;
        if (!update) {
            statement.setInt(index++, offer.getApplicationId());
        }
        statement.setBigDecimal(index++, offer.getSalary());
        statement.setDate(index++, offer.getStartDate());
        statement.setInt(index++, offer.getProbationMonths());
        statement.setString(index++, offer.getLocation());
        statement.setDate(index++, offer.getExpiryDate());
        statement.setString(index++, offer.getStatus());
        statement.setString(index, offer.getNote());
    }

    private List<Offer> mapList(ResultSet resultSet) throws SQLException {
        List<Offer> offers = new ArrayList<>();
        while (resultSet.next()) {
            offers.add(map(resultSet));
        }
        return offers;
    }

    private Offer map(ResultSet resultSet) throws SQLException {
        Offer offer = new Offer();
        offer.setId(resultSet.getInt("id"));
        offer.setApplicationId(resultSet.getInt("application_id"));
        offer.setSalary(resultSet.getBigDecimal("salary"));
        offer.setStartDate(resultSet.getDate("start_date"));
        offer.setProbationMonths(resultSet.getInt("probation_months"));
        offer.setLocation(resultSet.getString("location"));
        offer.setExpiryDate(resultSet.getDate("expiry_date"));
        offer.setStatus(resultSet.getString("status"));
        offer.setNote(resultSet.getString("note"));
        offer.setCreatedAt(resultSet.getTimestamp("created_at"));
        offer.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        offer.setCandidateId(resultSet.getInt("candidate_id"));
        offer.setCandidateName(resultSet.getString("candidate_name"));
        offer.setJobTitle(resultSet.getString("job_title"));
        offer.setDepartmentName(resultSet.getString("department_name"));
        return offer;
    }
}
