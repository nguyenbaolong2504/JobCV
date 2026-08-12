package vn.edu.eaut.recruitflow.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI().substring(req.getContextPath().length());
        boolean loggedIn = session != null && session.getAttribute("userId") instanceof Integer
                && session.getAttribute("role") instanceof String;

        if (loggedIn || !requiresAuthentication(path)) {
            chain.doFilter(request, response);
        } else {
            session = req.getSession(true);
            session.setAttribute("flashWarning", "Vui lòng đăng nhập để tiếp tục.");
            res.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private boolean requiresAuthentication(String path) {
        return path.startsWith("/candidate/")
                || path.startsWith("/hr/")
                || path.startsWith("/interviewer/")
                || path.startsWith("/admin/");
    }

    @Override
    public void destroy() {}
}
