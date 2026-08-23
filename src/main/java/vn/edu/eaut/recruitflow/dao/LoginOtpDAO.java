package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.LoginOtp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/** JDBC persistence for password-login OTPs, deliberately separate from password-reset codes. */
public class LoginOtpDAO extends DaoSupport {
    public Timestamp findMostRecentCreatedAtForUpdate(Connection connection, int userId) throws SQLException {
        String sql = "SELECT created_at FROM login_verification_otps WHERE user_id = ? ORDER BY id DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getTimestamp("created_at") : null;
            }
        }
    }

    public int invalidateOutstanding(Connection connection, int userId) throws SQLException {
        String sql = "UPDATE login_verification_otps SET consumed_at = CURRENT_TIMESTAMP WHERE user_id = ? AND consumed_at IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate();
        }
    }

    public long insert(Connection connection, LoginOtp otp) throws SQLException {
        String sql = "INSERT INTO login_verification_otps (user_id, otp_hash, expires_at, attempt_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, otp.getUserId());
            statement.setString(2, otp.getOtpHash());
            statement.setTimestamp(3, otp.getExpiresAt());
            statement.setInt(4, otp.getAttemptCount());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    otp.setId(keys.getLong(1));
                    return otp.getId();
                }
            }
        }
        throw new SQLException("Creating login OTP did not return a generated id.");
    }

    public LoginOtp findLatestActiveForUpdate(Connection connection, int userId) throws SQLException {
        String sql = "SELECT id, user_id, otp_hash, expires_at, attempt_count, consumed_at, created_at "
                + "FROM login_verification_otps WHERE user_id = ? AND consumed_at IS NULL AND expires_at > CURRENT_TIMESTAMP "
                + "ORDER BY id DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean recordFailedAttempt(Connection connection, long otpId, int attemptCount) throws SQLException {
        String sql = "UPDATE login_verification_otps SET attempt_count = ? WHERE id = ? AND consumed_at IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, attemptCount);
            statement.setLong(2, otpId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean consume(Connection connection, long otpId) throws SQLException {
        String sql = "UPDATE login_verification_otps SET consumed_at = CURRENT_TIMESTAMP WHERE id = ? AND consumed_at IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, otpId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean consume(long otpId) throws SQLException {
        try (Connection connection = openConnection()) {
            return consume(connection, otpId);
        }
    }

    private LoginOtp map(ResultSet resultSet) throws SQLException {
        LoginOtp otp = new LoginOtp();
        otp.setId(resultSet.getLong("id"));
        otp.setUserId(resultSet.getInt("user_id"));
        otp.setOtpHash(resultSet.getString("otp_hash"));
        otp.setExpiresAt(resultSet.getTimestamp("expires_at"));
        otp.setAttemptCount(resultSet.getInt("attempt_count"));
        otp.setConsumedAt(resultSet.getTimestamp("consumed_at"));
        otp.setCreatedAt(resultSet.getTimestamp("created_at"));
        return otp;
    }
}
