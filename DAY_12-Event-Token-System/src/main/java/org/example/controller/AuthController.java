package org.example.controller;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.example.models.User;
import org.example.repository.UserRepository;
import org.example.security.JwtUtils;
import org.example.security.PasswordUtil;
import org.example.service.AuthService;
import org.example.service.EmailService;

import java.time.Duration;
import java.util.Random;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Gson gson;

    // Simple in-memory cache for reset codes (replace with Redis in production)
    private final Map<String, CodeData> resetCodeCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Inner class to store code with expiration
    private static class CodeData {
        String code;
        long expiryTime;

        CodeData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
        this.userRepository = new UserRepository();
        this.emailService = new EmailService();
        this.gson = new Gson();
        routes();

        // Clean up expired codes every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredCodes, 5, 5, TimeUnit.MINUTES);
    }

    private void cleanupExpiredCodes() {
        resetCodeCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // Helper method to parse JSON from request body
    private JsonObject getJsonFromRequest(spark.Request req) {
        try {
            return JsonParser.parseString(req.body()).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return new JsonObject();
        }
    }

    public void routes() {
        // Set content type for API responses
        before("/api/*", (req, res) -> {
            res.type("application/json");
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        // Handle preflight requests
        options("/api/*", (req, res) -> {
            return "OK";
        });

        // Registration
        post("/register", (req, res) -> {
            String name = req.queryParams("name");
            String email = req.queryParams("email");

            boolean success = authService.register(name, email);
            if (success) {
                res.redirect("/login.html?success=true");
            } else {
                res.redirect("/register.html?error=duplicate");
            }
            return null;
        });

        // Login
        post("/login", (req, res) -> {
            String email = req.queryParams("email");
            String password = req.queryParams("password");

            if (authService.verifyCredentials(email, password)) {
                User user = authService.getUserByEmail(email);
                String token = JwtUtils.generateToken(
                        email,
                        user.getRole(),
                        user.getName(),
                        user.getCreatedAt()
                );
                res.cookie("jwt", token);
                res.redirect("admin".equals(user.getRole()) ? "/admin.html" : "/user.html");
            } else {
                res.redirect("/login.html?error=invalid");
            }
            return null;
        });

        // Logout
        get("/logout", (req, res) -> {
            res.removeCookie("jwt");
            res.redirect("/login.html?logout=true");
            return null;
        });

        // Password Reset Endpoints
        path("/api/password", () -> {
            // 1. Send reset code
            post("/reset-code", (req, res) -> {
                try {
                    JsonObject json = getJsonFromRequest(req);
                    String email = json.get("email").getAsString();

                    // Validate email exists
                    if (!userRepository.existsByEmail(email)) {
                        res.status(404);
                        return gson.toJson(Map.of("error", "Email not registered"));
                    }

                    // Generate 6-digit code
                    String code = String.format("%06d", new Random().nextInt(999999));

                    // Store code with 10-minute expiry
                    long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes
                    resetCodeCache.put("pwreset:" + email, new CodeData(code, expiryTime));

                    // Send email
                    sendResetCodeEmail(email, code);

                    return gson.toJson(Map.of("message", "Verification code sent to your email"));

                } catch (Exception e) {
                    System.err.println("Error in reset-code: " + e.getMessage());
                    res.status(500);
                    return gson.toJson(Map.of("error", "Failed to send reset code"));
                }
            });

            // 2. Verify code
            post("/verify-code", (req, res) -> {
                try {
                    JsonObject json = getJsonFromRequest(req);
                    String email = json.get("email").getAsString();
                    String code = json.get("code").getAsString();

                    CodeData storedCodeData = resetCodeCache.get("pwreset:" + email);

                    if (storedCodeData == null || storedCodeData.isExpired()) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "Code expired or not found"));
                    }

                    if (!storedCodeData.code.equals(code)) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "Invalid verification code"));
                    }

                    // Generate one-time token for password update (valid for 15 minutes)
                    String token = JwtUtils.generatePasswordResetToken(email);

                    // Remove used code
                    resetCodeCache.remove("pwreset:" + email);

                    return gson.toJson(Map.of("token", token, "message", "Code verified successfully"));

                } catch (Exception e) {
                    System.err.println("Error in verify-code: " + e.getMessage());
                    res.status(500);
                    return gson.toJson(Map.of("error", "Failed to verify code"));
                }
            });

            // 3. Update password
            post("/update", (req, res) -> {
                try {
                    JsonObject json = getJsonFromRequest(req);
                    String email = json.get("email").getAsString();
                    String newPassword = json.get("newPassword").getAsString();
                    String token = json.get("token").getAsString();

                    // Validate token
                    if (!JwtUtils.validatePasswordResetToken(token, email)) {
                        res.status(401);
                        return gson.toJson(Map.of("error", "Invalid or expired security token"));
                    }

                    // Validate password strength
                    if (newPassword.length() < 8) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "Password must be at least 8 characters"));
                    }

                    // Update password
                    String hashedPassword = PasswordUtil.hashPassword(newPassword);
                    boolean updated = userRepository.updatePassword(email, hashedPassword);

                    if (!updated) {
                        res.status(500);
                        return gson.toJson(Map.of("error", "Failed to update password"));
                    }

                    return gson.toJson(Map.of("message", "Password updated successfully"));

                } catch (Exception e) {
                    System.err.println("Error in update password: " + e.getMessage());
                    res.status(500);
                    return gson.toJson(Map.of("error", "Failed to update password"));
                }
            });
        });
    }

    private void sendResetCodeEmail(String email, String code) {
        String subject = "Password Reset Code - Event Token System";
        String htmlContent = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<h2 style='color: #f39c12;'>Password Reset Request</h2>" +
                        "<p>You have requested to reset your password. Please use the verification code below:</p>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;'>" +
                        "<h1 style='color: #f39c12; font-size: 32px; margin: 0; letter-spacing: 5px;'>%s</h1>" +
                        "</div>" +
                        "<p><strong>This code will expire in 10 minutes.</strong></p>" +
                        "<p>If you did not request this password reset, please ignore this email.</p>" +
                        "<p>Best regards,<br>Event Token System Team</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                code
        );

        // Send email asynchronously
        EmailService.sendHtmlEmail(email, subject, htmlContent);
    }
}