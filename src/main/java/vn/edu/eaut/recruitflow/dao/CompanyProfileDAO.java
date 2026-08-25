package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.model.RecruiterProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database-backed employer directory and owner-scoped company profile updates. */
public class CompanyProfileDAO extends DaoSupport {
    private static final String SELECT_COMPANY = "SELECT rp.id, rp.user_id AS owner_user_id, "
            + "rp.organization_name AS company_name, rp.industry, rp.company_size, rp.address, rp.website, "
            + "rp.description, rp.logo_path, rp.cover_path, rp.is_verified, "
            + "u.full_name AS owner_name, u.email AS owner_email, u.status AS account_status, "
            + "COUNT(j.id) AS open_jobs "
            + "FROM recruiter_profiles rp "
            + "JOIN users u ON u.id = rp.user_id "
            + "JOIN roles r ON r.id = u.role_id AND r.role_name = 'HR' "
            + "LEFT JOIN jobs j ON j.created_by = rp.user_id AND j.status = 'PUBLISHED' "
            + "AND j.deadline >= CURRENT_DATE ";

    private static final String GROUP_BY = " GROUP BY rp.id, rp.user_id, rp.organization_name, rp.industry, "
            + "rp.company_size, rp.address, rp.website, rp.description, rp.logo_path, rp.cover_path, "
            + "rp.is_verified, u.full_name, u.email, u.status ";

    public CompanyProfile findById(int id, boolean publicOnly) throws SQLException {
        String sql = SELECT_COMPANY + "WHERE rp.id = ?" + (publicOnly
                ? " AND u.status = 'ACTIVE' AND rp.description IS NOT NULL AND TRIM(rp.description) <> ''"
                : "")
                + GROUP_BY;
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public CompanyProfile findByOwner(int ownerUserId) throws SQLException {
        String sql = SELECT_COMPANY + "WHERE rp.user_id = ?" + GROUP_BY;
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public List<CompanyProfile> search(String keyword, boolean publicOnly, String sort, int page, int pageSize)
            throws SQLException {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        StringBuilder sql = new StringBuilder(SELECT_COMPANY).append("WHERE 1 = 1");
        if (publicOnly) sql.append(" AND u.status = 'ACTIVE' AND rp.description IS NOT NULL AND TRIM(rp.description) <> ''");
        if (!normalized.isEmpty()) {
            sql.append(" AND (LOWER(rp.organization_name) LIKE ? OR LOWER(COALESCE(rp.industry, '')) LIKE ? "
                    + "OR LOWER(COALESCE(rp.address, '')) LIKE ?)");
        }
        sql.append(GROUP_BY);
        sql.append("name".equalsIgnoreCase(sort)
                ? " ORDER BY rp.organization_name, rp.id"
                : " ORDER BY rp.is_verified DESC, open_jobs DESC, rp.organization_name");
        sql.append(" LIMIT ? OFFSET ?");
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (!normalized.isEmpty()) {
                String value = '%' + normalized + '%';
                statement.setString(index++, value);
                statement.setString(index++, value);
                statement.setString(index++, value);
            }
            statement.setInt(index++, pageSize(pageSize));
            statement.setInt(index, offset(page, pageSize));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CompanyProfile> companies = new ArrayList<>();
                while (resultSet.next()) companies.add(map(resultSet));
                return companies;
            }
        }
    }

    public long count(String keyword, boolean publicOnly) throws SQLException {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM recruiter_profiles rp ")
                .append("JOIN users u ON u.id = rp.user_id ")
                .append("JOIN roles r ON r.id = u.role_id AND r.role_name = 'HR' WHERE 1 = 1");
        if (publicOnly) sql.append(" AND u.status = 'ACTIVE' AND rp.description IS NOT NULL AND TRIM(rp.description) <> ''");
        if (!normalized.isEmpty()) {
            sql.append(" AND (LOWER(rp.organization_name) LIKE ? OR LOWER(COALESCE(rp.industry, '')) LIKE ? "
                    + "OR LOWER(COALESCE(rp.address, '')) LIKE ?)");
        }
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (!normalized.isEmpty()) {
                String value = '%' + normalized + '%';
                statement.setString(1, value);
                statement.setString(2, value);
                statement.setString(3, value);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public boolean updateByOwner(int ownerUserId, RecruiterProfile profile) throws SQLException {
        String sql = "UPDATE recruiter_profiles SET organization_name = ?, job_title = ?, work_phone = ?, "
                + "industry = ?, company_size = ?, address = ?, website = ?, description = ?, "
                + "logo_path = ?, cover_path = ? WHERE user_id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.getOrganizationName());
            statement.setString(2, profile.getJobTitle());
            statement.setString(3, profile.getWorkPhone());
            statement.setString(4, profile.getIndustry());
            statement.setString(5, profile.getCompanySize());
            statement.setString(6, profile.getAddress());
            statement.setString(7, profile.getWebsite());
            statement.setString(8, profile.getDescription());
            statement.setString(9, profile.getLogoPath());
            statement.setString(10, profile.getCoverPath());
            statement.setInt(11, ownerUserId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean setVerified(int companyId, boolean verified) throws SQLException {
        String sql = "UPDATE recruiter_profiles rp JOIN users u ON u.id = rp.user_id "
                + "JOIN roles r ON r.id = u.role_id AND r.role_name = 'HR' "
                + "SET rp.is_verified = ? WHERE rp.id = ?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, verified);
            statement.setInt(2, companyId);
            return statement.executeUpdate() == 1;
        }
    }

    private CompanyProfile map(ResultSet resultSet) throws SQLException {
        CompanyProfile company = new CompanyProfile();
        company.setId(resultSet.getInt("id"));
        company.setOwnerUserId(resultSet.getInt("owner_user_id"));
        company.setName(resultSet.getString("company_name"));
        company.setIndustry(resultSet.getString("industry"));
        company.setSize(resultSet.getString("company_size"));
        company.setLocation(resultSet.getString("address"));
        company.setWebsite(resultSet.getString("website"));
        company.setDescription(resultSet.getString("description"));
        company.setLogoFile(resultSet.getString("logo_path"));
        company.setCoverFile(resultSet.getString("cover_path"));
        company.setVerified(resultSet.getBoolean("is_verified"));
        company.setOwnerName(resultSet.getString("owner_name"));
        company.setOwnerEmail(resultSet.getString("owner_email"));
        company.setAccountStatus(resultSet.getString("account_status"));
        company.setOpenJobs(resultSet.getLong("open_jobs"));
        return company;
    }
}
