package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/job-categories/update")
public class JobCategoryUpdateController extends BaseController {
    private JobCategoryService jobCategoryService;

    @Override
    public void init() throws ServletException {
        jobCategoryService = new JobCategoryService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            jobCategoryService.update(
                    RequestUtil.requiredPositiveInt(request, "id", "Danh mục"),
                    RequestUtil.text(request, "name"),
                    JobCategoryForm.parentId(request),
                    RequestUtil.text(request, "description"),
                    JobCategoryForm.displayOrder(request),
                    JobCategoryForm.active(request),
                    RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/admin/job-categories", "Đã cập nhật danh mục nghề nghiệp.");
        } catch (BusinessException | IllegalArgumentException exception) {
            redirectWithError(request, response, "/admin/job-categories", exception.getMessage());
        }
    }
}
