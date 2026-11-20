package util;

import model.Bank;

/** Money helpers: every amount in the system is kept to 2 decimal places. */
public final class MoneyUtil {

    private MoneyUtil() {
    }

    /** Rounds to the nearest thebe (2 decimal places). */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Formats as e.g. "BWP 1,250.50". */
    public static String format(double value) {
        return String.format("%s %,.2f", Bank.CURRENCY, value);
    }
}
