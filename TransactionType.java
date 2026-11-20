package model;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    INTEREST("Interest");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
