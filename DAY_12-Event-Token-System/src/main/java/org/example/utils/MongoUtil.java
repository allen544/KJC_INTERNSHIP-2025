package org.example.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoUtil {
    private static MongoClient mongoClient;
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "event_system";

    static {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            System.out.println("MongoDB connection established successfully");
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
            throw e;
        }
    }

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}