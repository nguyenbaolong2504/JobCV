package vn.edu.eaut.recruitflow.filter;

import vn.edu.eaut.recruitflow.enums.PermissionCode;
import vn.edu.eaut.recruitflow.service.PermissionService;
import vn.edu.eaut.recruitflow.util.PermissionPolicy;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {
    private PermissionService permissionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        permissionService = new PermissionService();
    }

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
                // Interviewer pages enforce assignment ownership. Admin has no interviewer
                // assignment context, so access belongs in the HR/Admin workspaces instead.
                allowed = "INTERVIEWER".equals(role);
            } else if (path.startsWith("/candidate/")) {
                allowed = "CANDIDATE".equals(role);
            }

            if (!allowed) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (!path.startsWith("/admin/") && !path.startsWith("/hr/")
                    && !path.startsWith("/interviewer/") && !path.startsWith("/candidate/")) {
                chain.doFilter(request, response);
                return;
            }
            if (!(session.getAttribute("userId") instanceof Integer userId)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            PermissionCode requiredPermission = PermissionPolicy.requiredForPath(path);
            if (requiredPermission != null) {
                try {
                    if (!permissionService.hasPermission(userId, role, requiredPermission)) {
                        res.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } catch (SQLException exception) {
                    res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                            "Không thể xác thực quyền truy cập. Vui lòng thử lại sau.");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
