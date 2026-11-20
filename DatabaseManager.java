package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the JDBC connection settings and the database schema.
 * Default database: SQLite file "smbanking.db" in the folder the program runs from.
 *
 * NOTE: every call opens its own connection, so do not use "jdbc:sqlite::memory:"
 * (each connection would get its own empty database). Use a file instead.
 */
public class DatabaseManager {

    public static final String DEFAULT_URL = "jdbc:sqlite:smbanking.db";

    /** A unit of work executed inside one database transaction. */
    @FunctionalInterface
    public interface SqlWork {
        void run(Connection connection) throws SQLException;
    }

    private static final String[] SCHEMA = {
        "CREATE TABLE IF NOT EXISTS customers ("
            + " customer_id TEXT PRIMARY KEY,"
            + " customer_type TEXT NOT NULL CHECK (customer_type IN ('INDIVIDUAL','COMPANY')),"
            + " first_name TEXT, surname TEXT, id_number TEXT,"
            + " company_name TEXT, registration_number TEXT,"
            + " address TEXT NOT NULL, phone TEXT NOT NULL, email TEXT NOT NULL)",

        "CREATE TABLE IF NOT EXISTS accounts ("
            + " account_number TEXT PRIMARY KEY,"
            + " account_type TEXT NOT NULL CHECK (account_type IN ('SAVINGS','INVESTMENT','CHEQUE')),"
            + " customer_id TEXT NOT NULL REFERENCES customers(customer_id) ON DELETE RESTRICT,"
            + " branch TEXT NOT NULL,"
            + " balance REAL NOT NULL CHECK (balance >= 0),"
            + " date_opened TEXT NOT NULL,"
            + " employer_name TEXT, employer_address TEXT)",

        "CREATE TABLE IF NOT EXISTS transactions ("
            + " transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " account_number TEXT NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,"
            + " tx_type TEXT NOT NULL CHECK (tx_type IN ('DEPOSIT','WITHDRAWAL','INTEREST')),"
            + " amount REAL NOT NULL CHECK (amount > 0),"
            + " balance_after REAL NOT NULL,"
            + " tx_timestamp TEXT NOT NULL,"
            + " description TEXT)",

        "CREATE TABLE IF NOT EXISTS users ("
            + " username TEXT PRIMARY KEY,"
            + " password_hash TEXT NOT NULL,"
            + " salt TEXT NOT NULL,"
            + " role TEXT NOT NULL CHECK (role IN ('TELLER','CUSTOMER')),"
            + " customer_id TEXT REFERENCES customers(customer_id) ON DELETE CASCADE)",

        "CREATE INDEX IF NOT EXISTS idx_accounts_customer ON accounts(customer_id)",
        "CREATE INDEX IF NOT EXISTS idx_tx_account ON transactions(account_number)"
    };

    private final String url;

    public DatabaseManager(String url) {
        this.url = url;
    }

    public DatabaseManager() {
        this(DEFAULT_URL);
    }

    public String getUrl() {
        return url;
    }

    /** Opens a new connection (foreign keys switched on). Callers close it. */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /** Creates the tables if they do not exist yet. Safe to call on every start-up. */
    public void initSchema() {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            for (String sql : SCHEMA) {
                st.execute(sql);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the database schema. "
                    + "Is the SQLite JDBC jar on the classpath? (" + e.getMessage() + ")", e);
        }
    }

    /** Runs the work in a single transaction: commit if it succeeds, roll back if it fails. */
    public void runInTransaction(SqlWork work) {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database transaction failed: " + e.getMessage(), e);
        }
    }
}
