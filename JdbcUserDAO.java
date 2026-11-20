package dao;

import model.Role;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
/** JDBC implementation of UserDAO. */
public class JdbcUserDAO implements UserDAO {

    private final DatabaseManager db;

    public JdbcUserDAO(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, role, customer_id) VALUES (?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole().name());
            if (user.getCustomerId() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, user.getCustomerId());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save user " + user.getUsername() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT username, password_hash, salt, role, customer_id FROM users WHERE username = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(rs.getString("username"), rs.getString("password_hash"),
                            rs.getString("salt"), Role.valueOf(rs.getString("role")),
                            rs.getString("customer_id")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public int count() {
        return DaoSupport.count(db, "SELECT COUNT(*) FROM users");
    }
}
