package vn.edu.eaut.recruitflow.service;

/**
 * A provider-side failure. Its message is deliberately safe to show only as a generic fallback
 * notice; raw HTTP bodies, endpoint credentials, and keys are never included here.
 */
public class AiProviderException extends Exception {
    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
