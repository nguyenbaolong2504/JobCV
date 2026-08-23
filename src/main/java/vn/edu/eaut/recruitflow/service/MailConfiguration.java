package vn.edu.eaut.recruitflow.service;

/**
 * Runtime-only SMTP configuration. Secrets are read from JVM properties or environment variables
 * and are never persisted, logged, or shipped in source control.
 */
public final class MailConfiguration {
    private final boolean enabled;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromAddress;
    private final String disabledReason;

    private MailConfiguration(boolean enabled, String host, int port, String username, String password,
                              String fromAddress, String disabledReason) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
        this.disabledReason = disabledReason;
    }

    public static MailConfiguration load() {
        boolean enabledRequested = booleanValue("recruitflow.mail.enabled", "RECRUITFLOW_MAIL_ENABLED", false);
        String host = value("recruitflow.mail.host", "RECRUITFLOW_MAIL_HOST", "smtp.gmail.com");
        int port = intValue("recruitflow.mail.port", "RECRUITFLOW_MAIL_PORT", 587);
        String username = value("recruitflow.mail.username", "RECRUITFLOW_MAIL_USERNAME", "");
        String password = value("recruitflow.mail.password", "RECRUITFLOW_MAIL_PASSWORD", "");
        String fromAddress = value("recruitflow.mail.from", "RECRUITFLOW_MAIL_FROM", username);

        if (!enabledRequested) {
            return new MailConfiguration(false, host, port, username, password, fromAddress,
                    "RECRUITFLOW_MAIL_ENABLED chưa được bật.");
        }
        if (host.isBlank() || port < 1 || port > 65535 || username.isBlank() || password.isBlank() || fromAddress.isBlank()) {
            return new MailConfiguration(false, host, port, username, password, fromAddress,
                    "Thiếu cấu hình Gmail SMTP bắt buộc.");
        }
        return new MailConfiguration(true, host, port, username, password, fromAddress, null);
    }

    public boolean isEnabled() { return enabled; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFromAddress() { return fromAddress; }
    public String getDisabledReason() { return disabledReason; }

    private static String value(String property, String environment, String fallback) {
        String configured = System.getProperty(property);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(environment);
        }
        return configured == null || configured.isBlank() ? fallback : configured.trim();
    }

    private static boolean booleanValue(String property, String environment, boolean fallback) {
        String configured = value(property, environment, "");
        return configured.isBlank() ? fallback : Boolean.parseBoolean(configured);
    }

    private static int intValue(String property, String environment, int fallback) {
        try {
            int configured = Integer.parseInt(value(property, environment, String.valueOf(fallback)));
            return configured >= 1 && configured <= 65535 ? configured : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
