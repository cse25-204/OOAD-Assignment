package model;

import util.MoneyUtil;

import java.time.LocalDate;
/**
 * Investment account: earns 5% interest per month, deposits and withdrawals allowed,
 * and can only be opened with an initial deposit of at least BWP 500.00.
 */
public class InvestmentAccount extends InterestBearingAccount {

    public static final double MONTHLY_RATE = 0.05; // 5%
    public static final double MINIMUM_OPENING_DEPOSIT = 500.00;

    /** Rebuilds an existing account (e.g. loaded from the database). */
    public InvestmentAccount(String accountNumber, Customer owner, String branch,
                             double balance, LocalDate dateOpened) {
        super(accountNumber, owner, branch, balance, dateOpened);
    }

    /** Opens a brand new investment account (enforces the BWP 500 minimum). */
    public static InvestmentAccount open(String accountNumber, Customer owner, String branch,
                                         double openingDeposit) throws BankException {
        if (openingDeposit < MINIMUM_OPENING_DEPOSIT) {
            throw new BankException("An Investment account needs an opening deposit of at least "
                    + MoneyUtil.format(MINIMUM_OPENING_DEPOSIT) + ".");
        }
        return new InvestmentAccount(accountNumber, owner, branch, openingDeposit, LocalDate.now());
    }

    @Override
    public double getMonthlyInterestRate() {
        return MONTHLY_RATE;
    }

    @Override
    public AccountType getType() {
        return AccountType.INVESTMENT;
    }

    @Override
    public boolean allowsWithdrawals() {
        return true;
    }

    @Override
    public Transaction withdraw(double amount) throws BankException {
        return debit(amount, "Withdrawal");
    }
}
