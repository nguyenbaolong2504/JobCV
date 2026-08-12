package vn.edu.eaut.recruitflow.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

/** Reads the optional external provider configuration without ever logging secret values. */
final class AiCoachConfiguration {
    private static final String BASE_URL_PROPERTY = "recruitflow.ai.baseUrl";
    private static final String BASE_URL_ENVIRONMENT = "RECRUITFLOW_AI_BASE_URL";
    private static final String API_KEY_PROPERTY = "recruitflow.ai.apiKey";
    private static final String API_KEY_ENVIRONMENT = "RECRUITFLOW_AI_API_KEY";
    private static final String MODEL_PROPERTY = "recruitflow.ai.model";
    private static final String MODEL_ENVIRONMENT = "RECRUITFLOW_AI_MODEL";
    private static final String TIMEOUT_PROPERTY = "recruitflow.ai.timeoutSeconds";
    private static final String TIMEOUT_ENVIRONMENT = "RECRUITFLOW_AI_TIMEOUT_SECONDS";

    private final URI endpoint;
    private final String apiKey;
    private final String model;
    private final Duration timeout;
    private final String disabledNotice;

    private AiCoachConfiguration(URI endpoint, String apiKey, String model, Duration timeout, String disabledNotice) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.model = model;
        this.timeout = timeout;
        this.disabledNotice = disabledNotice;
    }

    static AiCoachConfiguration load() {
        String baseUrl = configured(BASE_URL_PROPERTY, BASE_URL_ENVIRONMENT);
        String apiKey = configured(API_KEY_PROPERTY, API_KEY_ENVIRONMENT);
        String model = configured(MODEL_PROPERTY, MODEL_ENVIRONMENT);
        Duration timeout = Duration.ofSeconds(readTimeoutSeconds());

        if (baseUrl.isBlank() && apiKey.isBlank() && model.isBlank()) {
            return new AiCoachConfiguration(null, "", "", timeout,
                    "Phân tích cục bộ đang được dùng; CV chưa được gửi đến dịch vụ AI bên ngoài.");
        }
        if (baseUrl.isBlank() || apiKey.isBlank() || model.isBlank()) {
            return new AiCoachConfiguration(null, "", "", timeout,
                    "Cấu hình AI bên ngoài chưa đầy đủ nên hệ thống dùng phân tích cục bộ an toàn.");
        }

        try {
            URI endpoint = responsesEndpoint(baseUrl);
            return new AiCoachConfiguration(endpoint, apiKey, model, timeout, "");
        } catch (IllegalArgumentException | URISyntaxException exception) {
            return new AiCoachConfiguration(null, "", "", timeout,
                    "Endpoint AI chưa hợp lệ nên hệ thống dùng phân tích cục bộ an toàn.");
        }
    }

    boolean isExternalEnabled() {
        return endpoint != null;
    }

    URI getEndpoint() {
        return endpoint;
    }

    String getApiKey() {
        return apiKey;
    }

    String getModel() {
        return model;
    }

    Duration getTimeout() {
        return timeout;
    }

    String getDisabledNotice() {
        return disabledNotice;
    }

    private static URI responsesEndpoint(String baseUrl) throws URISyntaxException {
        String normalized = baseUrl.trim().replaceAll("/+$", "");
        if (!normalized.endsWith("/responses")) {
            normalized += "/responses";
        }
        URI endpoint = new URI(normalized);
        String scheme = endpoint.getScheme();
        if ((scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http")))
                || endpoint.getHost() == null) {
            throw new IllegalArgumentException("Only HTTP(S) response endpoints are supported.");
        }
        return endpoint;
    }

    private static int readTimeoutSeconds() {
        String configured = configured(TIMEOUT_PROPERTY, TIMEOUT_ENVIRONMENT);
        if (configured.isBlank()) {
            return 25;
        }
        try {
            return Math.max(5, Math.min(60, Integer.parseInt(configured)));
        } catch (NumberFormatException ignored) {
            return 25;
        }
    }

    private static String configured(String property, String environment) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }
        return value == null ? "" : value.trim();
    }
}
