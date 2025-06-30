package org.example.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.example.model.Student;
import org.example.elective.util.MongoConnection;

public class LoginService {

    private static final MongoCollection<Document> studentCollection =
            MongoConnection.getDatabase().getCollection("students");

    // Returns student object if login successful, otherwise null
    public static Student login(String email, String password) {
        Document doc = studentCollection.find(Filters.and(
                Filters.eq("email", email),
                Filters.eq("password", password)
        )).first();

        if (doc != null) {
            return new Student(
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("password"),
                    doc.getString("selectedCourse")
            );
        }
        return null;  // login failed
    }

    public static void updateSelectedCourse(String email, String courseCode) {
        MongoCollection<Document> collection = MongoConnection.getDatabase().getCollection("students");

        collection.updateOne(
                Filters.eq("email", email),
                Updates.set("selectedCourse", courseCode)
        );

        System.out.println("📌 Updated selected course for " + email + " → " + courseCode);
    }

    public static Student getStudentByEmail(String email) {
        MongoCollection<Document> collection = MongoConnection.getDatabase().getCollection("students");
        Document doc = collection.find(Filters.eq("email", email)).first();

        if (doc != null) {
            Student student = new Student();
            student.setName(doc.getString("name"));
            student.setEmail(doc.getString("email"));
            student.setPassword(doc.getString("password"));
            student.setSelectedCourse(doc.getString("selectedCourse"));
            return student;
        }
        return null;
    }


}
