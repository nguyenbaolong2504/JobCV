package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/departments")
public class DepartmentController extends BaseController {
    private DepartmentService departmentService;

    @Override
    public void init() throws ServletException {
        departmentService = new DepartmentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("departments", departmentService.getAllDepartments());
            view(request, response, "/WEB-INF/views/admin/departments.jsp", "Quản lý phòng ban | JobCV");
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/admin/dashboard", "Không thể tải danh sách phòng ban.");
        }
    }

    /** Compatibility endpoint for the original department form. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String name = RequestUtil.text(request, "name");
            if (name.isEmpty()) {
                throw new BusinessException("Tên phòng ban là bắt buộc.");
            }
            departmentService.create(name, RequestUtil.text(request, "description"),
                    RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/admin/departments", "Đã thêm phòng ban.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/admin/departments", ex.getMessage());
        }
    }
}
