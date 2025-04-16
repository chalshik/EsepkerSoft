package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String name;
    private String barcode;
    private int categoryId;
    private String unitType;
    private LocalDateTime createdAt;
    private double currentPrice; // From retail_prices table

    public Product() {}

    public Product(int id, String name, String barcode, int categoryId, 
                  String unitType, double currentPrice) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.unitType = unitType;
        this.currentPrice = currentPrice;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}