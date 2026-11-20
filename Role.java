package model;

/** Who is logged in: bank staff (teller/administrator) or a customer. */
public enum Role {
    TELLER("Bank Teller"),
    CUSTOMER("Customer");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
