package model;

import util.PasswordUtil;

/** A login. Tellers have no customerId; customers are linked to their Customer record. */
public class User {

    private final String username;
    private final String passwordHash;
    private final String salt;
    private final Role role;
    private final String customerId;

    /** Used when loading a stored user. */
    public User(String username, String passwordHash, String salt, Role role, String customerId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.customerId = customerId;
    }

    /** Creates a new user, hashing the raw password with a fresh salt. */
    public static User create(String username, String rawPassword, Role role, String customerId) {
        String salt = PasswordUtil.newSalt();
        return new User(username, PasswordUtil.hash(rawPassword, salt), salt, role, customerId);
    }

    public boolean passwordMatches(String rawPassword) {
        return PasswordUtil.matches(rawPassword, salt, passwordHash);
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public Role getRole() {
        return role;
    }

    /** Null for tellers. */
    public String getCustomerId() {
        return customerId;
    }

    public boolean isTeller() {
        return role == Role.TELLER;
    }
}
