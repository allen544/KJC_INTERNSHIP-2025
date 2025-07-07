package org.example.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.models.Event;
import org.example.utils.MongoUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventRepository {
    private final MongoCollection<Document> collection =
            MongoUtil.getDatabase().getCollection("events");
    public boolean decrementTokenLimit(String eventId) {
        try {
            // Ensure we don't go below 0
            return collection.updateOne(
                    Filters.and(
                            Filters.eq("_id", new ObjectId(eventId)),
                            Filters.gt("tokenLimit", 0)
                    ),
                    Updates.inc("tokenLimit", -1)
            ).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Decrement error: " + e.getMessage());
            return false;
        }
    }

    public String create(Event event) {
        Document doc = new Document()
                .append("name", event.getName())
                .append("description", event.getDescription())
                .append("location", event.getLocation())
                .append("time", event.getTime())
                .append("tokenLimit", event.getTokenLimit());

        collection.insertOne(doc);
        return doc.getObjectId("_id").toString();
    }

    public boolean update(Event event) {
        return collection.updateOne(
                Filters.eq("_id", new ObjectId(event.getId())),
                Updates.combine(
                        Updates.set("name", event.getName()),
                        Updates.set("description", event.getDescription()),
                        Updates.set("location", event.getLocation()),
                        Updates.set("time", event.getTime()),
                        Updates.set("tokenLimit", event.getTokenLimit())
                )
        ).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return collection.deleteOne(
                Filters.eq("_id", new ObjectId(id))
        ).getDeletedCount() > 0;
    }

    public Event findById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return doc != null ? Event.fromDocument(doc) : null;
    }

    public List<Event> findAll() {
        List<Event> events = new ArrayList<>();
        try {
            System.out.println("Querying MongoDB for events...");
            collection.find().forEach(doc -> {
                System.out.println("Found document: " + doc.toJson());
                events.add(Event.fromDocument(doc));
            });
            System.out.println("Total events retrieved: " + events.size());
        } catch (Exception e) {
            System.err.println("Error in findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }
    public long getTotalEvents() {
        return collection.countDocuments();
    }

    public long getActiveEventsCount() {
        Document query = new Document("time", new Document("$gt", new Date()));
        return collection.countDocuments(query);
    }


    }
