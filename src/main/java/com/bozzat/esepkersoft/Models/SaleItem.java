package com.bozzat.esepkersoft.Models;

import com.bozzat.esepkersoft.ViewModel.SaleItemViewModel;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private double quantity;
    private double unitPrice;

    public SaleItem() {}

    public SaleItem(int saleId, int productId, double quantity, double unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // SaleItemViewModel wrapper
    public static SaleItem valueOf(SaleItemViewModel itemViewModel) {
        return new SaleItem(
                0,
                itemViewModel.getProductId(),
                itemViewModel.getQuantity(),
                itemViewModel.getPrice()
        );
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

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() {
        return quantity * unitPrice;
    }
}