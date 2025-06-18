package com.example;

import org.bson.Document;

public class Course {
    public static Document createCourse(String title, String instructor) {
        return new Document("title", title).append("instructor", instructor);
    }
}