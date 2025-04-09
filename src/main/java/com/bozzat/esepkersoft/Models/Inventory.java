package com.bozzat.esepkersoft.Models;
public class Inventory {
    private int id;
    private int productId;
    private double quantity;
    private String lastUpdated;

    public Inventory() {}

    public Inventory(int id, int productId, double quantity, String lastUpdated) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}