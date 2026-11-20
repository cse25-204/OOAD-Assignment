package dao;

import model.Account;
import model.Transaction;

import java.util.List;
import java.util.Optional;
/** CRUD operations for accounts. */
public interface AccountDAO {

    /** Create: stores the account and (in the same database transaction) its opening deposit, if any. */
    void save(Account account, Transaction openingTransaction);

    Optional<Account> findByNumber(String accountNumber);

    List<Account> findByCustomer(String customerId);

    List<Account> findAll();

    /**
     * Update: stores the new balance and (in the same database transaction) the
     * transaction that caused it, so the two can never get out of step.
     */
    void update(Account account, Transaction transaction);

    void delete(String accountNumber);

    int count();

    /** Highest numeric part used so far in account numbers (0 if none). */
    long maxSequence();
}
