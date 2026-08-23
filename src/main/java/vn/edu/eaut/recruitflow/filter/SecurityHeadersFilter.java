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
import java.io.IOException;

/**
 * Browser-side baseline protection. The CSP permits the current Bootstrap CDN and legacy inline
 * scripts while still blocking unknown script/style origins, frames, plugins and cross-site form
 * posts. Remove inline scripts/event handlers before tightening the two unsafe-inline directives.
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {
    private static final String CONTENT_SECURITY_POLICY = "default-src 'self'; "
            + "base-uri 'self'; object-src 'none'; frame-ancestors 'none'; form-action 'self'; "
            + "img-src 'self' data:; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
            + "font-src 'self' data: https://cdn.jsdelivr.net; "
            + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; connect-src 'self'";

    @Override
    public void init(FilterConfig filterConfig) {
        // Annotation-based configuration only.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        httpResponse.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=(), usb=()");
        httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin");

        // HSTS is valid only for an HTTPS request. Keeping it conditional preserves the local
        // http://localhost developer flow while production HTTPS receives the stronger policy.
        if (httpRequest.isSecure()) {
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if (containsSensitiveState(path)) {
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0L);
        }
        chain.doFilter(request, response);
    }

    private boolean containsSensitiveState(String path) {
        return path.startsWith("/candidate/")
                || path.startsWith("/hr/")
                || path.startsWith("/interviewer/")
                || path.startsWith("/admin/")
                || path.startsWith("/login")
                || path.startsWith("/forgot-password")
                || path.startsWith("/oauth/");
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
