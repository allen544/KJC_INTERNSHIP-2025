package org.example.security;

import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final SecureRandom random = new SecureRandom();
    private static final int DEFAULT_PASSWORD_LENGTH = 12;

    // Generate random password
    public static String generateRandomPassword() {
        byte[] randomBytes = new byte[DEFAULT_PASSWORD_LENGTH];
        random.nextBytes(randomBytes);
        return Base64.getEncoder()
                .encodeToString(randomBytes)
                .substring(0, DEFAULT_PASSWORD_LENGTH)
                .replace("/", "x").replace("+", "y"); // Make URL-safe
    }

    // Hash password using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Verify password
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        try {
            return BCrypt.checkpw(inputPassword, storedHash);
        } catch (Exception e) {
            System.err.println("Password verification error: " + e.getMessage());
            return false;
        }
    }
}