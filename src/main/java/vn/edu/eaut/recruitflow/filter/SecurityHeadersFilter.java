package vn.edu.eaut.recruitflow.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Applies browser security headers to every application response. */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'; "
                        + "img-src 'self' data:; font-src 'self'; "
                        + "style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'");
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
}
