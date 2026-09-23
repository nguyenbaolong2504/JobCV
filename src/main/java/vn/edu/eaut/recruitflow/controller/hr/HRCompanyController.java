package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.CompanyLogoStorageUtil;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.UploadUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Path;

@WebServlet(name = "HRCompanyController", urlPatterns = "/hr/company")
@MultipartConfig(maxFileSize = UploadUtil.MAX_AVATAR_SIZE, maxRequestSize = UploadUtil.MAX_AVATAR_SIZE + 64 * 1024)
public class HRCompanyController extends BaseController {
    private CompanyProfileService companyService;
    @Override public void init() { companyService = new CompanyProfileService(); }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("company", companyService.getCompanyForMember(RequestUtil.currentUserId(request)));
            view(request, response, "/WEB-INF/views/hr/company.jsp", "Thông tin công ty | JobCV");
        } catch (BusinessException exception) { redirectWithError(request, response, "/hr/dashboard", exception.getMessage()); }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Path stored = null; Path directory = null;
        try {
            int actorId = RequestUtil.currentUserId(request);
            CompanyProfile current = companyService.getCompanyForMember(actorId);
            CompanyProfile submitted = new CompanyProfile();
            submitted.setName(required(request, "name", "Tên công ty", 150));
            submitted.setIndustry(optional(request, "industry", 150)); submitted.setSize(optional(request, "size", 100));
            submitted.setLocation(optional(request, "location", 255)); submitted.setWebsite(optional(request, "website", 255));
            submitted.setDescription(optional(request, "description", 5000)); submitted.setLogoFile(current.getLogoFile());
            Part logo = request.getPart("logoFile");
            if (logo != null && logo.getSize() > 0) {
                directory = CompanyLogoStorageUtil.resolveDirectory(getServletContext());
                stored = UploadUtil.storeCompanyLogo(logo, current.getId(), directory);
                submitted.setLogoFile(stored.getFileName().toString());
            }
            companyService.updateCompany(submitted, actorId);
            if (stored != null) CompanyLogoStorageUtil.deleteStored(directory, current.getLogoFile());
            redirectWithSuccess(request, response, "/hr/company", "Đã cập nhật thông tin công ty.");
        } catch (BusinessException | IllegalArgumentException exception) {
            if (stored != null && directory != null) CompanyLogoStorageUtil.deleteStored(directory, stored.getFileName().toString());
            redirectWithError(request, response, "/hr/company", exception.getMessage());
        }
    }

    private String required(HttpServletRequest request, String name, String label, int max) throws BusinessException {
        String value = RequestUtil.text(request, name); if (value.isBlank()) throw new BusinessException(label + " là bắt buộc.");
        if (value.length() > max) throw new BusinessException(label + " không được vượt quá " + max + " ký tự."); return value;
    }
    private String optional(HttpServletRequest request, String name, int max) throws BusinessException {
        String value = RequestUtil.text(request, name); if (value.length() > max) throw new BusinessException("Dữ liệu không được vượt quá " + max + " ký tự.");
        return value.isBlank() ? null : value;
    }
}
