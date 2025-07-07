package org.example;

import static spark.Spark.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.example.controller.AdminController;
import org.example.controller.AuthController;
import org.example.controller.BookingController;
import org.example.controller.EventController;
import org.example.repository.BookingRepository;
import org.example.repository.EventRepository;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        port(4567); // http://localhost:4567/

        // Configure CORS
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            res.header("Access-Control-Allow-Credentials", "true");
        });

        // Initialize services and controllers
        AuthService authService = new AuthService();

        // Initialize controllers (each only once)
        new AuthController(new AuthService());
        new EventController();
        new BookingController();
        // Static HTML endpoints
        String[] pages = {
                "/register", "/login",
                "/login.html", "/register.html",
                "/user.html", "/admin.html",
                "/events.html", "/bookings.html",
                "/admin","/reset_password.html"
        };

        for (String page : pages) {
            get(page, (req, res) -> {
                try {
                    String filePath = page.endsWith(".html") ? page : page + ".html";
                    return new String(Files.readAllBytes(
                            Paths.get("src/main/resources/templates/" + filePath)
                    ));
                } catch (Exception e) {
                    halt(404, "Page not found");
                    return null;
                }
            });
        }

        // Add this with your other controllers
        new AdminController();

        // In your Main.java
        UserRepository userRepo = new UserRepository();
        EventRepository eventRepo = new EventRepository();
        BookingRepository bookingRepo = new BookingRepository();

        new AdminController(userRepo, eventRepo, bookingRepo);  // Updated initialization


        // Test endpoint
        get("/test", (req, res) -> {
            return "Server is running! Current time: " + java.time.LocalDateTime.now();
        });

        // Error handling
        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body("Internal Server Error: " + e.getMessage());
        });

        notFound((req, res) -> {
            res.type("text/html");
            return "<h1>404 - Page Not Found</h1>";
        });

    }
}