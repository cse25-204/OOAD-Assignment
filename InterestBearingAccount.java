package model;

import util.MoneyUtil;

import java.time.LocalDate;
/**
 * Shared behaviour of accounts that earn interest (Savings and Investment).
 * Each subclass only supplies its own monthly rate.
 */
public abstract class InterestBearingAccount extends Account implements InterestBearing {

    protected InterestBearingAccount(String accountNumber, Customer owner, String branch,
                                     double balance, LocalDate dateOpened) {
        super(accountNumber, owner, branch, balance, dateOpened);
    }

    @Override
    public double calculateMonthlyInterest() {
        return MoneyUtil.round(getBalance() * getMonthlyInterestRate());
    }

    @Override
    public Transaction payInterest() throws BankException {
        double interest = calculateMonthlyInterest();
        if (interest <= 0) {
            throw new BankException("No interest is due on account " + getAccountNumber() + ".");
        }
        return credit(interest, TransactionType.INTEREST,
                "Monthly interest (" + describeRate() + ")");
    }
}
