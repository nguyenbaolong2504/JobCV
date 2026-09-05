package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.CompanyLogoStorageUtil;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "CompanyLogoController", urlPatterns = "/company-logo")
public class CompanyLogoController extends BaseController {
    private static final Map<String, String> TYPES = Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp");
    private final CompanyProfileService companyService = new CompanyProfileService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            CompanyProfile company = companyService.getCompany(RequestUtil.requiredPositiveInt(request, "id", "Công ty"));
            if (!company.isUploadedLogo()) throw new BusinessException("Công ty chưa tải logo.");
            Path logo = CompanyLogoStorageUtil.resolveStored(CompanyLogoStorageUtil.resolveDirectory(getServletContext()), company.getLogoFile());
            String name = logo.getFileName().toString(); int dot = name.lastIndexOf('.');
            String type = TYPES.get(dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT));
            if (type == null) throw new BusinessException("Định dạng logo không hợp lệ.");
            response.setContentType(type); response.setContentLengthLong(Files.size(logo));
            response.setHeader("X-Content-Type-Options", "nosniff"); response.setHeader("Cache-Control", "public, max-age=86400");
            Files.copy(logo, response.getOutputStream());
        } catch (BusinessException | IllegalArgumentException exception) { response.sendError(HttpServletResponse.SC_NOT_FOUND); }
    }
}
