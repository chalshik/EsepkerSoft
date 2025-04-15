package com.bozzat.esepkersoft.Models;

public class ReturnItem {
    private int id;
    private int returnId;
    private int productId;
    private double quantity;
    private double unitPrice;

    public ReturnItem() {}

    public ReturnItem(int returnId, int productId, double quantity, double unitPrice) {
        this.returnId = returnId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() {
        return quantity * unitPrice;
    }
} 