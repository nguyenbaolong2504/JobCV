package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.CompanyMediaStorageUtil;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Streams public company media without exposing server file-system paths. */
@WebServlet("/companies/media")
public class CompanyMediaController extends BaseController {
    private static final Map<String, String> TYPES = Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp");
    private CompanyProfileService companyService;

    @Override public void init() { companyService = new CompanyProfileService(); }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            CompanyProfile company = companyService.getCompany(RequestUtil.requiredPositiveInt(request, "id", "Doanh nghiệp"));
            String type = "cover".equals(RequestUtil.text(request, "type")) ? "cover" : "logo";
            String storedName = "cover".equals(type) ? company.getCoverFile() : company.getLogoFile();
            if (storedName == null || storedName.isBlank()) {
                if ("logo".equals(type)) {
                    response.sendRedirect(request.getContextPath() + "/assets/images/employers/generic-careers.svg");
                    return;
                }
                throw new BusinessException("Không tìm thấy ảnh bìa doanh nghiệp.");
            }
            if (!storedName.startsWith("company_")) {
                if (!storedName.matches("[A-Za-z0-9._-]{1,120}")) throw new BusinessException("Tên ảnh doanh nghiệp không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/assets/images/employers/" + storedName);
                return;
            }
            Path media = CompanyMediaStorageUtil.resolveStored(CompanyMediaStorageUtil.resolveDirectory(getServletContext()), storedName);
            String fileName = media.getFileName().toString();
            int dot = fileName.lastIndexOf('.');
            String contentType = dot < 0 ? null : TYPES.get(fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
            if (contentType == null) throw new BusinessException("Định dạng ảnh doanh nghiệp không hợp lệ.");
            response.reset();
            response.setContentType(contentType);
            response.setContentLengthLong(Files.size(media));
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "public, max-age=86400");
            Files.copy(media, response.getOutputStream());
        } catch (BusinessException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
