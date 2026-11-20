package dao;

import model.Transaction;
import model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
/** JDBC implementation of TransactionDAO. */
public class JdbcTransactionDAO implements TransactionDAO {

    private final DatabaseManager db;

    public JdbcTransactionDAO(DatabaseManager db) {
        this.db = db;
    }

    /** Inserts a transaction using an existing connection (so callers can group it in a DB transaction). */
    static void insert(Connection c, Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, tx_type, amount, balance_after, "
                + "tx_timestamp, description) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getAccountNumber());
            ps.setString(2, tx.getType().name());
            ps.setDouble(3, tx.getAmount());
            ps.setDouble(4, tx.getBalanceAfter());
            ps.setString(5, tx.getTimestamp().toString());
            ps.setString(6, tx.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    tx.setId(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void save(Transaction transaction) {
        db.runInTransaction(c -> insert(c, transaction));
    }

    @Override
    public List<Transaction> findByAccount(String accountNumber) {
        String sql = "SELECT transaction_id, account_number, tx_type, amount, balance_after, "
                + "tx_timestamp, description FROM transactions WHERE account_number = ? "
                + "ORDER BY transaction_id";
        List<Transaction> result = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Transaction(
                            rs.getLong("transaction_id"),
                            rs.getString("account_number"),
                            TransactionType.valueOf(rs.getString("tx_type")),
                            rs.getDouble("amount"),
                            rs.getDouble("balance_after"),
                            LocalDateTime.parse(rs.getString("tx_timestamp")),
                            rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read transactions: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean interestPaidForMonth(String accountNumber, YearMonth month) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_number = ? "
                + "AND tx_type = 'INTEREST' AND tx_timestamp LIKE ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, month.toString() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check interest history: " + e.getMessage(), e);
        }
    }

    @Override
    public int count() {
        return DaoSupport.count(db, "SELECT COUNT(*) FROM transactions");
    }
}
