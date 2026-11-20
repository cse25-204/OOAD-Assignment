package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Small helpers shared by the JDBC DAO classes. */
final class DaoSupport {

    private DaoSupport() {
    }

    static long scalarLong(DatabaseManager db, String sql) {
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DataAccessException("Database query failed: " + e.getMessage(), e);
        }
    }

    static int count(DatabaseManager db, String sql) {
        return (int) scalarLong(db, sql);
    }
}
