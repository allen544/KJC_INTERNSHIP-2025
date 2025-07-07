package org.example.models;

import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Booking {
    private String id;
    private String userEmail;
    private String eventId;
    private String token;
    private LocalDateTime bookingTime;
    private String eventName;
    private String eventLocation;
    private LocalDateTime eventTime;
    private Date bookingDate; // Optional

    // Constructor
    public Booking(String id, String userEmail, String eventId, String token, LocalDateTime bookingTime) {
        this.id = id;
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.token = token;
        this.bookingTime = bookingTime;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    // ✅ Convert to MongoDB-compatible Document (serialize LocalDateTime as String)
    public Document toDocument() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return new Document()
                .append("id", id)
                .append("userEmail", userEmail)
                .append("eventId", eventId)
                .append("token", token)
                .append("bookingTime", bookingTime != null ? bookingTime.format(formatter) : null)
                .append("eventName", eventName)
                .append("eventLocation", eventLocation)
                .append("eventTime", eventTime != null ? eventTime.format(formatter) : null);
    }

    // ✅ Convert from MongoDB Document back to Booking object
    public static Booking fromDocument(Document doc) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        LocalDateTime bookingTime = doc.getString("bookingTime") != null
                ? LocalDateTime.parse(doc.getString("bookingTime"), formatter)
                : null;

        LocalDateTime eventTime = doc.getString("eventTime") != null
                ? LocalDateTime.parse(doc.getString("eventTime"), formatter)
                : null;

        Booking booking = new Booking(
                doc.getString("id"),
                doc.getString("userEmail"),
                doc.getString("eventId"),
                doc.getString("token"),
                bookingTime
        );

        booking.setEventName(doc.getString("eventName"));
        booking.setEventLocation(doc.getString("eventLocation"));
        booking.setEventTime(eventTime);
        return booking;
    }
}
