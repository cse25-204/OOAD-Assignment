package model;

/**
 * Anything money can be paid into or taken out of.
 * (Interface: Account implements it, subclasses decide how withdrawals behave.)
 */
public interface Transactable {

    /** Pays money in and returns the transaction record (to be stored). */
    Transaction deposit(double amount) throws BankException;

    /** Takes money out and returns the transaction record (to be stored). */
    Transaction withdraw(double amount) throws BankException;
}
