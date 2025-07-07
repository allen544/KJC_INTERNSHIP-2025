package org.example.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.example.models.Booking;
import org.example.utils.MongoUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingRepository {
    private final MongoCollection<Document> collection =
            MongoUtil.getDatabase().getCollection("bookings");

    public BookingRepository() {
        // Create indexes for faster access
        collection.createIndex(Indexes.compoundIndex(
                Indexes.ascending("userEmail"),
                Indexes.ascending("eventId")
        ));
        collection.createIndex(Indexes.ascending("userEmail"));
    }

    public void save(Booking booking) {
        try {
            // ✅ Convert LocalDateTime to java.util.Date
            Date bookingDate = booking.getBookingTime() != null
                    ? Date.from(booking.getBookingTime().atZone(ZoneId.systemDefault()).toInstant())
                    : null;

            Document doc = new Document()
                    .append("_id", booking.getId())
                    .append("userEmail", booking.getUserEmail())
                    .append("eventId", booking.getEventId())
                    .append("token", booking.getToken())
                    .append("timestamp", bookingDate);  // ✅ Save Date, not LocalDateTime

            System.out.println("Saving booking document: " + doc.toJson());
            collection.insertOne(doc);
            System.out.println("Booking saved successfully");
        } catch (Exception e) {
            System.err.println("Error saving booking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save booking", e);
        }
    }

    public List<Booking> findByEmail(String email) {
        List<Booking> bookings = new ArrayList<>();
        try {
            System.out.println("Searching for bookings with email: " + email);

            collection.find(Filters.eq("userEmail", email))
                    .forEach(doc -> {
                        try {
                            System.out.println("Found booking document: " + doc.toJson());

                            String id = doc.getString("_id");
                            String userEmail = doc.getString("userEmail");
                            String eventId = doc.getString("eventId");
                            String token = doc.getString("token");

                            // ✅ Convert stored java.util.Date back to LocalDateTime
                            LocalDateTime timestamp = null;
                            Object timestampObj = doc.get("timestamp");
                            if (timestampObj instanceof Date) {
                                timestamp = ((Date) timestampObj).toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            }

                            Booking booking = new Booking(id, userEmail, eventId, token, timestamp);
                            bookings.add(booking);
                        } catch (Exception e) {
                            System.err.println("Error parsing booking document: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

            System.out.println("Retrieved " + bookings.size() + " bookings for email: " + email);
        } catch (Exception e) {
            System.err.println("Error finding bookings by email: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean delete(String id) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
            System.out.println("Deleted booking with ID: " + id);
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAlreadyBooked(String email, String eventId) {
        try {
            Document existing = collection.find(
                    Filters.and(
                            Filters.eq("userEmail", email),
                            Filters.eq("eventId", eventId)
                    )
            ).first();

            boolean hasBooked = existing != null;
            System.out.println("User " + email + " has already booked event " + eventId + ": " + hasBooked);
            return hasBooked;
        } catch (Exception e) {
            System.err.println("Error checking existing booking: " + e.getMessage());
            return false;
        }
    }

    public long getTotalBookings() {
        return collection.countDocuments();
    }

    public long getBookingsCountForEvent(String eventId) {
        return collection.countDocuments(Filters.eq("eventId", eventId));
    }
}
