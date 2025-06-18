package com.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MongoDatabase db = MongoConnection.getDatabase();
        MongoCollection<Document> students = db.getCollection("students");
        MongoCollection<Document> courses = db.getCollection("courses");
        MongoCollection<Document> enrollments = db.getCollection("enrollments");

        students.drop(); courses.drop(); enrollments.drop();

        Scanner scanner = new Scanner(System.in);

        // 1. Take input for students
        System.out.print("Enter first student's name: ");
        String name1 = scanner.nextLine();
        System.out.print("Enter first student's email: ");
        String email1 = scanner.nextLine();

        System.out.print("Enter second student's name: ");
        String name2 = scanner.nextLine();
        System.out.print("Enter second student's email: ");
        String email2 = scanner.nextLine();

        Document s1 = Student.createStudent(name1, email1);
        Document s2 = Student.createStudent(name2, email2);
        students.insertMany(Arrays.asList(s1, s2));

        // 2. Take input for courses
        System.out.print("Enter first course title: ");
        String title1 = scanner.nextLine();
        System.out.print("Enter first course instructor: ");
        String instructor1 = scanner.nextLine();

        System.out.print("Enter second course title: ");
        String title2 = scanner.nextLine();
        System.out.print("Enter second course instructor: ");
        String instructor2 = scanner.nextLine();

        Document c1 = Course.createCourse(title1, instructor1);
        Document c2 = Course.createCourse(title2, instructor2);
        courses.insertMany(Arrays.asList(c1, c2));

        ObjectId s1Id = s1.getObjectId("_id");
        ObjectId c1Id = c1.getObjectId("_id");

        // 3. Add two types of enrollments
        Document embedded = Enrollment.createEmbeddedEnrollment(s1, c1);
        Document referenced = Enrollment.createReferencedEnrollment(s1Id, c1Id);
        enrollments.insertMany(Arrays.asList(embedded, referenced));

        // 4. Query and print both types
        System.out.println("\nEmbedded Enrollment:");
        enrollments.find(Filters.eq("type", "embedded")).forEach(System.out::println);

        System.out.println("\nReferenced Enrollment (with lookup):");
        for (Document doc : enrollments.find(Filters.eq("type", "referenced"))) {
            ObjectId stId = doc.getObjectId("studentId");
            ObjectId crId = doc.getObjectId("courseId");

            Document student = students.find(Filters.eq("_id", stId)).first();
            Document course = courses.find(Filters.eq("_id", crId)).first();

            if (student != null && course != null) {
                System.out.println("Student: " + student.getString("name") +
                        " | Course: " + course.getString("title"));
            }
        }

        // 5. Update student name and show effect
        System.out.println("\nUpdating student name (s1) to '" + name1 + " Updated'...");
        students.updateOne(Filters.eq("_id", s1Id), Updates.set("name", name1 + " Updated"));

        System.out.println("→ Referenced data updated:");
        Document updatedStudent = students.find(Filters.eq("_id", s1Id)).first();
        System.out.println(updatedStudent.toJson());

        System.out.println("→ Embedded data remains the same:");
        Document embeddedAfter = enrollments.find(Filters.eq("type", "embedded")).first();
        System.out.println(embeddedAfter.get("student"));

        // 6. Create index on student name
        students.createIndex(Indexes.ascending("name"));
        System.out.println("\nCreated index on 'name' field.");

        System.out.println("\n=== DONE ===");

        scanner.close();
    }
}
