package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.Job;
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
import java.math.BigDecimal;
import java.sql.Date;

@WebServlet("/hr/jobs/create")
public class JobCreateController extends BaseController {
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
            request.setAttribute("departments", departmentService.getAllDepartments());
        } catch (BusinessException ex) {
            redirectWithError(request, response, "/hr/jobs", ex.getMessage());
            return;
        }
        try {
            request.setAttribute("jobCategories", jobCategoryService.getActiveLeafCategories());
        } catch (BusinessException ignored) {
            // Category selection is optional; HR can still create an uncategorised job during a staged migration.
            request.setAttribute("jobCategories", java.util.List.of());
        }
        view(request, response, "/WEB-INF/views/hr/job-form.jsp", "Tạo tin tuyển dụng | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int actorId = RequestUtil.currentUserId(request);
            Job job = bindJob(request);
            job.setCreatedBy(actorId);
            job.setStatus(JobStatus.DRAFT);
            jobService.createJob(job, RequestUtil.text(request, "skills"), actorId);
            redirectWithSuccess(request, response, "/hr/jobs", "Đã tạo tin tuyển dụng ở trạng thái bản nháp.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/jobs/create", ex.getMessage());
        }
    }

    static Job bindJob(HttpServletRequest request) throws BusinessException {
        String jobCode = RequestUtil.text(request, "jobCode");
        String title = RequestUtil.text(request, "title");
        if (jobCode.isEmpty() || title.isEmpty()) {
            throw new BusinessException("Mã tin và tiêu đề công việc là bắt buộc.");
        }
        BigDecimal salaryMin = RequestUtil.decimal(request, "salaryMin", "Lương tối thiểu");
        BigDecimal salaryMax = RequestUtil.decimal(request, "salaryMax", "Lương tối đa");
        if (salaryMin.signum() < 0 || salaryMax.compareTo(salaryMin) < 0) {
            throw new BusinessException("Khoảng lương không hợp lệ.");
        }

        Job job = new Job();
        job.setJobCode(jobCode);
        job.setTitle(title);
        job.setDepartmentId(RequestUtil.requiredPositiveInt(request, "departmentId", "Phòng ban"));
        String categoryId = RequestUtil.text(request, "categoryId");
        job.setCategoryId(categoryId.isEmpty() ? null
                : RequestUtil.requiredPositiveInt(request, "categoryId", "Danh mục nghề nghiệp"));
        job.setLocation(RequestUtil.text(request, "location"));
        job.setEmploymentType(EmploymentType.fromValue(RequestUtil.text(request, "employmentType")));
        job.setNumberOfPositions(RequestUtil.requiredPositiveInt(request, "numberOfPositions", "Số lượng tuyển"));
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setDescription(RequestUtil.text(request, "description"));
        job.setRequirements(RequestUtil.text(request, "requirements"));
        job.setBenefits(RequestUtil.text(request, "benefits"));
        job.setExperienceRequired(RequestUtil.nonNegativeInt(request, "experienceRequired", "Kinh nghiệm yêu cầu"));
        job.setDeadline(Date.valueOf(RequestUtil.date(request, "deadline", "Hạn nộp hồ sơ")));
        return job;
    }
}
