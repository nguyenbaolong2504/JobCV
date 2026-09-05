package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.model.HomeBanner;
import vn.edu.eaut.recruitflow.service.HomeBannerService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.HomeBannerStorageUtil;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Streams only active, server-owned banner files to public pages. */
@WebServlet(name = "HomeBannerImageController", urlPatterns = "/home-banner")
public class HomeBannerImageController extends BaseController {
    private static final Map<String, String> TYPES = Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp");
    private final HomeBannerService bannerService = new HomeBannerService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = RequestUtil.requiredPositiveInt(request, "id", "Banner");
            HttpSession session = request.getSession(false);
            HomeBanner banner = session != null && "ADMIN".equals(session.getAttribute("role"))
                    ? bannerService.getForAdmin(id, RequestUtil.currentUserId(request)) : bannerService.getPublic(id);
            Path image = HomeBannerStorageUtil.resolveStored(HomeBannerStorageUtil.resolveDirectory(getServletContext()), banner.getImagePath());
            String name = image.getFileName().toString(); int dot = name.lastIndexOf('.'); String type = TYPES.get(dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT));
            if (type == null) throw new BusinessException("Định dạng banner không hợp lệ.");
            response.setContentType(type); response.setContentLengthLong(Files.size(image)); response.setHeader("X-Content-Type-Options", "nosniff"); response.setHeader("Cache-Control", "public, max-age=86400");
            Files.copy(image, response.getOutputStream());
        } catch (BusinessException | IllegalArgumentException exception) { response.sendError(HttpServletResponse.SC_NOT_FOUND); }
    }
}
