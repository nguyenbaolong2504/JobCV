package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.NotificationDAO;
import vn.edu.eaut.recruitflow.model.Notification;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this(new NotificationDAO());
    }

    NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public PageResult<Notification> getNotifications(int userId, int page, int pageSize) throws BusinessException {
        try {
            List<Notification> notifications = notificationDAO.findByUserId(userId, page, pageSize);
            long total = notificationDAO.countByUserId(userId);
            return new PageResult<>(notifications, page, pageSize, total);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông báo.", exception);
        }
    }

    public List<Notification> getUnread(int userId) throws BusinessException {
        try {
            return notificationDAO.findUnreadByUserId(userId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thông báo.", exception);
        }
    }

    public long unreadCount(int userId) throws BusinessException {
        try {
            return notificationDAO.countUnread(userId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải số thông báo chưa đọc.", exception);
        }
    }

    public void markRead(int userId, int notificationId) throws BusinessException {
        try {
            if (!notificationDAO.markRead(notificationId, userId)) {
                throw new BusinessException("Không tìm thấy thông báo.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể đánh dấu thông báo đã đọc.", exception);
        }
    }

    public void markAllRead(int userId) throws BusinessException {
        try {
            notificationDAO.markAllRead(userId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật thông báo.", exception);
        }
    }

    void create(Connection connection, int userId, String title, String message) throws SQLException {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationDAO.insert(connection, notification);
    }

    public void create(int userId, String title, String message) throws BusinessException {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notificationDAO.insert(notification);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tạo thông báo.", exception);
        }
    }
}
