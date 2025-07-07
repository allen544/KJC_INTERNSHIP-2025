package org.example.repository;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.example.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRepository {
    private final MongoCollection<Document> collection;

    public UserRepository() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("event_system");
        this.collection = database.getCollection("users");
        System.out.println("Creating email index...");
        collection.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
        System.out.println("Email index created");
    }

    public User findByEmail(String email) {
        Document doc = collection.find(Filters.eq("email", email)).first();
        if (doc == null) return null;

        return new User(
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("password"),
                doc.getString("role"),
                doc.getDate("createdAt") // Now matches the new constructor
        );
    }

    public Document findDocumentByEmail(String email) {
        return collection.find(Filters.eq("email", email)).first();
    }

    public void save(User user) {
        Document doc = new Document()
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("role", user.getRole())
                .append("createdAt", user.getCreatedAt());
        collection.insertOne(doc);
    }

    public boolean deleteByEmail(String email) {
        try {
            System.out.println("Deleting user with email: " + email);
            DeleteResult result = collection.deleteOne(Filters.eq("email", email));
            System.out.println("Delete result: " + result);
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(String email, String newName, String newRole) {
        try {
            UpdateResult result = collection.updateOne(
                    Filters.eq("email", email),
                    Updates.combine(
                            Updates.set("name", newName),
                            Updates.set("role", newRole),
                            Updates.set("updatedAt", new Date())
                    )
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByEmail(String email) {
        return collection.find(Filters.eq("email", email)).first() != null;
    }

    public String findPasswordByEmail(String email) {
        Document doc = collection.find(Filters.eq("email", email)).first();
        return doc != null ? doc.getString("password") : null;
    }

    public String findRoleByEmail(String email) {
        Document doc = collection.find(Filters.eq("email", email)).first();
        return doc != null ? doc.getString("role") : "user";
    }

    public void save(String name, String email, String hashedPassword, String role) {
        Document doc = new Document()
                .append("name", name)
                .append("email", email)
                .append("password", hashedPassword)
                .append("role", role)
                .append("createdAt", new Date());
        collection.insertOne(doc);
    }

    public List<Document> getAllUsers() {
        List<Document> users = new ArrayList<>();
        collection.find()
                .projection(new Document()
                        .append("name", 1)
                        .append("email", 1)
                        .append("createdAt", 1)
                        .append("role", 1)
                        .append("_id", 0))
                .forEach(users::add);
        return users;
    }

    public long getTotalUsers() {
        return collection.countDocuments();


    }

    public boolean updatePassword(String email, String hashedPassword) {
        try {
            UpdateResult result = collection.updateOne(
                    Filters.eq("email", email),
                    Updates.combine(
                            Updates.set("password", hashedPassword),
                            Updates.set("updatedAt", new Date())
                    )
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}