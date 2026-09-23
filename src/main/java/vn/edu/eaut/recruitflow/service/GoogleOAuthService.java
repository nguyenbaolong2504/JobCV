package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.CandidateProfileDAO;
import vn.edu.eaut.recruitflow.dao.OAuthAccountDAO;
import vn.edu.eaut.recruitflow.dao.RoleDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.UserStatus;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.OAuthAccount;
import vn.edu.eaut.recruitflow.model.Role;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.PasswordUtil;
import vn.edu.eaut.recruitflow.util.SimpleJson;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Google OpenID Connect authorization-code flow for Candidate accounts only. */
public class GoogleOAuthService {
    public static final String PROVIDER = "GOOGLE";
    private static final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String TOKEN_INFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo";

    private final GoogleOAuthConfiguration configuration;
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final CandidateProfileDAO candidateProfileDAO;
    private final OAuthAccountDAO oauthAccountDAO;
    private final HttpClient httpClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleOAuthService() {
        this(GoogleOAuthConfiguration.load(), new UserDAO(), new RoleDAO(), new CandidateProfileDAO(), new OAuthAccountDAO(),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    GoogleOAuthService(GoogleOAuthConfiguration configuration, UserDAO userDAO, RoleDAO roleDAO,
                       CandidateProfileDAO candidateProfileDAO, OAuthAccountDAO oauthAccountDAO,
                       HttpClient httpClient) {
        this.configuration = configuration;
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.candidateProfileDAO = candidateProfileDAO;
        this.oauthAccountDAO = oauthAccountDAO;
        this.httpClient = httpClient;
    }

    public boolean isEnabled() {
        return configuration.isEnabled();
    }

    public String disabledReason() {
        return configuration.getDisabledReason();
    }

    public String authorizationUrl(String state) throws BusinessException {
        requireEnabled();
        if (state == null || state.length() < 32 || state.length() > 256) {
            throw new BusinessException("Không thể khởi tạo phiên đăng nhập Google an toàn.");
        }
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("client_id", configuration.getClientId());
        parameters.put("redirect_uri", configuration.getRedirectUri());
        parameters.put("response_type", "code");
        parameters.put("scope", "openid email profile");
        parameters.put("state", state);
        parameters.put("access_type", "online");
        parameters.put("prompt", "select_account");
        return AUTHORIZATION_ENDPOINT + "?" + formEncode(parameters);
    }

    /** Exchanges, verifies, and links a Google identity. Access/refresh tokens are never persisted. */
    public User authenticate(String authorizationCode) throws BusinessException {
        requireEnabled();
        if (authorizationCode == null || authorizationCode.isBlank() || authorizationCode.length() > 4096) {
            throw new BusinessException("Google không trả về mã đăng nhập hợp lệ.");
        }
        String idToken = exchangeAuthorizationCode(authorizationCode);
        GoogleIdentity identity = verifyIdToken(idToken);
        return findOrCreateCandidate(identity);
    }

    private String exchangeAuthorizationCode(String code) throws BusinessException {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("code", code);
        parameters.put("client_id", configuration.getClientId());
        parameters.put("client_secret", configuration.getClientSecret());
        parameters.put("redirect_uri", configuration.getRedirectUri());
        parameters.put("grant_type", "authorization_code");
        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formEncode(parameters), StandardCharsets.UTF_8))
                .build();
        Map<String, Object> response = sendJson(request, "Không thể xác thực với Google lúc này.");
        String idToken = stringValue(response.get("id_token"));
        if (idToken == null || idToken.length() > 12_000) {
            throw new BusinessException("Google không trả về danh tính đăng nhập hợp lệ.");
        }
        return idToken;
    }

