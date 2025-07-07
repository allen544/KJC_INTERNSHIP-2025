package org.example.models;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;

public class User {
    private ObjectId id;
    private String name;
    private String email;
    private String password; // hashed password
    private String role; // "user" or "admin"
    private Date createdAt;
    private Date updatedAt;

    // MongoDB collection - should be initialized from outside
    private static MongoCollection<Document> collection;

    // Initialize the collection (should be called once at application startup)
    public static void initCollection(MongoDatabase database) {
        collection = database.getCollection("users");
    }

    // Constructors
    public User() {
        this.createdAt = new Date();
    }

    // Add this constructor to your User class
    public User(String name, String email, String password, String role, Date createdAt) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }
    // Database operations
    public Document findByEmail(String email) {
        if (collection == null) {
            throw new IllegalStateException("MongoDB collection not initialized");
        }

        return collection.find(Filters.eq("email", email))
                .projection(new Document()
                        .append("name", 1)
                        .append("email", 1)
                        .append("createdAt", 1)
                        .append("role", 1)
                        .append("_id", 0))
                .first();
    }

    public boolean updateProfile(String newName) {
        if (collection == null) {
            throw new IllegalStateException("MongoDB collection not initialized");
        }

        this.updatedAt = new Date();
        UpdateResult result = collection.updateOne(
                Filters.eq("email", this.email),
                Updates.combine(
                        Updates.set("name", newName),
                        Updates.set("updatedAt", this.updatedAt)
                )
        );
        return result.getModifiedCount() > 0;
    }

    // Getters & Setters
    public String getId() { return id != null ? id.toHexString() : null; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}