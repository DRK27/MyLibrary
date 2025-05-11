package com.example.mylibrary.model;

import com.google.firebase.firestore.DocumentId;

public class BorrowedBook {
    @DocumentId
    private String id;
    private String bookId;
    private String studentId;
    private long borrowDate;
    private long dueDate;
    private long returnDate;
    private String status; // "BORROWED", "RETURNED", "OVERDUE"

    private String bookTitle;
    private String studentName;

    private String userId;

    public BorrowedBook() {}

    public BorrowedBook(String bookId, String studentId, String bookTitle, String studentName) {
        this.bookId = bookId;
        this.studentId = studentId;
        this.bookTitle = bookTitle;
        this.studentName = studentName;
        this.borrowDate = System.currentTimeMillis();
        this.dueDate = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L); // 14 дней
        this.status = "BORROWED";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public long getBorrowDate() { return borrowDate; }
    public void setBorrowDate(long borrowDate) { this.borrowDate = borrowDate; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public long getReturnDate() { return returnDate; }
    public void setReturnDate(long returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
} 