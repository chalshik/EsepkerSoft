package com.bozzat.esepkersoft.Models;
public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private double quantity;
    private double price;
    private Integer movementId;

    public SaleItem() {}

    public SaleItem(int id, int saleId, int productId, double quantity, double price, Integer movementId) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.movementId = movementId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Integer getMovementId() { return movementId; }
    public void setMovementId(Integer movementId) { this.movementId = movementId; }

    public double getTotalPrice() {
        return quantity * price;
    }
}