package util;

import model.BankException;

import java.util.regex.Pattern;
/** Input validation shared by all controllers. Every method returns the cleaned value. */
public final class Validator {

    private static final Pattern NAME = Pattern.compile("[\\p{L} .'-]{2,60}");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ]{7,15}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");

    private Validator() {
    }

    public static String requireNotBlank(String value, String field) throws BankException {
        if (value == null || value.trim().isEmpty()) {
            throw new BankException(field + " is required.");
        }
        return value.trim();
    }

    public static String requireName(String value, String field) throws BankException {
        String v = requireNotBlank(value, field);
        if (!NAME.matcher(v).matches()) {
            throw new BankException(field + " may only contain letters, spaces, '.', '-' and apostrophes.");
        }
        return v;
    }

    public static String requireEmail(String value) throws BankException {
        String v = requireNotBlank(value, "Email");
        if (!EMAIL.matcher(v).matches()) {
            throw new BankException("Email address is not valid.");
        }
        return v;
    }

    public static String requirePhone(String value) throws BankException {
        String v = requireNotBlank(value, "Phone number");
        if (!PHONE.matcher(v).matches()) {
            throw new BankException("Phone number must be 7-15 digits (a leading + is allowed).");
        }
        return v;
    }

    public static String requireUsername(String value) throws BankException {
        String v = requireNotBlank(value, "Username");
        if (!USERNAME.matcher(v).matches()) {
            throw new BankException("Username must be 3-30 characters: letters, digits, '.', '_' or '-'.");
        }
        return v;
    }

    public static void requirePassword(String password, String confirm) throws BankException {
        if (password == null || password.length() < 6) {
            throw new BankException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirm)) {
            throw new BankException("Password and confirmation do not match.");
        }
    }

    /** Parses text such as "1,250.50" or "BWP 500" into a positive-or-zero amount. */
    public static double parseAmount(String text, String field) throws BankException {
        if (text == null || text.trim().isEmpty()) {
            throw new BankException(field + " is required.");
        }
        String cleaned = text.trim().toUpperCase().replace("BWP", "").replace(",", "").replace(" ", "");
        try {
            double value = Double.parseDouble(cleaned);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException();
            }
            return MoneyUtil.round(value);
        } catch (NumberFormatException e) {
            throw new BankException(field + " must be a valid number.");
        }
    }
}
