package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.JobSkill;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobCategoryService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/hr/jobs/edit")
public class JobEditController extends BaseController {
    private JobService jobService;
    private DepartmentService departmentService;
    private JobCategoryService jobCategoryService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        jobCategoryService = new JobCategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int jobId = RequestUtil.requiredPositiveInt(request, "id", "Tin tuyển dụng");
            Job job = jobService.getJobById(jobId);
            request.setAttribute("job", job);
            request.setAttribute("skillsText", job.getSkills().stream()
                    .map(this::toSkillInput)
                    .collect(Collectors.joining(", ")));
            request.setAttribute("departments", departmentService.getAllDepartments());
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/jobs", ex.getMessage());
            return;
        }
        try {
            request.setAttribute("jobCategories", jobCategoryService.getActiveLeafCategories());
        } catch (BusinessException ignored) {
            request.setAttribute("jobCategories", java.util.List.of());
        }
        view(request, response, "/WEB-INF/views/hr/job-form.jsp", "Chỉnh sửa tin tuyển dụng | RecruitFlow");
    }

    private String toSkillInput(JobSkill skill) {
        return skill.getSkillName() + ":" + skill.getWeight();
    }
}
