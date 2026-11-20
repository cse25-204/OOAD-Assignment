package model;

/** Constants describing the bank itself. */
public final class Bank {

    public static final String NAME = "SM BANKING";
    public static final String CURRENCY = "BWP";

    /** Branches a customer can choose when opening an account. */
    public static final String[] BRANCHES = {
        "Gaborone Main", "Francistown", "Maun", "Kasane", "Palapye", "Lobatse", "Selebi-Phikwe"
    };

    private Bank() {
        // constants only
    }
}
