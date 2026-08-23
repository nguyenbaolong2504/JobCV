package vn.edu.eaut.recruitflow.service;

import java.net.URI;
import java.net.URISyntaxException;

/** Runtime-only Google OAuth client configuration; the client secret never appears in source or a JSP. */
public final class GoogleOAuthConfiguration {
    private final boolean enabled;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String disabledReason;

    private GoogleOAuthConfiguration(boolean enabled, String clientId, String clientSecret, String redirectUri,
                                     String disabledReason) {
        this.enabled = enabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.disabledReason = disabledReason;
    }

    public static GoogleOAuthConfiguration load() {
        boolean enabledRequested = booleanValue("recruitflow.googleOAuth.enabled", "RECRUITFLOW_GOOGLE_OAUTH_ENABLED", false);
        String clientId = value("recruitflow.googleOAuth.clientId", "RECRUITFLOW_GOOGLE_OAUTH_CLIENT_ID", "");
        String clientSecret = value("recruitflow.googleOAuth.clientSecret", "RECRUITFLOW_GOOGLE_OAUTH_CLIENT_SECRET", "");
        String redirectUri = value("recruitflow.googleOAuth.redirectUri", "RECRUITFLOW_GOOGLE_OAUTH_REDIRECT_URI", "");

        if (!enabledRequested) {
            return new GoogleOAuthConfiguration(false, clientId, clientSecret, redirectUri,
                    "RECRUITFLOW_GOOGLE_OAUTH_ENABLED chưa được bật.");
        }
        if (clientId.isBlank() || clientSecret.isBlank() || redirectUri.isBlank()) {
            return new GoogleOAuthConfiguration(false, clientId, clientSecret, redirectUri,
                    "Thiếu Google OAuth client ID, client secret hoặc callback URI.");
        }
        if (!isSafeRedirectUri(redirectUri)) {
            return new GoogleOAuthConfiguration(false, clientId, clientSecret, redirectUri,
                    "Google OAuth callback URI phải là HTTPS (hoặc http://localhost cho môi trường phát triển).");
        }
        return new GoogleOAuthConfiguration(true, clientId, clientSecret, redirectUri, null);
    }

    public boolean isEnabled() { return enabled; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getRedirectUri() { return redirectUri; }
    public String getDisabledReason() { return disabledReason; }

    private static boolean isSafeRedirectUri(String value) {
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getHost() == null || uri.getFragment() != null) {
                return false;
            }
            if ("https".equalsIgnoreCase(uri.getScheme())) {
                return true;
            }
            return "http".equalsIgnoreCase(uri.getScheme()) && "localhost".equalsIgnoreCase(uri.getHost());
        } catch (URISyntaxException exception) {
            return false;
        }
    }

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
}
