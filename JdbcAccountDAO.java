package dao;

import model.Account;
import model.AccountType;
import model.ChequeAccount;
import model.Customer;
import model.InvestmentAccount;
import model.SavingsAccount;
import model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/** JDBC implementation of AccountDAO. Needs the CustomerDAO to rebuild each account's owner. */
public class JdbcAccountDAO implements AccountDAO {

    private static final String COLUMNS =
        "account_number, account_type, customer_id, branch, balance, date_opened, "
        + "employer_name, employer_address";

    private final DatabaseManager db;
    private final CustomerDAO customers;

    public JdbcAccountDAO(DatabaseManager db, CustomerDAO customers) {
        this.db = db;
        this.customers = customers;
    }

    @Override
    public void save(Account account, Transaction openingTransaction) {
        db.runInTransaction(c -> {
            String sql = "INSERT INTO accounts (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, account.getAccountNumber());
                ps.setString(2, account.getType().name());
                ps.setString(3, account.getOwner().getCustomerId());
                ps.setString(4, account.getBranch());
                ps.setDouble(5, account.getBalance());
                ps.setString(6, account.getDateOpened().toString());
                if (account instanceof ChequeAccount cheque) {
                    ps.setString(7, cheque.getEmployerName());
                    ps.setString(8, cheque.getEmployerAddress());
                } else {
                    ps.setNull(7, Types.VARCHAR);
                    ps.setNull(8, Types.VARCHAR);
                }
                ps.executeUpdate();
            }
            if (openingTransaction != null) {
                JdbcTransactionDAO.insert(c, openingTransaction);
            }
        });
    }

    @Override
    public void update(Account account, Transaction transaction) {
        db.runInTransaction(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE accounts SET balance = ?, branch = ? WHERE account_number = ?")) {
                ps.setDouble(1, account.getBalance());
                ps.setString(2, account.getBranch());
                ps.setString(3, account.getAccountNumber());
                ps.executeUpdate();
            }
            if (transaction != null) {
                JdbcTransactionDAO.insert(c, transaction);
            }
        });
    }

    @Override
    public Optional<Account> findByNumber(String accountNumber) {
        List<Account> found = query("SELECT " + COLUMNS + " FROM accounts WHERE account_number = ?",
                accountNumber);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    @Override
    public List<Account> findByCustomer(String customerId) {
        return query("SELECT " + COLUMNS + " FROM accounts WHERE customer_id = ? ORDER BY account_number",
                customerId);
    }

    @Override
    public List<Account> findAll() {
        return query("SELECT " + COLUMNS + " FROM accounts ORDER BY account_number", null);
    }

    private List<Account> query(String sql, String param) {
        List<Account> result = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read accounts: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void delete(String accountNumber) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete account " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int count() {
        return DaoSupport.count(db, "SELECT COUNT(*) FROM accounts");
    }

    @Override
    public long maxSequence() {
        return DaoSupport.scalarLong(db,
            "SELECT COALESCE(MAX(CAST(SUBSTR(account_number, 2) AS INTEGER)), 0) FROM accounts");
    }

    private Account map(ResultSet rs) throws SQLException {
        String number = rs.getString("account_number");
        AccountType type = AccountType.valueOf(rs.getString("account_type"));
        String customerId = rs.getString("customer_id");
        Customer owner = customers.findById(customerId).orElseThrow(() ->
                new DataAccessException("Account " + number + " refers to unknown customer " + customerId, null));
        String branch = rs.getString("branch");
        double balance = rs.getDouble("balance");
        LocalDate opened = LocalDate.parse(rs.getString("date_opened"));
        return switch (type) {
            case SAVINGS -> new SavingsAccount(number, owner, branch, balance, opened);
            case INVESTMENT -> new InvestmentAccount(number, owner, branch, balance, opened);
            case CHEQUE -> new ChequeAccount(number, owner, branch, balance, opened,
                    rs.getString("employer_name"), rs.getString("employer_address"));
        };
    }
}
