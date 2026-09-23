package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.HomeBannerDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.model.HomeBanner;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.util.List;

/** Admin-only curation rules for the visual banner carousel. */
public class HomeBannerService {
    private final HomeBannerDAO bannerDAO;
    private final UserDAO userDAO;

    public HomeBannerService() { this(new HomeBannerDAO(), new UserDAO()); }
    HomeBannerService(HomeBannerDAO bannerDAO, UserDAO userDAO) { this.bannerDAO = bannerDAO; this.userDAO = userDAO; }

    public List<HomeBanner> getActive() throws BusinessException {
        try { return bannerDAO.findActive(); }
        catch (SQLException exception) { throw new BusinessException("Không thể tải banner trang chủ.", exception); }
    }
    public List<HomeBanner> getAll(int actorId) throws BusinessException {
        requireAdmin(actorId);
        try { return bannerDAO.findAll(); }
        catch (SQLException exception) { throw new BusinessException("Không thể tải danh sách banner.", exception); }
    }
    public HomeBanner getPublic(int id) throws BusinessException {
        try {
            HomeBanner banner = bannerDAO.findById(id);
            if (banner == null || !banner.isActive()) throw new BusinessException("Không tìm thấy banner.");
            return banner;
        } catch (SQLException exception) { throw new BusinessException("Không thể tải banner.", exception); }
    }
    public HomeBanner getForAdmin(int id, int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            HomeBanner banner = bannerDAO.findById(id);
            if (banner == null) throw new BusinessException("Không tìm thấy banner.");
            return banner;
        } catch (SQLException exception) { throw new BusinessException("Không thể tải banner.", exception); }
    }
    public void create(HomeBanner banner, int actorId) throws BusinessException {
        requireAdmin(actorId); validate(banner, true);
        try { bannerDAO.insert(banner); }
        catch (SQLException exception) { throw new BusinessException("Không thể lưu banner.", exception); }
    }
    public void update(HomeBanner submitted, int actorId) throws BusinessException {
        requireAdmin(actorId); validate(submitted, false);
        try {
            HomeBanner existing = bannerDAO.findById(submitted.getId());
            if (existing == null) throw new BusinessException("Không tìm thấy banner cần cập nhật.");
            submitted.setImagePath(existing.getImagePath());
            if (!bannerDAO.update(submitted)) throw new BusinessException("Không thể cập nhật banner.");
        } catch (SQLException exception) { throw new BusinessException("Không thể cập nhật banner.", exception); }
    }
    public HomeBanner delete(int id, int actorId) throws BusinessException {
        requireAdmin(actorId);
        try {
            HomeBanner existing = bannerDAO.findById(id);
            if (existing == null) throw new BusinessException("Không tìm thấy banner cần xóa.");
            if (!bannerDAO.delete(id)) throw new BusinessException("Không thể xóa banner.");
            return existing;
        } catch (SQLException exception) { throw new BusinessException("Không thể xóa banner.", exception); }
    }

    private void validate(HomeBanner banner, boolean requireImage) throws BusinessException {
        if (banner == null || (requireImage && (banner.getImagePath() == null || banner.getImagePath().isBlank()))) throw new BusinessException("Vui lòng chọn ảnh banner.");
        banner.setTitle(clean(banner.getTitle(), "Tiêu đề", 120));
        banner.setSubtitle(clean(banner.getSubtitle(), "Mô tả", 300));
        String target = clean(banner.getTargetUrl(), "Liên kết", 500);
        if (target != null && (!target.startsWith("/") || target.startsWith("//") || !target.matches("/[A-Za-z0-9/_?=&%#.-]*"))) {
            throw new BusinessException("Liên kết banner phải là đường dẫn nội bộ, ví dụ /jobs hoặc /candidate/jobs.");
        }
        banner.setTargetUrl(target); banner.setDisplayOrder(Math.max(0, Math.min(10000, banner.getDisplayOrder())));
    }
    private String clean(String value, String label, int max) throws BusinessException {
        if (value == null || value.trim().isEmpty()) return null;
        String result = value.trim(); if (result.length() > max) throw new BusinessException(label + " không được vượt quá " + max + " ký tự."); return result;
    }
    private void requireAdmin(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !"ADMIN".equals(user.getRoleName())) throw new BusinessException("Chỉ Admin được quản lý banner trang chủ.");
        } catch (SQLException exception) { throw new BusinessException("Không thể xác thực quyền quản trị.", exception); }
    }
}
