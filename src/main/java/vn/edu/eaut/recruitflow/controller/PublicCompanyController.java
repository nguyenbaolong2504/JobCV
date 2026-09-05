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
        try {
            if ("/companies/detail".equals(request.getServletPath())) {
                int id = RequestUtil.requiredPositiveInt(request, "id", "Doanh nghiệp");
                request.setAttribute("company", companyService.getCompany(id));
                request.setAttribute("companyJobs", companyService.getOpenJobs(id));
                view(request, response, "/WEB-INF/views/public/company-detail.jsp", "Thông tin doanh nghiệp | JobCV");
                return;
            }
            String keyword = RequestUtil.text(request, "keyword");
            if (keyword.length() > 100) throw new BusinessException("Từ khóa không được vượt quá 100 ký tự.");
            request.setAttribute("companies", companyService.getCompanies(keyword));
            request.setAttribute("keyword", keyword);
            view(request, response, "/WEB-INF/views/public/companies.jsp", "Danh sách công ty | JobCV");
        } catch (BusinessException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
