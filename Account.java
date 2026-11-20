package model;

import util.MoneyUtil;

import java.time.LocalDate;
/**
 * Abstract parent of every account type (abstraction + encapsulation).
 *
 * An account cannot exist without a Customer who holds it. The balance can only
 * be changed through deposit/withdraw/interest so the rules cannot be bypassed.
 * deposit(...) is overloaded; withdraw(...) is overridden by each subclass.
 */
public abstract class Account implements Transactable {

    private final String accountNumber;
    private final Customer owner;
    private String branch;
    private double balance;
    private final LocalDate dateOpened;

    protected Account(String accountNumber, Customer owner, String branch,
                      double balance, LocalDate dateOpened) {
        if (owner == null) {
            throw new IllegalArgumentException("An account cannot exist without a customer who holds it.");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.branch = branch;
        this.balance = MoneyUtil.round(balance);
        this.dateOpened = dateOpened;
    }

    // ---------- getters (state is private: encapsulation) ----------

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getOwner() {
        return owner;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getBalance() {
        return balance;
    }

    public LocalDate getDateOpened() {
        return dateOpened;
    }

    // ---------- behaviour that differs per account type ----------

    public abstract AccountType getType();

    /** Whether this kind of account lets the customer take money out. */
    public abstract boolean allowsWithdrawals();

    @Override
    public abstract Transaction withdraw(double amount) throws BankException;

    // ---------- shared behaviour ----------

    /** Deposit with the default description (overload 1). */
    @Override
    public Transaction deposit(double amount) throws BankException {
        return deposit(amount, "Deposit");
    }

    /** Deposit with a custom description (overload 2). */
    public Transaction deposit(double amount, String description) throws BankException {
        return credit(amount, TransactionType.DEPOSIT, description);
    }

    /** Adds money and records it. Used for deposits and interest. */
    protected Transaction credit(double amount, TransactionType type, String description)
            throws BankException {
        double amt = checkAmount(amount);
        balance = MoneyUtil.round(balance + amt);
        return new Transaction(accountNumber, type, amt, balance, description);
    }

    /** Removes money (no overdraft) and records it. Subclasses call this once their own rules pass. */
    protected Transaction debit(double amount, String description) throws BankException {
        double amt = checkAmount(amount);
        if (amt > balance) {
            throw new BankException("Insufficient funds. Available balance is "
                    + MoneyUtil.format(balance) + ".");
        }
        balance = MoneyUtil.round(balance - amt);
        return new Transaction(accountNumber, TransactionType.WITHDRAWAL, amt, balance, description);
    }

    private double checkAmount(double amount) throws BankException {
        double amt = MoneyUtil.round(amount);
        if (Double.isNaN(amount) || amt <= 0) {
            throw new BankException("Amount must be greater than zero.");
        }
        return amt;
    }

    @Override
    public String toString() {
        return accountNumber + " | " + getType().getLabel() + " | " + owner.getDisplayName()
                + " | " + MoneyUtil.format(balance);
    }
}
