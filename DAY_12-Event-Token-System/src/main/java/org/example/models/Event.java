package org.example.models;

import org.bson.Document;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;

public class Event {
    private String id;
    private String name;
    private String description;
    private String location;
    private LocalDateTime time;
    private int tokenLimit;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
    public int getTokenLimit() { return tokenLimit; }
    public void setTokenLimit(int tokenLimit) { this.tokenLimit = tokenLimit; }

    // Helper method for frontend display
    public String getFormattedTime() {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public Document toDocument() {
        return new Document()
                .append("name", name)
                .append("description", description)
                .append("location", location)
                .append("time", time)
                .append("tokenLimit", tokenLimit);
    }

    public static Event fromDocument(Document doc) {
        Event event = new Event();
        event.setId(doc.getObjectId("_id").toString());
        event.setName(doc.getString("name"));
        event.setDescription(doc.getString("description"));
        event.setLocation(doc.getString("location"));

        // Handle multiple date formats
        Object timeObj = doc.get("time");
        try {
            if (timeObj instanceof LocalDateTime) {
                event.setTime((LocalDateTime) timeObj);
            } else if (timeObj instanceof Date) {
                // Convert Date to LocalDateTime
                Date date = (Date) timeObj;
                event.setTime(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (timeObj instanceof String) {
                String timeStr = (String) timeObj;
                // Try different formats
                try {
                    event.setTime(LocalDateTime.parse(timeStr));
                } catch (Exception e1) {
                    try {
                        // Try ISO format with T
                        event.setTime(LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    } catch (Exception e2) {
                        System.err.println("Could not parse time string: " + timeStr);
                        event.setTime(LocalDateTime.now()); // Default fallback
                    }
                }
            } else {
                System.err.println("Unknown time format: " + timeObj.getClass() + " - " + timeObj);
                event.setTime(LocalDateTime.now()); // Default fallback
            }
        } catch (Exception e) {
            System.err.println("Error parsing time: " + e.getMessage());
            event.setTime(LocalDateTime.now()); // Default fallback
        }

        event.setTokenLimit(doc.getInteger("tokenLimit", 0));
        return event;
    }
}