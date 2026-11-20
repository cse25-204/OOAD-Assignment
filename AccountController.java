package controller;

import dao.AccountDAO;
import dao.CustomerDAO;
import model.Account;
import model.AccountType;
import model.BankException;
import model.ChequeAccount;
import model.Customer;
import model.InvestmentAccount;
import model.SavingsAccount;
import model.Transaction;
import model.TransactionType;
import model.User;
import util.Validator;

import java.util.List;
/** Opening accounts and looking them up. Applies the rules of each account type via the model. */
public class AccountController {

    private final AccountDAO accounts;
    private final CustomerDAO customers;
    private final Session session;

    public AccountController(AccountDAO accounts, CustomerDAO customers, Session session) {
        this.accounts = accounts;
        this.customers = customers;
        this.session = session;
    }

    /**
     * Opens an account. A customer can only open accounts for themselves; a teller
     * names the customer. A customer may hold several accounts (even of the same type).
     */
    public Account openAccount(AccountType type, String customerId, String branch,
                               String openingDepositText, String employerName,
                               String employerAddress) throws BankException {
        User user = session.requireLogin();
        String ownerId = user.isTeller() ? customerId : user.getCustomerId();
        if (type == null) {
            throw new BankException("Choose an account type.");
        }
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new BankException("Choose the customer who will hold the account.");
        }
        Customer owner = customers.findById(ownerId)
                .orElseThrow(() -> new BankException("Customer " + ownerId + " not found."));
        String branchName = Validator.requireNotBlank(branch, "Branch");

        double opening = 0;
        if (openingDepositText != null && !openingDepositText.trim().isEmpty()) {
            opening = Validator.parseAmount(openingDepositText, "Opening deposit");
        }

        String number = type.getPrefix() + String.format("%09d", accounts.maxSequence() + 1);
        Account account = switch (type) {
            case SAVINGS -> SavingsAccount.open(number, owner, branchName, opening);
            case INVESTMENT -> InvestmentAccount.open(number, owner, branchName, opening);
            case CHEQUE -> ChequeAccount.open(number, owner, branchName, opening, employerName, employerAddress);
        };

        Transaction openingTx = opening > 0
                ? new Transaction(number, TransactionType.DEPOSIT, opening, opening, "Opening deposit")
                : null;
        accounts.save(account, openingTx);
        return account;
    }

    /** Teller: every account in the bank. Customer: only their own. */
    public List<Account> getVisibleAccounts() throws BankException {
        User user = session.requireLogin();
        return user.isTeller() ? accounts.findAll() : accounts.findByCustomer(user.getCustomerId());
    }

    public List<Account> getAccountsForCustomer(String customerId) throws BankException {
        session.requireAccessToCustomer(customerId);
        return accounts.findByCustomer(customerId);
    }

    public Account getAccount(String accountNumber) throws BankException {
        Account account = accounts.findByNumber(accountNumber)
                .orElseThrow(() -> new BankException("Account " + accountNumber + " not found."));
        session.requireAccessToCustomer(account.getOwner().getCustomerId());
        return account;
    }
}
