package vn.edu.eaut.recruitflow.util;

/** A known CV Coach API failure that can be returned to the candidate safely. */
public class AiReviewException extends BusinessException {
    private final int httpStatus;

    public AiReviewException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AiReviewException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
