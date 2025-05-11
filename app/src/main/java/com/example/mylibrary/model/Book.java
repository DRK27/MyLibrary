package com.example.mylibrary.model;

import com.google.firebase.firestore.DocumentId;

public class Book {
    @DocumentId
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private int quantity;
    private int availableQuantity;
    private String imageUrl;
    private String category;
    private long addedDate;
    private String borrowedBy;
    private String borrowedDate;

    public Book() {}

    public Book(String title, String author, String isbn, String description, int quantity, String category, String imageUrl) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.quantity = quantity;
        this.availableQuantity = quantity;
        this.category = category;
        this.imageUrl = imageUrl;
        this.addedDate = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getAddedDate() { return addedDate; }
    public void setAddedDate(long addedDate) { this.addedDate = addedDate; }

    public String getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }

    public String getBorrowedDate() { return borrowedDate; }
    public void setBorrowedDate(String borrowedDate) { this.borrowedDate = borrowedDate; }

    public boolean isAvailable() {
        return borrowedBy == null || borrowedBy.isEmpty();
    }
} 