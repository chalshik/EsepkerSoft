package com.bozzat.esepkersoft.Models;

public class Category {
    private int id;
    private String name;

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
} 