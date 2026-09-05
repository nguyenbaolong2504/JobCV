package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.AdminService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/users/company")
public class AdminUserCompanyController extends BaseController {
    private final AdminService adminService = new AdminService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            adminService.assignCompany(RequestUtil.requiredPositiveInt(request,"userId","Người dùng"),
                    RequestUtil.requiredPositiveInt(request,"companyId","Công ty"),RequestUtil.currentUserId(request));
            redirectWithSuccess(request,response,"/admin/users","Đã liên kết tài khoản với công ty.");
        } catch (BusinessException exception) { redirectWithError(request,response,"/admin/users",exception.getMessage()); }
    }
}
