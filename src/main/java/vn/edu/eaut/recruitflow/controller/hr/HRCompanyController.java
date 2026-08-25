package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.RecruiterProfile;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.CompanyMediaStorageUtil;
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

/** Owner-scoped employer-branding page. */
@WebServlet("/hr/company")
@MultipartConfig(fileSizeThreshold = 512 * 1024, maxFileSize = UploadUtil.MAX_COMPANY_IMAGE_SIZE,
        maxRequestSize = 5L * 1024L * 1024L)
public class HRCompanyController extends BaseController {
    private CompanyProfileService companyService;

    @Override public void init() { companyService = new CompanyProfileService(); }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int ownerId = RequestUtil.currentUserId(request);
            request.setAttribute("profile", companyService.getRecruiterProfile(ownerId));
            request.setAttribute("company", companyService.getCompanyByOwner(ownerId));
        } catch (BusinessException exception) {
            request.setAttribute("error", exception.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/company.jsp", "Hồ sơ doanh nghiệp | RecruitFlow");
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path directory = null;
        Path newLogo = null;
        Path newCover = null;
        try {
            request.setCharacterEncoding("UTF-8");
            int ownerId = RequestUtil.currentUserId(request);
            RecruiterProfile current = companyService.getRecruiterProfile(ownerId);
            RecruiterProfile submitted = bind(request, current);
            directory = CompanyMediaStorageUtil.resolveDirectory(getServletContext());
            Part logoPart = request.getPart("logoFile");
            Part coverPart = request.getPart("coverFile");
            newLogo = UploadUtil.storeCompanyImage(logoPart, ownerId, "logo", directory);
            newCover = UploadUtil.storeCompanyImage(coverPart, ownerId, "cover", directory);
            if (newLogo != null) submitted.setLogoPath(newLogo.getFileName().toString());
            if (newCover != null) submitted.setCoverPath(newCover.getFileName().toString());
            if ("true".equals(request.getParameter("removeLogo"))) submitted.setLogoPath(null);
            if ("true".equals(request.getParameter("removeCover"))) submitted.setCoverPath(null);
            companyService.updateRecruiterProfile(ownerId, submitted);
            if (newLogo != null || submitted.getLogoPath() == null) CompanyMediaStorageUtil.deleteStored(directory, current.getLogoPath());
            if (newCover != null || submitted.getCoverPath() == null) CompanyMediaStorageUtil.deleteStored(directory, current.getCoverPath());
            redirectWithSuccess(request, response, "/hr/company", "Đã cập nhật hồ sơ doanh nghiệp.");
        } catch (BusinessException | ServletException | IllegalStateException exception) {
            if (directory != null && newLogo != null) CompanyMediaStorageUtil.deleteStored(directory, newLogo.getFileName().toString());
            if (directory != null && newCover != null) CompanyMediaStorageUtil.deleteStored(directory, newCover.getFileName().toString());
            redirectWithError(request, response, "/hr/company", exception.getMessage());
        }
    }

    private RecruiterProfile bind(HttpServletRequest request, RecruiterProfile current) throws BusinessException {
        RecruiterProfile profile = new RecruiterProfile();
        profile.setUserId(current.getUserId());
        profile.setOrganizationName(text(request, "organizationName", 150));
        profile.setJobTitle(text(request, "jobTitle", 100));
        profile.setWorkPhone(text(request, "workPhone", 30));
        profile.setIndustry(text(request, "industry", 120));
        profile.setCompanySize(text(request, "companySize", 60));
        profile.setAddress(text(request, "address", 255));
        profile.setWebsite(text(request, "website", 255));
        profile.setDescription(text(request, "description", 5000));
        profile.setLogoPath(current.getLogoPath());
        profile.setCoverPath(current.getCoverPath());
        profile.setVerified(current.isVerified());
        return profile;
    }

    private String text(HttpServletRequest request, String name, int max) throws BusinessException {
        String value = RequestUtil.text(request, name).replaceAll("\\s+$", "");
        if (value.length() > max) throw new BusinessException("Dữ liệu ở trường " + name + " vượt quá giới hạn cho phép.");
        return value;
    }
}
