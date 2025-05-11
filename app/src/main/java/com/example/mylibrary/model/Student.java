package com.example.mylibrary.model;

import java.util.List;
import com.google.firebase.firestore.DocumentId;

public class Student {
    @DocumentId
    private String id;
    private String name;
    private String email;
    private String role;
    private String studentId;
    private String phone;
    private boolean active;
    private List<String> borrowedBooks;
    private long registrationDate;

    public Student() {}

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
        this.role = "student";
        this.active = true;
        this.registrationDate = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(List<String> borrowedBooks) { this.borrowedBooks = borrowedBooks; }

    public long getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(long registrationDate) { this.registrationDate = registrationDate; }
} 