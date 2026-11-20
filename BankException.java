package model;

/**
 * Checked exception used for every business-rule violation in SM BANKING
 * (insufficient funds, invalid input, access denied, etc.). The message is
 * always safe to show directly to the user.
 */
public class BankException extends Exception {

    private static final long serialVersionUID = 1L;

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
