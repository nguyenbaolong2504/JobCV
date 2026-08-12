package vn.edu.eaut.recruitflow.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * Protects browser form submissions with a session-bound CSRF token.
 * The token is exposed only to pages rendered by this application and is injected into POST
 * forms by the shared app.js helper.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {
    private static final String TOKEN_ATTRIBUTE = "csrfToken";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) {
        // No container configuration is required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Static resources never render forms and must not create anonymous sessions.
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        String method = httpRequest.getMethod().toUpperCase();
        HttpSession session = httpRequest.getSession(false);
        if (SAFE_METHODS.contains(method)) {
            session = session == null ? httpRequest.getSession(true) : session;
            httpRequest.setAttribute(TOKEN_ATTRIBUTE, tokenFor(session));
            chain.doFilter(request, response);
            return;
        }

        String suppliedToken = httpRequest.getParameter("_csrf");
        // JSON/fetch clients cannot naturally add a form parameter, so accept the standard header
        // as an equivalent session-bound token. Existing HTML forms continue using _csrf.
        if (suppliedToken == null || suppliedToken.isBlank()) {
            suppliedToken = httpRequest.getHeader("X-CSRF-Token");
        }
        if (session == null || !tokenMatches(tokenFor(session), suppliedToken)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }
        httpRequest.setAttribute(TOKEN_ATTRIBUTE, tokenFor(session));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }

    private String tokenFor(HttpSession session) {
        Object existing = session.getAttribute(TOKEN_ATTRIBUTE);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(TOKEN_ATTRIBUTE, token);
        return token;
    }

    private boolean tokenMatches(String expected, String supplied) {
        return supplied != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }
}
