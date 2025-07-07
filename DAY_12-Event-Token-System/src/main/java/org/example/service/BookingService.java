package org.example.service;

import org.example.models.Booking;
import org.example.models.Event;
import org.example.repository.BookingRepository;
import org.example.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingService {
    private final BookingRepository bookingRepo = new BookingRepository();
    private final EventRepository eventRepo = new EventRepository();

    public boolean bookTicket(String userEmail, String eventId) {
        try {
            // 1. First check event exists and has capacity
            Event event = eventRepo.findById(eventId);
            if (event == null || event.getTokenLimit() <= 0) {
                System.out.println("Event unavailable - ID: " + eventId
                        + ", Tokens: " + (event != null ? event.getTokenLimit() : "null"));
                return false;
            }

            // 2. Check for existing booking (atomic operation)
            if (bookingRepo.hasAlreadyBooked(userEmail, eventId)) {
                System.out.println("Duplicate booking attempt - User: "
                        + userEmail + ", Event: " + eventId);
                return false;
            }

            // 3. Create and save booking
            String bookingToken = UUID.randomUUID().toString().substring(0, 8);
            Booking booking = new Booking(
                    UUID.randomUUID().toString(),
                    userEmail,
                    eventId,
                    bookingToken,
                    LocalDateTime.now()
            );
            bookingRepo.save(booking);

            // 4. Decrement tokens (atomic operation)
            if (!eventRepo.decrementTokenLimit(eventId)) {
                // Critical: Rollback booking if token update fails
                boolean deleted = bookingRepo.delete(booking.getId());
                System.err.println("Token update failed - booking rollback " +
                        (deleted ? "successful" : "failed"));
                return false;
            }

            // 5. Send confirmation email AFTER successful booking
            System.out.println("Booking successful - sending confirmation email to: " + userEmail);
            sendConfirmationEmail(userEmail, event, bookingToken);

            return true;
        } catch (Exception e) {
            System.err.println("Booking failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Booking> getBookingsByEmail(String email) {
        System.out.println("BookingService: Fetching bookings for email: " + email);

        try {
            List<Booking> bookings = bookingRepo.findByEmail(email);
            System.out.println("Found " + bookings.size() + " bookings");

            // Enrich with event data
            bookings.forEach(booking -> {
                try {
                    Event event = eventRepo.findById(booking.getEventId());
                    if (event != null) {
                        booking.setEventName(event.getName());
                        booking.setEventLocation(event.getLocation());
                        booking.setEventTime(event.getTime());
                        System.out.println("Enriched booking with event data: " + event.getName());
                    } else {
                        System.out.println("Event not found for booking: " + booking.getEventId());
                    }
                } catch (Exception e) {
                    System.err.println("Error enriching booking: " + e.getMessage());
                }
            });

            return bookings;
        } catch (Exception e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list instead of null
        }
    }

    // FIXED: Make method accessible and add proper error handling
    private void sendConfirmationEmail(String email, Event event, String token) {
        // Use a daemon thread to prevent blocking
        Thread emailThread = new Thread(() -> {
            try {
                System.out.println("Sending confirmation email to: " + email);

                String subject = "🎟 Booking Confirmation: " + event.getName();
                String body = String.format(
                        "Dear Customer,\n\n" +
                                "Your booking has been confirmed!\n\n" +
                                "Event Details:\n" +
                                "• Event: %s\n" +
                                "• Location: %s\n" +
                                "• Time: %s\n" +
                                "• Your Token: %s\n\n" +
                                "Please save this token for entry.\n\n" +
                                "Best regards,\n" +
                                "Event Token System Team",
                        event.getName(),
                        event.getLocation(),
                        event.getTime().toString(),
                        token
                );

                EmailService.send(email, subject, body);
                System.out.println("✅ Confirmation email sent successfully to: " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send confirmation email to " + email + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Set as daemon thread to prevent JVM hanging
        emailThread.setDaemon(true);
        emailThread.start();
    }

    // Optional: Test email functionality
    public void testEmailConnection() {
        boolean connected = EmailService.testEmailConnection();
        System.out.println("Email connection test: " + (connected ? "PASSED" : "FAILED"));
    }
}