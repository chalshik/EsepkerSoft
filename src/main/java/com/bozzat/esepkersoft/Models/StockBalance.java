package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class StockBalance {
    private int id;
    private int productId;
    private double quantity;
    private LocalDateTime updatedAt;

    public StockBalance() {}

    public StockBalance(int productId, double quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 