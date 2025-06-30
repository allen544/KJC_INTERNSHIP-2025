package org.example.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.example.model.Student;
import org.example.elective.util.MongoConnection;
import org.example.elective.util.PasswordGenerator;
import org.example.service.EmailService;

import java.util.ArrayList;
import java.util.List;

public class StudentService {

    private static final MongoCollection<Document> studentCollection =
            MongoConnection.getDatabase().getCollection("students");

    public static void registerStudent(String name, String email) {
        // 1. Generate random password
        String password = PasswordGenerator.generate();

        // 2. Create Student object
        Student student = new Student(name, email, password, null);

        // 3. Send email with password
        EmailService.sendPasswordEmail(email, name, password);

        // 4. Save student to MongoDB
        Document doc = new Document("name", student.getName())
                .append("email", student.getEmail())
                .append("password", student.getPassword())
                .append("selectedCourse", student.getSelectedCourse());

        studentCollection.insertOne(doc);

        System.out.println("✅ Student registered and saved in DB: " + email);
    }

    // Get all students
    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        for (Document doc : studentCollection.find()) {
            students.add(new Student(
                    doc.getString("name"),
                    doc.getString("email"),
                    null, // Don't return passwords
                    doc.getString("selectedCourse")
            ));
        }
        return students;
    }

    // Delete student
    public static boolean deleteStudent(String email) {
        Document result = studentCollection.findOneAndDelete(Filters.eq("email", email));
        return result != null;
    }
}