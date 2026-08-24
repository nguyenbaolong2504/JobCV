package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.OAuthAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Stores a provider's stable subject identifier, never an OAuth access token. */
public class OAuthAccountDAO extends DaoSupport {
    public OAuthAccount findByProviderAndSubject(Connection connection, String provider, String providerSubject)
            throws SQLException {
        String sql = "SELECT id, user_id, provider, provider_subject, created_at, last_login_at "
                + "FROM oauth_accounts WHERE provider = ? AND provider_subject = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, provider);
            statement.setString(2, providerSubject);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public long insert(Connection connection, OAuthAccount account) throws SQLException {
        String sql = "INSERT INTO oauth_accounts (user_id, provider, provider_subject, last_login_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, account.getUserId());
            statement.setString(2, account.getProvider());
            statement.setString(3, account.getProviderSubject());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                    return account.getId();
                }
            }
        }
        throw new SQLException("Creating OAuth account did not return a generated id.");
    }

    public boolean touchLastLogin(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE oauth_accounts SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private OAuthAccount map(ResultSet resultSet) throws SQLException {
        OAuthAccount account = new OAuthAccount();
        account.setId(resultSet.getLong("id"));
        account.setUserId(resultSet.getInt("user_id"));
        account.setProvider(resultSet.getString("provider"));
        account.setProviderSubject(resultSet.getString("provider_subject"));
        account.setCreatedAt(resultSet.getTimestamp("created_at"));
        account.setLastLoginAt(resultSet.getTimestamp("last_login_at"));
        return account;
    }
}
