package vn.edu.eaut.recruitflow.service;

/** Controls whether password login is required to complete the optional email OTP second step. */
public final class LoginOtpConfiguration {
    private final boolean requested;
    private final boolean mailAvailable;

    private LoginOtpConfiguration(boolean requested, boolean mailAvailable) {
        this.requested = requested;
        this.mailAvailable = mailAvailable;
    }

    public static LoginOtpConfiguration load(MailConfiguration mailConfiguration) {
        String configured = System.getProperty("recruitflow.authOtp.required");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("RECRUITFLOW_AUTH_OTP_REQUIRED");
        }
        boolean requested = configured != null && Boolean.parseBoolean(configured.trim());
        return new LoginOtpConfiguration(requested, mailConfiguration != null && mailConfiguration.isEnabled());
    }

    public boolean isRequired() { return requested && mailAvailable; }
    public boolean isMisconfigured() { return requested && !mailAvailable; }
    public boolean isRequested() { return requested; }
}
