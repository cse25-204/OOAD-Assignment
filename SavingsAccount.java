package model;

import java.time.LocalDate;

/**
 * Savings account: deposits only, earns 0.05% interest per month,
 * withdrawals are NOT allowed.
 */
public class SavingsAccount extends InterestBearingAccount {

    public static final double MONTHLY_RATE = 0.0005; // 0.05%

    /** Rebuilds an existing account (e.g. loaded from the database). */
    public SavingsAccount(String accountNumber, Customer owner, String branch,
                          double balance, LocalDate dateOpened) {
        super(accountNumber, owner, branch, balance, dateOpened);
    }

    /** Opens a brand new savings account (enforces the opening rules). */
    public static SavingsAccount open(String accountNumber, Customer owner, String branch,
                                      double openingDeposit) throws BankException {
        if (openingDeposit < 0) {
            throw new BankException("Opening deposit cannot be negative.");
        }
        return new SavingsAccount(accountNumber, owner, branch, openingDeposit, LocalDate.now());
    }

    @Override
    public double getMonthlyInterestRate() {
        return MONTHLY_RATE;
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVINGS;
    }

    @Override
    public boolean allowsWithdrawals() {
        return false;
    }

    @Override
    public Transaction withdraw(double amount) throws BankException {
        throw new BankException("Savings accounts do not allow withdrawals.");
    }
}
