package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Persistence for company tenants and their staff memberships. */
public class CompanyDAO extends DaoSupport {
    private static final String SELECT_COMPANY = "SELECT c.id, c.name, c.logo_path, c.industry, c.company_size, "
            + "c.address, c.website, c.description, c.status, "
            + "(SELECT COUNT(*) FROM jobs j WHERE j.company_id = c.id AND j.status = 'PUBLISHED' "
            + "AND j.deadline >= CURRENT_DATE) AS open_jobs FROM companies c ";

    public CompanyProfile findById(int id) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COMPANY + "WHERE c.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public CompanyProfile findActiveById(int id) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COMPANY + "WHERE c.id = ? AND c.status = 'ACTIVE'")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public CompanyProfile findByUserId(int userId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COMPANY
                     + "JOIN company_members cm ON cm.company_id = c.id "
                     + "WHERE cm.user_id = ? AND cm.is_active = TRUE AND c.status = 'ACTIVE'")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Integer findCompanyIdByUserId(Connection connection, int userId) throws SQLException {
        return findCompanyIdByUser(connection, userId);
    }

    public Integer findCompanyIdByUserId(int userId) throws SQLException {
        try (Connection connection = openConnection()) { return findCompanyIdByUser(connection, userId); }
    }

    public List<CompanyProfile> findActive(String keyword) throws SQLException {
        String sql = SELECT_COMPANY + "WHERE c.status = 'ACTIVE' "
                + "AND (? = '' OR LOWER(c.name) LIKE ? OR LOWER(COALESCE(c.industry,'')) LIKE ? "
                + "OR LOWER(COALESCE(c.address,'')) LIKE ?) ORDER BY open_jobs DESC, c.name";
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        String pattern = '%' + normalized + '%';
        List<CompanyProfile> result = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) result.add(map(resultSet));
            }
        }
        return result;
    }

    public boolean isActiveMember(Connection connection, int userId, int companyId, String role) throws SQLException {
        String sql = "SELECT 1 FROM company_members cm JOIN companies c ON c.id = cm.company_id "
                + "WHERE cm.user_id = ? AND cm.company_id = ? AND cm.member_role = ? "
                + "AND cm.is_active = TRUE AND c.status = 'ACTIVE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, companyId);
            statement.setString(3, role);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next(); }
        }
    }

    public List<User> findActiveInterviewers(int companyId) throws SQLException {
        String sql = "SELECT u.id, u.email, u.password_hash, u.full_name, u.role_id, "
                + "r.role_name, u.status, u.created_at, u.updated_at FROM users u "
                + "JOIN roles r ON r.id = u.role_id JOIN company_members cm ON cm.user_id = u.id "
                + "WHERE cm.company_id = ? AND cm.member_role = 'INTERVIEWER' AND cm.is_active = TRUE "
                + "AND u.status = 'ACTIVE' ORDER BY u.full_name";
        List<User> result = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id")); user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash")); user.setFullName(rs.getString("full_name"));
                    user.setRoleId(rs.getInt("role_id"));
                    user.setRoleName(rs.getString("role_name")); user.setStatus(rs.getString("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at")); user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    result.add(user);
                }
            }
        }
        return result;
    }

    public List<Integer> findActiveHrUserIds(int companyId) throws SQLException {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT cm.user_id FROM company_members cm JOIN users u ON u.id = cm.user_id "
                + "WHERE cm.company_id = ? AND cm.member_role = 'HR' AND cm.is_active = TRUE AND u.status = 'ACTIVE'";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) result.add(rs.getInt(1)); }
        }
        return result;
    }

    public Map<Integer, Integer> findMembershipCompanyIds(List<Integer> userIds) throws SQLException {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) return result;
        String placeholders = String.join(",", java.util.Collections.nCopies(userIds.size(), "?"));
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT user_id,company_id FROM company_members WHERE user_id IN (" + placeholders + ") AND is_active=TRUE")) {
            for (int i=0;i<userIds.size();i++) statement.setInt(i+1,userIds.get(i));
            try (ResultSet rs=statement.executeQuery()) { while(rs.next()) result.put(rs.getInt(1),rs.getInt(2)); }
        }
        return result;
    }

    public void assignMembership(Connection connection, int userId, int companyId, String memberRole) throws SQLException {
        String sql = "INSERT INTO company_members(company_id,user_id,member_role,is_active) VALUES (?,?,?,TRUE) "
                + "ON DUPLICATE KEY UPDATE company_id=VALUES(company_id),member_role=VALUES(member_role),is_active=TRUE";
        try (PreparedStatement statement=connection.prepareStatement(sql)) {
            statement.setInt(1,companyId); statement.setInt(2,userId); statement.setString(3,memberRole); statement.executeUpdate();
        }
    }

    public boolean update(CompanyProfile company) throws SQLException {
        String sql = "UPDATE companies SET name=?, logo_path=?, industry=?, company_size=?, address=?, website=?, description=? WHERE id=?";
        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, company.getName()); statement.setString(2, company.getLogoFile());
            statement.setString(3, company.getIndustry()); statement.setString(4, company.getSize());
            statement.setString(5, company.getLocation()); statement.setString(6, company.getWebsite());
            statement.setString(7, company.getDescription()); statement.setInt(8, company.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public int ensureCompanyMembership(Connection connection, int userId, String organizationName, String jobTitle) throws SQLException {
        Integer existing = findCompanyIdByUser(connection, userId);
        if (existing != null) return existing;
        Integer companyId = findCompanyIdByName(connection, organizationName);
        if (companyId == null) {
            try (PreparedStatement st = connection.prepareStatement("INSERT INTO companies (name, description) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, organizationName); st.setString(2, "Trang tuyển dụng chính thức của " + organizationName + " trên JobCV.");
                st.executeUpdate();
                try (ResultSet keys = st.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Missing company id"); companyId = keys.getInt(1); }
            }
        }
        try (PreparedStatement st = connection.prepareStatement("INSERT INTO company_members (company_id,user_id,member_role,job_title) VALUES (?,?,'HR',?)")) {
            st.setInt(1, companyId); st.setInt(2, userId); st.setString(3, jobTitle); st.executeUpdate();
        }
        return companyId;
    }

    private Integer findCompanyIdByUser(Connection connection, int userId) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement("SELECT company_id FROM company_members WHERE user_id=? AND is_active=TRUE")) {
            st.setInt(1, userId); try (ResultSet rs = st.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
        }
    }

    private Integer findCompanyIdByName(Connection connection, String name) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement("SELECT id FROM companies WHERE name=?")) {
            st.setString(1, name); try (ResultSet rs = st.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
        }
    }

    private CompanyProfile map(ResultSet rs) throws SQLException {
        CompanyProfile company = new CompanyProfile();
        company.setId(rs.getInt("id")); company.setName(rs.getString("name"));
        String logo = rs.getString("logo_path"); company.setLogoFile(logo == null || logo.isBlank() ? "generic-careers.svg" : logo);
        company.setIndustry(rs.getString("industry")); company.setSize(rs.getString("company_size"));
        company.setLocation(rs.getString("address")); company.setWebsite(rs.getString("website"));
        company.setDescription(rs.getString("description")); company.setVerified("ACTIVE".equals(rs.getString("status")));
        company.setOpenJobs(rs.getLong("open_jobs"));
        company.setHighlights(List.of("Thông tin doanh nghiệp được quản lý tập trung",
                "Tin tuyển dụng hiển thị minh bạch", "Hồ sơ được chuyển đúng bộ phận tuyển dụng"));
        return company;
    }
}
