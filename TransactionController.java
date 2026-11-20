package controller;

import dao.AccountDAO;
import dao.TransactionDAO;
import model.Account;
import model.BankException;
import model.InterestBearing;
import model.Transaction;
import util.MoneyUtil;
import util.Validator;

import java.time.YearMonth;
import java.util.List;
/** Deposits, withdrawals, history and the monthly interest run. */
public class TransactionController {

    /** What a monthly interest run did. */
    public static class InterestRunSummary {
        private final int accountsPaid;
        private final int accountsSkipped;
        private final double totalInterest;

        InterestRunSummary(int accountsPaid, int accountsSkipped, double totalInterest) {
            this.accountsPaid = accountsPaid;
            this.accountsSkipped = accountsSkipped;
            this.totalInterest = totalInterest;
        }

        public int getAccountsPaid() {
            return accountsPaid;
        }

        public int getAccountsSkipped() {
            return accountsSkipped;
        }

        public double getTotalInterest() {
            return totalInterest;
        }

        @Override
        public String toString() {
            return "Interest paid to " + accountsPaid + " account(s), total "
                    + MoneyUtil.format(totalInterest) + ". Skipped " + accountsSkipped
                    + " (already paid this month or nothing due).";
        }
    }

    private final AccountDAO accounts;
    private final TransactionDAO transactions;
    private final Session session;

    public TransactionController(AccountDAO accounts, TransactionDAO transactions, Session session) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.session = session;
    }

    /** Deposits into any account the logged-in user may access. Returns the updated account. */
    public Account deposit(String accountNumber, String amountText) throws BankException {
        Account account = load(accountNumber);
        double amount = Validator.parseAmount(amountText, "Amount");
        Transaction tx = account.deposit(amount);
        accounts.update(account, tx);
        return account;
    }

    /** Withdraws; the account type decides whether this is allowed (Savings: never). */
    public Account withdraw(String accountNumber, String amountText) throws BankException {
        Account account = load(accountNumber);
        double amount = Validator.parseAmount(amountText, "Amount");
        Transaction tx = account.withdraw(amount);
        accounts.update(account, tx);
        return account;
    }

    public List<Transaction> getHistory(String accountNumber) throws BankException {
        load(accountNumber);
        return transactions.findByAccount(accountNumber);
    }

    /**
     * Teller only. Pays this month's interest (5% Investment, 0.05% Savings) to every
     * interest-bearing account that has not been paid yet this month.
     */
    public InterestRunSummary payMonthlyInterest() throws BankException {
        session.requireTeller();
        YearMonth thisMonth = YearMonth.now();
        int paid = 0;
        int skipped = 0;
        double total = 0;
        for (Account account : accounts.findAll()) {
            if (!(account instanceof InterestBearing earner)
                    || transactions.interestPaidForMonth(account.getAccountNumber(), thisMonth)) {
                if (account instanceof InterestBearing) {
                    skipped++;
                }
                continue;
            }
            try {
                Transaction tx = earner.payInterest();
                accounts.update(account, tx);
                paid++;
                total += tx.getAmount();
            } catch (BankException nothingDue) {
                skipped++;
            }
        }
        return new InterestRunSummary(paid, skipped, MoneyUtil.round(total));
    }

    private Account load(String accountNumber) throws BankException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BankException("Choose an account first.");
        }
        Account account = accounts.findByNumber(accountNumber)
                .orElseThrow(() -> new BankException("Account " + accountNumber + " not found."));
        session.requireAccessToCustomer(account.getOwner().getCustomerId());
        return account;
    }
}
