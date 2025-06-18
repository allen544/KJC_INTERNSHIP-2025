package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    public static MongoDatabase getDatabase() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        return client.getDatabase("day3taskdb");
    }
}