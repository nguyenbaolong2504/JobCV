package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Role;
import vn.edu.eaut.recruitflow.service.PermissionService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Admin screen for configuring the granular role-permission matrix. */
@WebServlet("/admin/permissions")
public class AdminPermissionController extends BaseController {
    private PermissionService permissionService;

    @Override
    public void init() throws ServletException {
        permissionService = new PermissionService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int actorId = RequestUtil.currentUserId(request);
            List<Role> roles = permissionService.getRoles(actorId);
            int selectedRoleId = selectedRoleId(request, roles);
            request.setAttribute("roles", roles);
            request.setAttribute("selectedRoleId", selectedRoleId);
            request.setAttribute("permissions", permissionService.getPermissions(actorId));
            request.setAttribute("assignedPermissionCodes", permissionService.getRolePermissionCodes(selectedRoleId, actorId));
        } catch (BusinessException | IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
        }
        view(request, response, "/WEB-INF/views/admin/permissions.jsp", "Phân quyền nâng cao | JobCV");
    }

    private int selectedRoleId(HttpServletRequest request, List<Role> roles) throws BusinessException {
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException("Chưa có vai trò hệ thống để cấu hình.");
        }
        String value = RequestUtil.text(request, "roleId");
        if (value.isEmpty()) return roles.get(0).getId();
        int roleId = RequestUtil.requiredPositiveInt(request, "roleId", "Vai trò");
        boolean exists = roles.stream().anyMatch(role -> role.getId() == roleId);
        if (!exists) throw new BusinessException("Vai trò được chọn không hợp lệ.");
        return roleId;
    }
}
