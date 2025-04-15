package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class Supplier {
    private int id;
    private String name;
    private String contactInfo;
    private LocalDateTime createdAt;

    public Supplier() {}

    public Supplier(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 