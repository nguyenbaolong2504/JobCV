package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.PermissionService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Saves the selected permission codes atomically for a role. */
@WebServlet("/admin/permissions/update")
public class AdminPermissionUpdateController extends BaseController {
    private PermissionService permissionService;

    @Override
    public void init() {
        permissionService = new PermissionService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int roleId = 0;
        try {
            roleId = RequestUtil.requiredPositiveInt(request, "roleId", "Vai trò");
            String[] values = request.getParameterValues("permissionCode");
            List<String> codes = values == null ? List.of() : Arrays.asList(values);
            permissionService.updateRolePermissions(roleId, codes, RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/admin/permissions?roleId=" + roleId,
                    "Đã lưu ma trận phân quyền. Các request tiếp theo áp dụng quyền mới ngay.");
        } catch (BusinessException | IllegalArgumentException exception) {
            String suffix = roleId > 0 ? "?roleId=" + roleId : "";
            redirectWithError(request, response, "/admin/permissions" + suffix, exception.getMessage());
        }
    }
}
