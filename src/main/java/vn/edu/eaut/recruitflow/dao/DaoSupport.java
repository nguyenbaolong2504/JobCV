package vn.edu.eaut.recruitflow.dao;

import vn.edu.eaut.recruitflow.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Package-private JDBC helpers shared by DAO implementations. */
abstract class DaoSupport {
    protected Connection openConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    protected static int page(int page) {
        return Math.max(1, page);
    }

    protected static int pageSize(int pageSize) {
        return Math.min(100, Math.max(1, pageSize));
    }

    protected static int offset(int page, int pageSize) {
        return (page(page) - 1) * pageSize(pageSize);
    }

    protected static void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    protected static Integer getNullableInt(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }
}
