package com.example;

import org.bson.Document;

public class Student {
    public static Document createStudent(String name, String email) {
        return new Document("name", name).append("email", email);
    }
}
