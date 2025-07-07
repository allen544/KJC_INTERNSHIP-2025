package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.Booking;
import org.example.security.JwtUtils;
import org.example.service.BookingService;
import org.example.utils.LocalDateTimeAdapter;
import java.time.LocalDateTime;
import java.util.List;
import static spark.Spark.*;

public class BookingController {
    private final BookingService bookingService = new BookingService();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public BookingController() {
        setupRoutes();
    }

    private void setupRoutes() {
        // Updated authentication check with debug
        before("/book/*", (req, res) -> {
            debugTokenValidation(req);
            String token = getTokenFromRequest(req);
            if (token == null || !JwtUtils.validateToken(token)) {
                halt(401, "Unauthorized - Invalid or missing token");
            }
        });

        before("/bookings", (req, res) -> {
            debugTokenValidation(req);
            String token = getTokenFromRequest(req);
            if (token == null || !JwtUtils.validateToken(token)) {
                halt(401, "Unauthorized - Invalid or missing token");
            }
        });

        post("/book/:eventId", (req, res) -> {
            res.type("application/json");
            try {
                String token = getTokenFromRequest(req);
                String email = JwtUtils.extractEmail(token);

                System.out.println("Booking attempt - Email: " + email + ", EventId: " + req.params(":eventId"));

                boolean success = bookingService.bookTicket(email, req.params(":eventId"));

                if (success) {
                    res.status(201);
                    return gson.toJson(new SimpleResponse("Booking created successfully"));
                } else {
                    res.status(400);
                    return gson.toJson(new SimpleResponse("Booking failed - Event not available or already booked"));
                }
            } catch (Exception e) {
                System.err.println("Booking error: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new SimpleResponse("Internal server error: " + e.getMessage()));
            }
        });

        get("/bookings", (req, res) -> {
            res.type("application/json");
            try {
                String token = getTokenFromRequest(req);
                String email = JwtUtils.extractEmail(token);

                System.out.println("Fetching bookings for email: " + email);

                List<Booking> bookings = bookingService.getBookingsByEmail(email);
                return gson.toJson(bookings);
            } catch (Exception e) {
                System.err.println("Error fetching bookings: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new SimpleResponse("Error fetching bookings: " + e.getMessage()));
            }
        });
    }

    // Helper method to extract token from request
    private String getTokenFromRequest(spark.Request req) {
        // First try Authorization header
        String authHeader = req.headers("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Then try cookie
        String cookieToken = req.cookie("jwt");
        if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }

        return null;
    }

    // Debug method to help troubleshoot token issues
    private void debugTokenValidation(spark.Request req) {
        String token = getTokenFromRequest(req);
        System.out.println("=== DEBUG TOKEN VALIDATION ===");
        System.out.println("Token extracted: " + (token != null ? "YES" : "NO"));
        System.out.println("Token length: " + (token != null ? token.length() : "N/A"));
        System.out.println("Token (first 50 chars): " + (token != null ? token.substring(0, Math.min(50, token.length())) : "N/A"));

        if (token != null) {
            try {
                boolean isValid = JwtUtils.validateToken(token);
                System.out.println("Token validation result: " + isValid);

                if (isValid) {
                    String email = JwtUtils.extractEmail(token);
                    System.out.println("Extracted email: " + email);
                } else {
                    System.out.println("Token validation failed - token is invalid");
                }
            } catch (Exception e) {
                System.out.println("Token validation error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No token found in request");
            System.out.println("Authorization header: " + req.headers("Authorization"));
            System.out.println("Cookie header: " + req.headers("Cookie"));
        }
        System.out.println("=== END DEBUG ===");
    }

    // Simple response wrapper
    private static class SimpleResponse {
        String message;

        SimpleResponse(String message) {
            this.message = message;
        }
    }
}