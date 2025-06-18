package com.example;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Enrollment {
    public static Document createEmbeddedEnrollment(Document student, Document course) {
        return new Document("type", "embedded")
                .append("student", student)
                .append("course", course);
    }

    public static Document createReferencedEnrollment(ObjectId studentId, ObjectId courseId) {
        return new Document("type", "referenced")
                .append("studentId", studentId)
                .append("courseId", courseId);
    }
}
