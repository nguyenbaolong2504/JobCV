package vn.edu.eaut.recruitflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central JDBC connection factory. Values can be supplied through JVM system
 * properties (recruitflow.db.*) or environment variables (RECRUITFLOW_DB_*).
 */
public final class DBUtil {
    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    /** Matches the database created by schema.sql. Credentials must never be committed here. */
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/recruitflow?useSSL=false&serverTimezone=Asia/Bangkok&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String URL = configuration("recruitflow.db.url", "RECRUITFLOW_DB_URL", DEFAULT_URL);
    private static final String USER = configuration("recruitflow.db.user", "RECRUITFLOW_DB_USER", "root");
    // An empty fallback supports a deliberately passwordless local MySQL account only. Production
    // deployments must supply the password through a JVM property or environment variable.
    private static final String PASSWORD = configuration("recruitflow.db.password", "RECRUITFLOW_DB_PASSWORD", "");

    static {    
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Cannot load MySQL JDBC driver: " + e.getMessage());
        }
    }

    private DBUtil() {
    }

    private static String configuration(String property, String environment, String fallback) {
        String configured = System.getProperty(property);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(environment);
        }
        return configured == null || configured.isBlank() ? fallback : configured.trim();
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException exception) {
            // The URL and user are configuration metadata, not credentials. The password is never logged.
            LOGGER.log(Level.SEVERE,
                    "Cannot connect to configured RecruitFlow database (url={0}, user={1}, sqlState={2}, errorCode={3}).",
                    new Object[]{URL, USER, exception.getSQLState(), exception.getErrorCode()});
            throw exception;
        }
    }
}
