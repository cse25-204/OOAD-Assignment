package model;

/** Implemented by accounts that earn monthly interest. */
public interface InterestBearing {

    /** Monthly rate as a fraction, e.g. 0.05 means 5% per month. */
    double getMonthlyInterestRate();

    /** Interest that would be paid this month on the current balance. */
    double calculateMonthlyInterest();

    /** Credits this month's interest and returns the transaction record. */
    Transaction payInterest() throws BankException;

    /** Human readable rate, e.g. "0.05% per month". */
    default String describeRate() {
        return String.format("%.2f%% per month", getMonthlyInterestRate() * 100);
    }
}
