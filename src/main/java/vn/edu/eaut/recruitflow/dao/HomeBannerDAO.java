package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.HomeBanner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC storage for the administrator-managed home-page campaign images. */
public class HomeBannerDAO extends DaoSupport {
    private static final String SELECT = "SELECT id,image_path,title,subtitle,target_url,display_order,is_active,created_at,updated_at FROM home_banners ";

    public List<HomeBanner> findAll() throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT + "ORDER BY display_order,id"); ResultSet result = statement.executeQuery()) {
            return list(result);
        }
    }

    public List<HomeBanner> findActive() throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE is_active=TRUE ORDER BY display_order,id"); ResultSet result = statement.executeQuery()) {
            return list(result);
        }
    }

    public HomeBanner findById(int id) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE id=?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; }
        }
    }

    public int insert(HomeBanner banner) throws SQLException {
        String sql = "INSERT INTO home_banners(image_path,title,subtitle,target_url,display_order,is_active) VALUES(?,?,?,?,?,?)";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, banner, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) { banner.setId(keys.getInt(1)); return banner.getId(); }
            }
        }
        throw new SQLException("Creating home banner did not return an id.");
    }

    public boolean update(HomeBanner banner) throws SQLException {
        String sql = "UPDATE home_banners SET title=?,subtitle=?,target_url=?,display_order=?,is_active=? WHERE id=?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, banner, true);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM home_banners WHERE id=?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, HomeBanner banner, boolean update) throws SQLException {
        int index = 1;
        if (!update) statement.setString(index++, banner.getImagePath());
        statement.setString(index++, banner.getTitle());
        statement.setString(index++, banner.getSubtitle());
        statement.setString(index++, banner.getTargetUrl());
        statement.setInt(index++, banner.getDisplayOrder());
        statement.setBoolean(index++, banner.isActive());
        if (update) statement.setInt(index, banner.getId());
    }

    private List<HomeBanner> list(ResultSet result) throws SQLException { List<HomeBanner> banners = new ArrayList<>(); while (result.next()) banners.add(map(result)); return banners; }
    private HomeBanner map(ResultSet result) throws SQLException {
        HomeBanner banner = new HomeBanner(); banner.setId(result.getInt("id")); banner.setImagePath(result.getString("image_path"));
        banner.setTitle(result.getString("title")); banner.setSubtitle(result.getString("subtitle")); banner.setTargetUrl(result.getString("target_url"));
        banner.setDisplayOrder(result.getInt("display_order")); banner.setActive(result.getBoolean("is_active"));
        banner.setCreatedAt(result.getTimestamp("created_at")); banner.setUpdatedAt(result.getTimestamp("updated_at")); return banner;
    }
}
