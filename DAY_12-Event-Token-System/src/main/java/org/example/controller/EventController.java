package org.example.controller;

import com.google.gson.*;
import org.example.models.Event;
import org.example.repository.EventRepository;
import org.example.security.JwtUtils;
import org.example.utils.LocalDateTimeAdapter;

import java.time.LocalDateTime;
import java.util.List;

import static spark.Spark.*;

public class EventController {
    private final EventRepository eventRepo = new EventRepository();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public EventController() {
        System.out.println("Initializing EventController routes...");
        setupRoutes();
    }

    private void setupRoutes() {
        // CORS preflight handler
        options("/events", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            res.status(200);
            return "OK";
        });

        options("/events/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            res.status(200);
            return "OK";
        });

        // Admin access check for all event routes
        before("/events*", (req, res) -> {
            // Skip auth check for OPTIONS requests
            if ("OPTIONS".equals(req.requestMethod())) {
                return;
            }

            System.out.println("Checking admin access for events endpoint");
            if (!JwtUtils.isAdmin(req.cookie("jwt"))) {
                halt(403, "Admin access required");
            }
        });

        // Get all events
        get("/events", (req, res) -> {
            System.out.println("Fetching all events from database");
            try {
                List<Event> events = eventRepo.findAll();
                System.out.println("Found " + events.size() + " events");
                res.type("application/json");
                res.header("Access-Control-Allow-Origin", "*");
                return gson.toJson(events);
            } catch (Exception e) {
                System.err.println("Error fetching events: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Error fetching events: " + e.getMessage();
            }
        });

        // Get single event
        get("/events/:id", (req, res) -> {
            String id = req.params(":id");
            System.out.println("Fetching event: " + id);
            try {
                Event event = eventRepo.findById(id);
                if (event == null) {
                    halt(404, "Event not found");
                }
                res.type("application/json");
                res.header("Access-Control-Allow-Origin", "*");
                return gson.toJson(event);
            } catch (Exception e) {
                System.err.println("Error fetching event: " + e.getMessage());
                res.status(500);
                return "Error fetching event: " + e.getMessage();
            }
        });

        // Create event
        post("/events", (req, res) -> {
            try {
                Event event = gson.fromJson(req.body(), Event.class);
                System.out.println("Creating event: " + event.getName());
                String id = eventRepo.create(event);
                res.status(201);
                res.type("application/json");
                res.header("Access-Control-Allow-Origin", "*");
                return gson.toJson(id);
            } catch (Exception e) {
                System.err.println("Error creating event: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Error creating event: " + e.getMessage();
            }
        });

        // Update event
        put("/events/:id", (req, res) -> {
            String id = req.params(":id");
            System.out.println("Updating event: " + id);
            try {
                Event existing = eventRepo.findById(id);
                if (existing == null) {
                    halt(404, "Event not found");
                }

                Event updates = gson.fromJson(req.body(), Event.class);
                updates.setId(id); // Ensure the ID is set

                boolean success = eventRepo.update(updates);
                if (success) {
                    res.type("application/json");
                    res.header("Access-Control-Allow-Origin", "*");
                    return gson.toJson(updates);
                } else {
                    res.status(500);
                    return "Update failed";
                }
            } catch (Exception e) {
                System.err.println("Error updating event: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Error updating event: " + e.getMessage();
            }
        });

        // Delete event
        delete("/events/:id", (req, res) -> {
            String id = req.params(":id");
            System.out.println("Deleting event: " + id);
            try {
                boolean success = eventRepo.delete(id);
                res.header("Access-Control-Allow-Origin", "*");
                if (success) {
                    return "Event deleted successfully";
                } else {
                    res.status(404);
                    return "Event not found";
                }
            } catch (Exception e) {
                System.err.println("Error deleting event: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Error deleting event: " + e.getMessage();
            }
        });
    }
}