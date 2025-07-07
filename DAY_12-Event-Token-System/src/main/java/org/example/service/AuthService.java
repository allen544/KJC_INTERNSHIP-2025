package org.example.service;

import org.bson.Document;
import org.example.models.User;
import org.example.security.PasswordUtil;
import org.example.repository.UserRepository;

import java.util.Date;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public String getUserRole(String email) {
        return userRepository.findRoleByEmail(email);
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return null;

        // Return sanitized user without password
        return new User(
                user.getName(),
                user.getEmail(),
                null, // Password not exposed
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public boolean register(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }

        String generatedPassword = PasswordUtil.generateRandomPassword();
        String hashedPassword = PasswordUtil.hashPassword(generatedPassword);
        userRepository.save(name, email, hashedPassword, "user");
        EmailService.sendPasswordEmail(email, generatedPassword);
        return true;
    }

    public boolean verifyCredentials(String email, String password) {
        System.out.println("=== DEBUG START ===");
        System.out.println("Input email: " + email);
        System.out.println("Input password: " + password);

        String storedHash = userRepository.findPasswordByEmail(email);
        System.out.println("Stored hash: " + storedHash);

        boolean isValid = PasswordUtil.verifyPassword(password, storedHash);
        System.out.println("Verification result: " + isValid);
        System.out.println("=== DEBUG END ===");

        return isValid;
    }

    public UserTokenData getUserTokenData(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return null;

        return new UserTokenData(
                user.getEmail(),
                user.getRole(),
                user.getName(),
                user.getCreatedAt()
        );
    }

    public static class UserTokenData {
        private final String email;
        private final String role;
        private final String name;
        private final Date createdAt;

        public UserTokenData(String email, String role, String name, Date createdAt) {
            this.email = email;
            this.role = role;
            this.name = name;
            this.createdAt = createdAt;
        }

        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getName() { return name; }
        public Date getCreatedAt() { return createdAt; }
    }
}