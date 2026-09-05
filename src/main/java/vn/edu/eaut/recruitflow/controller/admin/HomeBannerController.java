package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.HomeBanner;
import vn.edu.eaut.recruitflow.service.HomeBannerService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.HomeBannerStorageUtil;
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

/** Admin screen for uploading, ordering, enabling and removing home page campaign images. */
@WebServlet(name = "HomeBannerController", urlPatterns = {"/admin/home-banners", "/admin/home-banners/update", "/admin/home-banners/delete"})
@MultipartConfig(maxFileSize = UploadUtil.MAX_AVATAR_SIZE, maxRequestSize = UploadUtil.MAX_AVATAR_SIZE * 10 + 256 * 1024)
public class HomeBannerController extends BaseController {
    private HomeBannerService bannerService;
    @Override public void init() { bannerService = new HomeBannerService(); }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("homeBanners", bannerService.getAll(RequestUtil.currentUserId(request)));
        } catch (BusinessException exception) { request.setAttribute("error", exception.getMessage()); }
        view(request, response, "/WEB-INF/views/admin/home-banners.jsp", "Banner trang chủ | JobCV");
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getServletPath();
        if (path.endsWith("/delete")) { delete(request, response); return; }
        if (path.endsWith("/update")) { update(request, response); return; }
        upload(request, response);
    }

    private void upload(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Path directory = null;
        try {
            int actorId = RequestUtil.currentUserId(request); directory = HomeBannerStorageUtil.resolveDirectory(getServletContext());
            int order = number(request, "displayOrder"); int saved = 0;
            for (Part image : request.getParts()) {
                if (!"bannerFiles".equals(image.getName()) || image.getSize() == 0) continue;
                Path stored = UploadUtil.storeHomeBanner(image, directory);
                try {
                    HomeBanner banner = bind(request); banner.setImagePath(stored.getFileName().toString()); banner.setDisplayOrder(order + saved); bannerService.create(banner, actorId); saved++;
                } catch (BusinessException exception) { HomeBannerStorageUtil.deleteStored(directory, stored.getFileName().toString()); throw exception; }
            }
            if (saved == 0) throw new BusinessException("Vui lòng chọn ít nhất một ảnh JPG, PNG hoặc WEBP (tối đa 2 MB/ảnh).");
            redirectWithSuccess(request, response, "/admin/home-banners", "Đã thêm " + saved + " banner trang chủ.");
        } catch (BusinessException | IllegalArgumentException exception) { redirectWithError(request, response, "/admin/home-banners", exception.getMessage()); }
    }

    private void update(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int actorId = RequestUtil.currentUserId(request); HomeBanner banner = bind(request);
            banner.setId(RequestUtil.requiredPositiveInt(request, "id", "Banner")); bannerService.update(banner, actorId);
            redirectWithSuccess(request, response, "/admin/home-banners", "Đã cập nhật banner.");
        } catch (BusinessException | IllegalArgumentException exception) { redirectWithError(request, response, "/admin/home-banners", exception.getMessage()); }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path directory = null;
        try {
            int actorId = RequestUtil.currentUserId(request); HomeBanner deleted = bannerService.delete(RequestUtil.requiredPositiveInt(request, "id", "Banner"), actorId);
            directory = HomeBannerStorageUtil.resolveDirectory(getServletContext()); HomeBannerStorageUtil.deleteStored(directory, deleted.getImagePath());
            redirectWithSuccess(request, response, "/admin/home-banners", "Đã xóa banner.");
        } catch (BusinessException | IllegalArgumentException exception) { redirectWithError(request, response, "/admin/home-banners", exception.getMessage()); }
    }

    private HomeBanner bind(HttpServletRequest request) throws BusinessException {
        HomeBanner banner = new HomeBanner(); banner.setTitle(RequestUtil.text(request, "title")); banner.setSubtitle(RequestUtil.text(request, "subtitle"));
        banner.setTargetUrl(RequestUtil.text(request, "targetUrl")); banner.setDisplayOrder(number(request, "displayOrder")); banner.setActive("true".equals(request.getParameter("active")));
        return banner;
    }
    private int number(HttpServletRequest request, String field) throws BusinessException {
        String raw = RequestUtil.text(request, field); if (raw.isBlank()) return 0;
        try { return Math.max(0, Math.min(10000, Integer.parseInt(raw))); } catch (NumberFormatException exception) { throw new BusinessException("Thứ tự hiển thị không hợp lệ."); }
    }
}
