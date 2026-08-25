package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PublicCompanyController", urlPatterns = {"/companies", "/companies/detail"})
public class PublicCompanyController extends BaseController {
    private CompanyProfileService companyService;

    @Override
    public void init() {
        companyService = new CompanyProfileService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if ("/companies/detail".equals(request.getServletPath())) {
            try {
                int id = RequestUtil.requiredPositiveInt(request, "id", "Doanh nghiệp");
                var company = companyService.getCompany(id);
                request.setAttribute("company", company);
                request.setAttribute("companyJobs", companyService.getOpenJobs(id));
                request.setAttribute("pageDescription", "Khám phá thông tin và các vị trí đang tuyển tại " + company.getName() + ".");
                view(request, response, "/WEB-INF/views/public/company-detail.jsp", company.getName() + " | RecruitFlow");
                return;
            } catch (BusinessException | IllegalArgumentException exception) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        try {
            String keyword = RequestUtil.text(request, "keyword");
            if (keyword.length() > 100) throw new BusinessException("Từ khóa không được vượt quá 100 ký tự.");
            String sort = "name".equalsIgnoreCase(RequestUtil.text(request, "sort")) ? "name" : "jobs";
            var page = companyService.searchCompanies(keyword, true, sort,
                    RequestUtil.page(request), RequestUtil.pageSize(request));
            request.setAttribute("companies", page.getItems());
            request.setAttribute("page", page);
            request.setAttribute("keyword", keyword);
            request.setAttribute("sort", sort);
            request.setAttribute("pageDescription", "Khám phá nhà tuyển dụng, lĩnh vực hoạt động và các vị trí đang mở trên RecruitFlow.");
            view(request, response, "/WEB-INF/views/public/companies.jsp", "Danh sách công ty | RecruitFlow");
        } catch (BusinessException exception) {
            request.setAttribute("companies", java.util.List.of());
            request.setAttribute("error", exception.getMessage());
            view(request, response, "/WEB-INF/views/public/companies.jsp", "Danh sách công ty | RecruitFlow");
        }
    }
}
