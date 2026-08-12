package vn.edu.eaut.recruitflow.util;

import javax.servlet.http.HttpSession;

/** Helpers for one-request messages after POST-Redirect-GET. */
public final class FlashMessage {
    public static final String SUCCESS = "flashSuccess";
    public static final String ERROR = "flashError";
    public static final String WARNING = "flashWarning";

    private FlashMessage() {
    }

    public static void success(HttpSession session, String message) {
        session.setAttribute(SUCCESS, message);
    }

    public static void error(HttpSession session, String message) {
        session.setAttribute(ERROR, message);
    }

    public static void warning(HttpSession session, String message) {
        session.setAttribute(WARNING, message);
    }
}
