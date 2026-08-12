package vn.edu.eaut.recruitflow.util;

/** A user-facing rule or validation failure raised by the service layer. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
