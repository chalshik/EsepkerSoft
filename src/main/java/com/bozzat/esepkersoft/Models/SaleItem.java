package com.bozzat.esepkersoft.Models;

import com.bozzat.esepkersoft.ViewModel.SaleItemViewModel;

public class SaleItem {

    private int saleId;
    private String barcode;
    private double quantity;
    private double price;

    public SaleItem() {}

    public SaleItem(int saleId, String barcode, double quantity, double price) {
        this.saleId = saleId;
        this.barcode = barcode;
        this.quantity = quantity;
        this.price = price;
    }

    // SaleItemViewModel wrapper
    public SaleItem valueOf(SaleItemViewModel itemViewModel) {
        return new SaleItem(
                0,
                itemViewModel.getBarcode(),
                itemViewModel.getQuantity(),
                itemViewModel.getPrice()
        );
    }

    // Getters and Setters

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }


    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalPrice() {
        return quantity * price;
    }

    public String getBarcode() {
        return barcode;
    }
}