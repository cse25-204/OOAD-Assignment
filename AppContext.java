package controller;

import dao.AccountDAO;
import dao.CustomerDAO;
import dao.DatabaseManager;
import dao.InMemoryStore;
import dao.JdbcAccountDAO;
import dao.JdbcCustomerDAO;
import dao.JdbcTransactionDAO;
import dao.JdbcUserDAO;
import dao.TransactionDAO;
import dao.UserDAO;

/**
 * Wires everything together: DAOs -> controllers. The views only ever receive an
 * AppContext and talk to the controllers it exposes.
 */
public class AppContext {

    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;

    private final Session session = new Session();
    private final AuthController auth;
    private final CustomerController customers;
    private final AccountController accounts;
    private final TransactionController transactions;

    private AppContext(CustomerDAO customerDAO, AccountDAO accountDAO,
                       TransactionDAO transactionDAO, UserDAO userDAO) {
        this.customerDAO = customerDAO;
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.userDAO = userDAO;
        this.auth = new AuthController(userDAO, session);
        this.customers = new CustomerController(customerDAO, accountDAO, userDAO, session);
        this.accounts = new AccountController(accountDAO, customerDAO, session);
        this.transactions = new TransactionController(accountDAO, transactionDAO, session);
    }

    /** The real application: SQLite through JDBC. Creates the tables if needed. */
    public static AppContext forJdbc(String jdbcUrl) {
        DatabaseManager db = new DatabaseManager(jdbcUrl);
        db.initSchema();
        CustomerDAO customerDAO = new JdbcCustomerDAO(db);
        AccountDAO accountDAO = new JdbcAccountDAO(db, customerDAO);
        return new AppContext(customerDAO, accountDAO, new JdbcTransactionDAO(db), new JdbcUserDAO(db));
    }

    /** For tests that must run without a database driver. */
    public static AppContext forMemory() {
        InMemoryStore store = new InMemoryStore();
        return new AppContext(store.customers, store.accounts, store.transactions, store.users);
    }

    public AuthController auth() {
        return auth;
    }

    public CustomerController customers() {
        return customers;
    }

    public AccountController accounts() {
        return accounts;
    }

    public TransactionController transactions() {
        return transactions;
    }

    public Session session() {
        return session;
    }

    // Package-private: used only to provision the initial teller account.
    UserDAO userDAO() {
        return userDAO;
    }
}
