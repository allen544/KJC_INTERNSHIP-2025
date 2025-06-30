package org.example.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.example.model.Course;
import org.example.model.Student;
import org.example.elective.util.MongoConnection;

import java.util.ArrayList;
import java.util.List;

public class CourseService {

    private static final MongoCollection<Document> courseCollection =
            MongoConnection.getDatabase().getCollection("courses");

    private static final MongoCollection<Document> studentCollection =
            MongoConnection.getDatabase().getCollection("students");

    // Get all available courses
    public static List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        for (Document doc : courseCollection.find()) {
            Course course = new Course(
                    doc.getString("courseName"),
                    doc.getString("courseCode"),
                    doc.getInteger("seatsAvailable", 0),
                    doc.getString("description")
            );
            courses.add(course);
        }
        return courses;
    }

    // Add new course
    public static boolean addCourse(Course course) {
        // Check if course already exists
        Document existing = courseCollection.find(Filters.eq("courseCode", course.getCourseCode())).first();
        if (existing != null) {
            return false;
        }

        Document doc = new Document("courseName", course.getCourseName())
                .append("courseCode", course.getCourseCode())
                .append("seatsAvailable", course.getSeatsAvailable())
                .append("description", course.getDescription());

        courseCollection.insertOne(doc);
        return true;
    }

    // Delete course
    public static boolean deleteCourse(String courseCode) {
        Document result = courseCollection.findOneAndDelete(Filters.eq("courseCode", courseCode));
        return result != null;
    }

    // Reduce seat count by 1
    public static boolean reduceSeatCount(String courseCode) {
        Document course = courseCollection.find(Filters.eq("courseCode", courseCode)).first();
        if (course != null && course.getInteger("seatsAvailable", 0) > 0) {
            courseCollection.updateOne(
                    Filters.eq("courseCode", courseCode),
                    Updates.inc("seatsAvailable", -1)
            );
            return true;
        }
        return false;
    }

    // Get students enrolled in a course
    public static List<Student> getCourseEnrollments(String courseCode) {
        List<Student> students = new ArrayList<>();
        for (Document doc : studentCollection.find(Filters.eq("selectedCourse", courseCode))) {
            students.add(new Student(
                    doc.getString("name"),
                    doc.getString("email"),
                    null, // Don't return password
                    doc.getString("selectedCourse")
            ));
        }
        return students;
    }

    // Remove student from course
    public static boolean removeEnrollment(String courseCode, String studentEmail) {
        // 1. Remove course from student
        studentCollection.updateOne(
                Filters.eq("email", studentEmail),
                Updates.set("selectedCourse", null)
        );

        // 2. Increment available seats
        courseCollection.updateOne(
                Filters.eq("courseCode", courseCode),
                Updates.inc("seatsAvailable", 1)
        );

        return true;
    }
}