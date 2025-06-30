package org.example.model;

public class Course {
    private String courseName;
    private String courseCode;
    private int seatsAvailable;
    private String description;

    public Course() {}

    public Course(String courseName, String courseCode, int seatsAvailable, String description) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.seatsAvailable = seatsAvailable;
        this.description = description;
    }

    // Getters and Setters
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public int getSeatsAvailable() { return seatsAvailable; }
    public String getDescription() { return description; }

    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setSeatsAvailable(int seatsAvailable) { this.seatsAvailable = seatsAvailable; }
    public void setDescription(String description) { this.description = description; }
}