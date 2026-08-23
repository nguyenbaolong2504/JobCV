package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.JobCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JDBC access for the curated, two-level job/career taxonomy. */
public class JobCategoryDAO extends DaoSupport {
    private static final String SELECT_CATEGORY = "SELECT c.id, c.parent_id, parent.name AS parent_name, c.name, c.description, "
            + "c.display_order, c.is_active, c.created_at, c.updated_at "
            + "FROM job_categories c LEFT JOIN job_categories parent ON parent.id = c.parent_id ";

    /** Flat list for Admin management. Parent groups are kept ahead of their child categories. */
    public List<JobCategory> findAll() throws SQLException {
        String sql = SELECT_CATEGORY
                + "ORDER BY CASE WHEN c.parent_id IS NULL THEN 0 ELSE 1 END, "
                + "COALESCE(parent.display_order, c.display_order), COALESCE(parent.name, c.name), "
                + "c.display_order, c.name, c.id";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    /** Active groups and active child categories for the public navigation. */
    public List<JobCategory> findActiveHierarchy() throws SQLException {
        String sql = SELECT_CATEGORY + "WHERE c.is_active = TRUE AND (c.parent_id IS NULL OR parent.is_active = TRUE) "
                + "ORDER BY CASE WHEN c.parent_id IS NULL THEN 0 ELSE 1 END, "
                + "COALESCE(parent.display_order, c.display_order), COALESCE(parent.name, c.name), "
                + "c.display_order, c.name, c.id";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<Integer, JobCategory> roots = new LinkedHashMap<>();
            while (resultSet.next()) {
                JobCategory category = map(resultSet);
                if (category.getParentId() == null) {
                    roots.put(category.getId(), category);
                } else {
                    JobCategory parent = roots.get(category.getParentId());
                    if (parent != null) {
                        parent.getChildren().add(category);
                    }
                }
            }
            // An active child with an inactive or missing parent is deliberately omitted.
            // The service prevents this state through Admin CRUD.
            return new ArrayList<>(roots.values());
        }
    }

    /** Active selectable categories for HR's job form. A category with active children is a grouping node. */
    public List<JobCategory> findActiveLeafCategories() throws SQLException {
        String sql = SELECT_CATEGORY + "WHERE c.is_active = TRUE "
                + "AND (c.parent_id IS NULL OR parent.is_active = TRUE) "
                + "AND NOT EXISTS (SELECT 1 FROM job_categories child "
                + "WHERE child.parent_id = c.id AND child.is_active = TRUE) "
                + "ORDER BY COALESCE(parent.display_order, c.display_order), COALESCE(parent.name, c.name), "
                + "c.display_order, c.name, c.id";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    public JobCategory findById(int id) throws SQLException {
        try (Connection connection = openConnection()) {
            return findById(connection, id);
        }
    }

    public JobCategory findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_CATEGORY + "WHERE c.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public JobCategory findByNameUnderParent(Connection connection, String name, Integer parentId) throws SQLException {
        String sql = SELECT_CATEGORY + "WHERE LOWER(c.name) = LOWER(?) "
                + (parentId == null ? "AND c.parent_id IS NULL" : "AND c.parent_id = ?");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            if (parentId != null) {
                statement.setInt(2, parentId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean hasChildren(Connection connection, int categoryId) throws SQLException {
        return exists(connection, "SELECT 1 FROM job_categories WHERE parent_id = ? LIMIT 1", categoryId);
    }

    public boolean hasActiveChildren(Connection connection, int categoryId) throws SQLException {
        return exists(connection, "SELECT 1 FROM job_categories WHERE parent_id = ? AND is_active = TRUE LIMIT 1", categoryId);
    }

    public boolean hasJobs(Connection connection, int categoryId) throws SQLException {
        return exists(connection, "SELECT 1 FROM jobs WHERE category_id = ? LIMIT 1", categoryId);
    }

    public int insert(Connection connection, JobCategory category) throws SQLException {
        String sql = "INSERT INTO job_categories (parent_id, name, description, display_order, is_active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableInt(statement, 1, category.getParentId());
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());
            statement.setInt(4, category.getDisplayOrder());
            statement.setBoolean(5, category.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                    return category.getId();
                }
            }
        }
        throw new SQLException("Creating job category did not return a generated id.");
    }

    public boolean update(Connection connection, JobCategory category) throws SQLException {
        String sql = "UPDATE job_categories SET parent_id = ?, name = ?, description = ?, display_order = ?, is_active = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setNullableInt(statement, 1, category.getParentId());
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());
            statement.setInt(4, category.getDisplayOrder());
            statement.setBoolean(5, category.isActive());
            statement.setInt(6, category.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM job_categories WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private boolean exists(Connection connection, String sql, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private List<JobCategory> mapList(ResultSet resultSet) throws SQLException {
        List<JobCategory> categories = new ArrayList<>();
        while (resultSet.next()) {
            categories.add(map(resultSet));
        }
        return categories;
    }

    private JobCategory map(ResultSet resultSet) throws SQLException {
        JobCategory category = new JobCategory();
        category.setId(resultSet.getInt("id"));
        category.setParentId(getNullableInt(resultSet, "parent_id"));
        category.setParentName(resultSet.getString("parent_name"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        category.setDisplayOrder(resultSet.getInt("display_order"));
        category.setActive(resultSet.getBoolean("is_active"));
        category.setCreatedAt(resultSet.getTimestamp("created_at"));
        category.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return category;
    }
}
