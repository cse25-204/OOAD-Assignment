package dao;

import model.Transaction;

import java.time.YearMonth;
import java.util.List;
/** Operations for the (append-only) transaction ledger. */
public interface TransactionDAO {

    void save(Transaction transaction);

    /** Oldest first. */
    List<Transaction> findByAccount(String accountNumber);

    /** True if an INTEREST transaction already exists for that account in that month. */
    boolean interestPaidForMonth(String accountNumber, YearMonth month);

    int count();
}
