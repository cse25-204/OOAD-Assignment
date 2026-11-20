package controller;

import model.BankException;
import model.User;

/** Remembers who is logged in and answers "is this person allowed to do that?". */
public class Session {

    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User requireLogin() throws BankException {
        if (currentUser == null) {
            throw new BankException("Please log in first.");
        }
        return currentUser;
    }

    public void requireTeller() throws BankException {
        if (!requireLogin().isTeller()) {
            throw new BankException("Only bank staff may do that.");
        }
    }

    /** Tellers may touch any customer; a customer only themselves. */
    public void requireAccessToCustomer(String customerId) throws BankException {
        User user = requireLogin();
        if (!user.isTeller() && !customerId.equals(user.getCustomerId())) {
            throw new BankException("You do not have access to that customer's information.");
        }
    }
}