    /**
     * Google tokeninfo validates the signed ID token server-side. We still pin issuer, audience,
     * verified email and subject before trusting any claim.
     */
    private GoogleIdentity verifyIdToken(String idToken) throws BusinessException {
        URI uri = URI.create(TOKEN_INFO_ENDPOINT + "?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();
        Map<String, Object> claims = sendJson(request, "Không thể xác minh danh tính Google lúc này.");
        String audience = stringValue(claims.get("aud"));
        String issuer = stringValue(claims.get("iss"));
        String subject = stringValue(claims.get("sub"));
        String email = stringValue(claims.get("email"));
        if (!configuration.getClientId().equals(audience)
                || !("https://accounts.google.com".equals(issuer) || "accounts.google.com".equals(issuer))
                || subject == null || subject.isBlank() || subject.length() > 255
                || epochSeconds(claims.get("exp")) <= Instant.now().getEpochSecond()
                || !booleanValue(claims.get("email_verified"))) {
            throw new BusinessException("Không thể xác minh an toàn tài khoản Google này.");
        }
        String normalizedEmail = AuthValidation.email(email);
        return new GoogleIdentity(subject, normalizedEmail, validFullName(stringValue(claims.get("name")), normalizedEmail));
    }

    private User findOrCreateCandidate(GoogleIdentity identity) throws BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                OAuthAccount linkedAccount = oauthAccountDAO.findByProviderAndSubject(connection, PROVIDER, identity.subject());
                User user;
                if (linkedAccount != null) {
                    user = userDAO.findById(connection, linkedAccount.getUserId());
                    requireActiveCandidate(user);
                    oauthAccountDAO.touchLastLogin(connection, linkedAccount.getId());
                } else {
                    user = userDAO.findByEmail(connection, identity.email());
                    if (user != null) {
                        // A Google-verified email may be linked only to an existing Candidate account.
                        requireActiveCandidate(user);
                        ensureCandidateProfile(connection, user.getId());
                    } else {
                        Role candidateRole = roleDAO.findByName(connection, "CANDIDATE");
                        if (candidateRole == null) {
                            throw new BusinessException("Vai trò CANDIDATE chưa được cấu hình trong cơ sở dữ liệu.");
                        }
                        user = new User();
                        user.setEmail(identity.email());
                        user.setFullName(identity.fullName());
                        user.setPasswordHash(PasswordUtil.hash(randomLocalCredential()));
                        user.setRoleId(candidateRole.getId());
                        user.setStatus(UserStatus.ACTIVE.name());
                        userDAO.insert(connection, user);
                        ensureCandidateProfile(connection, user.getId());
                    }
                    OAuthAccount newAccount = new OAuthAccount();
                    newAccount.setUserId(user.getId());
                    newAccount.setProvider(PROVIDER);
                    newAccount.setProviderSubject(identity.subject());
                    oauthAccountDAO.insert(connection, newAccount);
                }
                connection.commit();
                return user;
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                if (exception instanceof SQLIntegrityConstraintViolationException) {
                    throw new BusinessException("Tài khoản Google này vừa được liên kết ở một phiên khác. Vui lòng thử đăng nhập lại.");
                }
                throw new BusinessException("Không thể hoàn tất đăng nhập Google lúc này.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể hoàn tất đăng nhập Google lúc này.", exception);
        }
    }

    private void ensureCandidateProfile(Connection connection, int userId) throws SQLException {
        if (candidateProfileDAO.findByUserId(connection, userId) == null) {
            CandidateProfile profile = new CandidateProfile();
            profile.setUserId(userId);
            profile.setExperienceYears(0);
            candidateProfileDAO.insert(connection, profile);
        }
    }

    private void requireActiveCandidate(User user) throws BusinessException {
        if (user == null || !"CANDIDATE".equals(user.getRoleName())) {
            throw new BusinessException("Đăng nhập Google chỉ hỗ trợ tài khoản Người tìm việc.");
        }
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new BusinessException("Tài khoản này hiện không hoạt động. Vui lòng liên hệ quản trị viên.");
        }
    }

    private Map<String, Object> sendJson(HttpRequest request, String userMessage) throws BusinessException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(userMessage);
            }
            Object parsed = SimpleJson.parse(response.body());
            if (!(parsed instanceof Map<?, ?> rawMap)) {
                throw new BusinessException(userMessage);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    result.put(key, entry.getValue());
                }
            }
            return result;
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(userMessage, exception);
        } catch (Exception exception) {
            throw new BusinessException(userMessage, exception);
        }
    }

    private String validFullName(String candidateName, String email) {
        try {
            return AuthValidation.fullName(candidateName);
        } catch (BusinessException ignored) {
            String localPart = email.substring(0, email.indexOf('@')).replaceAll("[._+-]+", " ").trim();
            try {
                return AuthValidation.fullName(localPart);
            } catch (BusinessException ignoredAgain) {
                return "Ứng viên JobCV";
            }
        }
    }

    private String randomLocalCredential() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void requireEnabled() throws BusinessException {
        if (!configuration.isEnabled()) {
            throw new BusinessException("Đăng nhập Google hiện chưa được cấu hình. Vui lòng dùng email và mật khẩu.");
        }
    }

    private static String formEncode(Map<String, String> parameters) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (result.length() > 0) {
                result.append('&');
            }
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    private static String stringValue(Object value) {
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    private static boolean booleanValue(Object value) {
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static long epochSeconds(Object value) {
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private record GoogleIdentity(String subject, String email, String fullName) {
    }
}
