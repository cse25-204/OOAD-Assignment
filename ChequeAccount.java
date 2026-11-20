package model;

import java.time.LocalDate;

/**
 * Cheque account: used for salaries, deposits and withdrawals of any amount (no
 * overdraft), no interest. Can only be opened for a working person, who must
 * give the name and address of their employer.
 */
public class ChequeAccount extends Account {

    private final String employerName;
    private final String employerAddress;

    /** Rebuilds an existing account (e.g. loaded from the database). */
    public ChequeAccount(String accountNumber, Customer owner, String branch, double balance,
                         LocalDate dateOpened, String employerName, String employerAddress) {
        super(accountNumber, owner, branch, balance, dateOpened);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    /** Opens a brand new cheque account (enforces the "must be working" rule). */
    public static ChequeAccount open(String accountNumber, Customer owner, String branch,
                                     double openingDeposit, String employerName,
                                     String employerAddress) throws BankException {
        if (!(owner instanceof IndividualCustomer)) {
            throw new BankException("A Cheque account can only be opened for a working person, "
                    + "not for a company.");
        }
        if (employerName == null || employerName.trim().isEmpty()
                || employerAddress == null || employerAddress.trim().isEmpty()) {
            throw new BankException("Employer name and employer address are required for a Cheque account.");
        }
        if (openingDeposit < 0) {
            throw new BankException("Opening deposit cannot be negative.");
        }
        return new ChequeAccount(accountNumber, owner, branch, openingDeposit, LocalDate.now(),
                employerName.trim(), employerAddress.trim());
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }

    @Override
    public AccountType getType() {
        return AccountType.CHEQUE;
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
