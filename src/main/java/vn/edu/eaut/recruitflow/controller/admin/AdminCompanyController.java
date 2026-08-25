package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/companies")
public class AdminCompanyController extends BaseController {
    private CompanyProfileService companyService;
    @Override public void init() { companyService = new CompanyProfileService(); }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            var page = companyService.searchCompanies(RequestUtil.text(request, "keyword"), false,
                    RequestUtil.text(request, "sort"), RequestUtil.page(request), RequestUtil.pageSize(request));
            request.setAttribute("companies", page.getItems());
            request.setAttribute("page", page);
        } catch (BusinessException exception) { request.setAttribute("error", exception.getMessage()); }
        view(request, response, "/WEB-INF/views/admin/companies.jsp", "Quản trị doanh nghiệp | RecruitFlow");
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int companyId = RequestUtil.requiredPositiveInt(request, "companyId", "Doanh nghiệp");
            boolean verified = "true".equalsIgnoreCase(RequestUtil.text(request, "verified"));
            companyService.setVerified(companyId, verified, RequestUtil.currentUserId(request), request.getRemoteAddr());
            FlashMessage.success(request.getSession(), verified
                    ? "Đã xác thực hồ sơ doanh nghiệp."
                    : "Đã thu hồi xác thực doanh nghiệp.");
        } catch (BusinessException exception) {
            FlashMessage.error(request.getSession(), exception.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/companies");
    }
}
