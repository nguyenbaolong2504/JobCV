package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.service.DepartmentService;
import vn.edu.eaut.recruitflow.service.JobService;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/jobs")
public class HRJobController extends BaseController {
    private JobService jobService;
    private DepartmentService departmentService;
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        jobService = new JobService();
        departmentService = new DepartmentService();
        applicationService = new ApplicationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer departmentId = optionalPositiveInt(request, "departmentId", "Phòng ban");
            JobStatus status = optionalJobStatus(RequestUtil.text(request, "status"));
            EmploymentType employmentType = optionalEmploymentType(RequestUtil.text(request, "employmentType"));
            PageResult<Job> page = jobService.searchForHr(
                    RequestUtil.text(request, "keyword"),
                    departmentId,
                    RequestUtil.text(request, "location"),
                    employmentType,
                    status,
                    RequestUtil.text(request, "sort"),
                    RequestUtil.page(request),
                    RequestUtil.pageSize(request),
                    RequestUtil.currentUserId(request)
            );
            request.setAttribute("jobs", page.getItems());
            request.setAttribute("page", page);
            request.setAttribute("departments", departmentService.getAllDepartments());
            request.setAttribute("applicationCounts", applicationService.countByJobForHr(RequestUtil.currentUserId(request)));
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/jobs.jsp", "Quản lý tin tuyển dụng | RecruitFlow");
    }

    private Integer optionalPositiveInt(HttpServletRequest request, String field, String label) throws BusinessException {
        return RequestUtil.text(request, field).isEmpty() ? null : RequestUtil.requiredPositiveInt(request, field, label);
    }

    private JobStatus optionalJobStatus(String value) {
        return value.isEmpty() ? null : JobStatus.fromValue(value);
    }

    private EmploymentType optionalEmploymentType(String value) {
        return value.isEmpty() ? null : EmploymentType.fromValue(value);
    }
}
