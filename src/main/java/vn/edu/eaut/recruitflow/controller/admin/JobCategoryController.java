package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Admin overview for managing career groups and their child categories. */
@WebServlet("/admin/job-categories")
public class JobCategoryController extends BaseController {
    private JobCategoryService jobCategoryService;

    @Override
    public void init() throws ServletException {
        jobCategoryService = new JobCategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("jobCategories", jobCategoryService.getAllCategories());
        } catch (BusinessException exception) {
            request.setAttribute("error", exception.getMessage());
        }
        view(request, response, "/WEB-INF/views/admin/job-categories.jsp", "Danh mục nghề nghiệp | JobCV");
    }
}
