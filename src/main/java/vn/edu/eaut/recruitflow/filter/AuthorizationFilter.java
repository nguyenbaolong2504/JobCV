package vn.edu.eaut.recruitflow.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (session != null && session.getAttribute("role") instanceof String) {
            String role = (String) session.getAttribute("role");
            boolean allowed = !path.startsWith("/admin/") && !path.startsWith("/hr/")
                    && !path.startsWith("/interviewer/") && !path.startsWith("/candidate/");

            if (path.startsWith("/admin/")) {
                allowed = "ADMIN".equals(role);
            } else if (path.startsWith("/hr/")) {
                allowed = "HR".equals(role) || "ADMIN".equals(role);
            } else if (path.startsWith("/interviewer/")) {
                allowed = "INTERVIEWER".equals(role) || "ADMIN".equals(role);
            } else if (path.startsWith("/candidate/")) {
                allowed = "CANDIDATE".equals(role);
            }

            if (!allowed) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
