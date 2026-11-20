package model;

/** The three kinds of account SM BANKING offers. */
public enum AccountType {
    SAVINGS("Savings Account", "1"),
    INVESTMENT("Investment Account", "2"),
    CHEQUE("Cheque Account", "3");

    private final String label;
    private final String prefix;

    AccountType(String label, String prefix) {
        this.label = label;
        this.prefix = prefix;
    }

    public String getLabel() {
        return label;
    }

    /** First digit of every account number of this type. */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return label;
    }
}
