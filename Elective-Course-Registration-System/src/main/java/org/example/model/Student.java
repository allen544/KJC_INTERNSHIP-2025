package org.example.model;

public class Student {
    private String name;
    private String email;
    private String password;
    private String selectedCourse;

    public Student() {}

    public Student(String name, String email, String password, String selectedCourse) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.selectedCourse = selectedCourse;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSelectedCourse() { return selectedCourse; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setSelectedCourse(String selectedCourse) { this.selectedCourse = selectedCourse; }
}
