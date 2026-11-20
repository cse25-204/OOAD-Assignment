package model;

import java.time.LocalDateTime;

/** One line of an account's history. Immutable once created (id is set when stored). */
public class Transaction {

    private long id;
    private final String accountNumber;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String description;

    /** Creates a brand new transaction stamped with the current time. */
    public Transaction(String accountNumber, TransactionType type, double amount,
                       double balanceAfter, String description) {
        this(0, accountNumber, type, amount, balanceAfter, LocalDateTime.now().withNano(0), description);
    }

    /** Used when rebuilding a transaction that was loaded from the database. */
    public Transaction(long id, String accountNumber, TransactionType type, double amount,
                       double balanceAfter, LocalDateTime timestamp, String description) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return timestamp + " " + type + " " + amount + " (balance " + balanceAfter + ")";
    }
}
