package vn.edu.eaut.recruitflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection factory. Values can be supplied through JVM system
 * properties (recruitflow.db.*) or environment variables (RECRUITFLOW_DB_*).
 */
public final class DBUtil {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/JobCVDB?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String URL = configuration("recruitflow.db.url", "RECRUITFLOW_DB_URL", DEFAULT_URL);
    private static final String USER = configuration("recruitflow.db.user", "RECRUITFLOW_DB_USER", "root");
    private static final String PASSWORD = configuration("recruitflow.db.password", "RECRUITFLOW_DB_PASSWORD",
            "123456aB@");

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
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
