package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Passwords are never stored as plain text. We store a random "salt" and the
 * SHA-256 hash of (salt + password). To check a login we hash again and compare.
 */
public class PasswordUtil {

    /** Makes a new random salt. */
    public static String newSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /** Hashes the salt together with the password. */
    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** True if the typed password gives the same hash that was stored. */
    public static boolean matches(String password, String salt, String storedHash) {
        return hash(password, salt).equals(storedHash);
    }
}
