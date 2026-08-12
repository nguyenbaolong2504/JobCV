package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO extends DaoSupport {
    public Notification findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, title, message, link_url, is_read, created_at FROM notifications WHERE id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<Notification> findByUserId(int userId, int page, int pageSize) throws SQLException {
        String sql = "SELECT id, user_id, title, message, link_url, is_read, created_at FROM notifications WHERE user_id = ? "
                + "ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, pageSize(pageSize));
            statement.setInt(3, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<Notification> findUnreadByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, title, message, link_url, is_read, created_at FROM notifications WHERE user_id = ? AND is_read = FALSE "
                + "ORDER BY created_at DESC, id DESC";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public int insert(Notification notification) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, notification);
        }
    }

    public int insert(Connection connection, Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, link_url, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, notification.getUserId());
            statement.setString(2, notification.getTitle());
            statement.setString(3, notification.getMessage());
            statement.setString(4, notification.getLinkUrl());
            statement.setBoolean(5, notification.isRead());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setId(keys.getInt(1));
                    return notification.getId();
                }
            }
        }
        throw new SQLException("Creating notification did not return a generated id.");
    }

    public boolean markRead(int notificationId, int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, notificationId);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public int markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate();
        }
    }

    public long countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private List<Notification> mapList(ResultSet resultSet) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        while (resultSet.next()) {
            notifications.add(map(resultSet));
        }
        return notifications;
    }

    private Notification map(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setId(resultSet.getInt("id"));
        notification.setUserId(resultSet.getInt("user_id"));
        notification.setTitle(resultSet.getString("title"));
        notification.setMessage(resultSet.getString("message"));
        notification.setLinkUrl(resultSet.getString("link_url"));
        notification.setRead(resultSet.getBoolean("is_read"));
        notification.setCreatedAt(resultSet.getTimestamp("created_at"));
        return notification;
    }
}
