package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class StockEntry {
    private int id;
    private int productId;
    private double quantity;
    private double purchasePrice;
    private int supplierId;
    private LocalDateTime arrivalDate;
    private String note;

    public StockEntry() {}

    public StockEntry(double quantity, double purchasePrice,
                     int supplierId) {
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.supplierId = supplierId;
        this.arrivalDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public LocalDateTime getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDateTime arrivalDate) { this.arrivalDate = arrivalDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
} 