package org.example;

import com.google.gson.Gson;
import org.example.model.Course;
import org.example.model.Student;
import org.example.service.CourseService;
import org.example.service.LoginService;
import org.example.service.StudentService;

import static spark.Spark.*;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        port(8080);

        // Serve HTML, CSS, JS from /templates directory
        staticFiles.externalLocation("src/main/resources/templates");

        // Redirect root URL to index.html
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        // CORS headers
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });

        options("/*", (req, res) -> "OK");

        Gson gson = new Gson();

        // 👤 Register Student
        post("/register-student", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String name = body.get("name");
            String email = body.get("email");

            StudentService.registerStudent(name, email);
            res.status(200);
            return gson.toJson(Map.of("message", "Student registered"));
        });

        // 🔐 Login
        post("/login", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String email = body.get("email");
            String password = body.get("password");

            // Admin login check
            if ("admin".equals(email) && "admin".equals(password)) {
                res.status(200);
                return gson.toJson(Map.of("name", "Admin", "email", "admin"));
            }

            Student student = LoginService.login(email, password);
            if (student != null) {
                res.status(200);
                return gson.toJson(student);
            } else {
                res.status(401);
                return gson.toJson(Map.of("error", "Invalid credentials"));
            }
        });

        // 📚 Get all courses
        get("/courses", (req, res) -> {
            List<Course> courses = CourseService.getAllCourses();
            res.type("application/json");
            return gson.toJson(courses);
        });

        // ➕ Add new course (Admin only)
        post("/courses", (req, res) -> {
            Course course = gson.fromJson(req.body(), Course.class);
            boolean success = CourseService.addCourse(course);
            if (success) {
                return gson.toJson(Map.of("message", "Course added successfully"));
            } else {
                res.status(400);
                return gson.toJson(Map.of("error", "Course already exists"));
            }
        });

        // ❌ Delete course (Admin only)
        delete("/courses/:courseCode", (req, res) -> {
            String courseCode = req.params(":courseCode");
            boolean deleted = CourseService.deleteCourse(courseCode);
            if (deleted) {
                return gson.toJson(Map.of("message", "Course deleted successfully"));
            } else {
                res.status(404);
                return gson.toJson(Map.of("error", "Course not found"));
            }
        });

        // ✅ Register course (update student's selectedCourse & reduce seat)
        post("/register-course", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String email = body.get("email");
            String courseCode = body.get("courseCode");

            boolean updated = CourseService.reduceSeatCount(courseCode);
            if (updated) {
                LoginService.updateSelectedCourse(email, courseCode);
                return gson.toJson(Map.of("message", "Registered successfully"));
            } else {
                res.status(400);
                return gson.toJson(Map.of("error", "Course is full or not found"));
            }
        });

        // 🚫 Remove enrollment (Admin only)
        post("/remove-enrollment", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String courseCode = body.get("courseCode");
            String studentEmail = body.get("studentEmail");

            boolean success = CourseService.removeEnrollment(courseCode, studentEmail);
            if (success) {
                return gson.toJson(Map.of("message", "Enrollment removed successfully"));
            } else {
                res.status(400);
                return gson.toJson(Map.of("error", "Failed to remove enrollment"));
            }
        });

        // 👥 Get all students (Admin only)
        get("/students", (req, res) -> {
            List<Student> students = StudentService.getAllStudents();
            return gson.toJson(students);
        });

        // 🗑️ Delete student (Admin only)
        delete("/students/:email", (req, res) -> {
            String email = req.params(":email");
            boolean deleted = StudentService.deleteStudent(email);
            if (deleted) {
                return gson.toJson(Map.of("message", "Student deleted successfully"));
            } else {
                res.status(404);
                return gson.toJson(Map.of("error", "Student not found"));
            }
        });

        // 📊 Get course enrollments (Admin only)
        get("/course-enrollments", (req, res) -> {
            String courseCode = req.queryParams("courseCode");
            List<Student> students = CourseService.getCourseEnrollments(courseCode);
            return gson.toJson(students);
        });

        // 📥 Get student details (used to show opted course)
        get("/student", (req, res) -> {
            String email = req.queryParams("email");
            Student student = LoginService.getStudentByEmail(email);
            if (student != null) {
                return gson.toJson(student);
            } else {
                res.status(404);
                return gson.toJson(Map.of("error", "Student not found"));
            }
        });

        System.out.println("✅ Server started at http://localhost:8080");
    }
}