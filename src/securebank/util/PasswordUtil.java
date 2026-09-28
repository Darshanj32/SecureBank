package securebank.util;

import java.io.Console;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * Utility class for password hashing and masked console input.
 */
public class PasswordUtil {

    /**
     * Hashes a plain-text password using SHA-256 and returns a hex string.
     */
    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    public static boolean verify(String plainText, String hashed) {
        return hash(plainText).equals(hashed);
    }

    /**
     * Reads a password from the console with masking (****) where supported.
     * Falls back to plain Scanner input (visible) when running in an IDE
     * or any environment where System.console() is unavailable.
     */
    public static String readPassword(Scanner fallbackScanner, String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pwdChars = console.readPassword(prompt);
            return pwdChars == null ? "" : new String(pwdChars);
        } else {
            // IDE / redirected-input fallback: no native masking available.
            System.out.print(prompt + " (visible - run from a terminal for masking): ");
            return fallbackScanner.nextLine();
        }
    }
}
