package org.example.controller;
import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import org.example.models.ErrorResponse;
import com.google.gson.Gson;
import org.bson.Document;
import org.example.models.SimpleResponse;
import org.example.repository.BookingRepository;
import org.example.repository.EventRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtils;
import spark.Spark;

import java.util.List;

import static spark.Spark.*;

public class AdminController {
    private final EventRepository eventRepo = new EventRepository();
    private final UserRepository userRepo = new UserRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final Gson gson = new Gson();



    public AdminController() {
        setupRoutes();
    }

    public AdminController(UserRepository userRepo, EventRepository eventRepo, BookingRepository bookingRepo) {
    }

    private void setupRoutes() {
        // Admin stats endpoints
        path("/api/admin", () -> {
            before("/*", (req, res) -> {
                if (!JwtUtils.isAdmin(req.cookie("jwt"))) {
                    halt(403, "Admin access required");
                }
            });


            get("/users", (req, res) -> {
                res.type("application/json");
                try {
                    List<Document> users = userRepo.getAllUsers();
                    return gson.toJson(users);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(new ErrorResponse("Failed to fetch users"));
                }
            });

            put("/users/:email", (req, res) -> {
                try {
                    String email = req.params(":email");
                    JsonObject body = gson.fromJson(req.body(), JsonObject.class);

                    // Validate required fields
                    if (!body.has("name") || !body.has("role")) {
                        res.status(400);
                        return gson.toJson(new ErrorResponse("Missing required fields"));
                    }

                    // Update user in database
                    boolean updated = userRepo.updateUser(
                            email,
                            body.get("name").getAsString(),
                            body.get("role").getAsString()
                    );

                    if (updated) {
                        return gson.toJson(new SimpleResponse("User updated successfully"));
                    } else {
                        res.status(404);
                        return gson.toJson(new ErrorResponse("User not found"));
                    }
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(new ErrorResponse("Update failed: " + e.getMessage()));
                }
            });

            delete("/users/:email", (req, res) -> {
                try {
                    String email = req.params(":email");
                    System.out.println("[DELETE] Attempting to delete user: " + email);

                    boolean deleted = userRepo.deleteByEmail(email);

                    if (deleted) {
                        System.out.println("[DELETE] Successfully deleted: " + email);
                        return gson.toJson(new SimpleResponse("User deleted successfully"));
                    } else {
                        System.out.println("[DELETE] User not found: " + email);
                        res.status(404);
                        return gson.toJson(new ErrorResponse("User not found"));
                    }
                } catch (Exception e) {
                    System.err.println("[DELETE] Error: " + e.getMessage());
                    res.status(500);
                    return gson.toJson(new ErrorResponse("Delete failed: " + e.getMessage()));
                }
            });


            get("/total-users", (req, res) -> {
                res.type("application/json");
                return gson.toJson(new StatResponse(userRepo.getTotalUsers()));
            });

            get("/total-events", (req, res) -> {
                res.type("application/json");
                return gson.toJson(new StatResponse(eventRepo.getTotalEvents()));
            });


            get("/active-events", (req, res) -> {
                res.type("application/json");
                return gson.toJson(new StatResponse(eventRepo.getActiveEventsCount()));
            });

            get("/total-bookings", (req, res) -> {
                res.type("application/json");
                return gson.toJson(new StatResponse(bookingRepo.getTotalBookings()));
            });


        });
    }



    private static class StatResponse {
        private final long count;

        public StatResponse(long count) {
            this.count = count;
        }

        public long getCount() {
            return count;
        }
    }

}