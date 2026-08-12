package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.model.JobSkill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JobSkillDAO extends DaoSupport {
    public List<JobSkill> findByJobId(int jobId) throws SQLException {
        try (Connection connection = openConnection()) {
            return findByJobId(connection, jobId);
        }
    }

    public List<JobSkill> findByJobId(Connection connection, int jobId) throws SQLException {
        String sql = "SELECT id, job_id, skill_name, weight, is_required FROM job_skills WHERE job_id = ? ORDER BY is_required DESC, weight DESC, skill_name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<JobSkill> skills = new ArrayList<>();
                while (resultSet.next()) {
                    skills.add(map(resultSet));
                }
                return skills;
            }
        }
    }

    public int insert(JobSkill skill) throws SQLException {
        try (Connection connection = openConnection()) {
            return insert(connection, skill);
        }
    }

    public int insert(Connection connection, JobSkill skill) throws SQLException {
        String sql = "INSERT INTO job_skills (job_id, skill_name, weight, is_required) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, skill.getJobId());
            statement.setString(2, skill.getSkillName());
            statement.setInt(3, skill.getWeight());
            statement.setBoolean(4, skill.isRequired());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    skill.setId(keys.getInt(1));
                    return skill.getId();
                }
            }
        }
        throw new SQLException("Creating job skill did not return a generated id.");
    }

    public boolean deleteByJobId(Connection connection, int jobId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM job_skills WHERE job_id = ?")) {
            statement.setInt(1, jobId);
            statement.executeUpdate();
            return true;
        }
    }

    /** Replaces skills atomically when used with a caller-owned transaction connection. */
    public void replaceSkills(Connection connection, int jobId, List<JobSkill> skills) throws SQLException {
        deleteByJobId(connection, jobId);
        if (skills == null) {
            return;
        }
        for (JobSkill skill : skills) {
            skill.setJobId(jobId);
            insert(connection, skill);
        }
    }

    public void replaceSkills(int jobId, List<JobSkill> skills) throws SQLException {
        try (Connection connection = openConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                replaceSkills(connection, jobId, skills);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private JobSkill map(ResultSet resultSet) throws SQLException {
        JobSkill skill = new JobSkill();
        skill.setId(resultSet.getInt("id"));
        skill.setJobId(resultSet.getInt("job_id"));
        skill.setSkillName(resultSet.getString("skill_name"));
        skill.setWeight(resultSet.getInt("weight"));
        skill.setRequired(resultSet.getBoolean("is_required"));
        return skill;
    }
}
