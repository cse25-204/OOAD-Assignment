package controller;

import model.Role;
import model.User;

/** Provisions a teller login when the database is first created. */
public final class DefaultTellerSetup {

    public static final String DEFAULT_TELLER = "admin";
    public static final String DEFAULT_TELLER_PASSWORD = "admin123";

    private DefaultTellerSetup() {
    }

    /** Makes sure at least one teller login exists, so someone can always sign in. */
    public static void ensureDefaultTeller(AppContext ctx) {
        if (!ctx.userDAO().existsByUsername(DEFAULT_TELLER)) {
            ctx.userDAO().save(User.create(DEFAULT_TELLER, DEFAULT_TELLER_PASSWORD, Role.TELLER, null));
        }
    }
}
