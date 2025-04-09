package com.bozzat.esepkersoft.Models;
public class Product {
    private int id;
    private String barcode;
    private String name;
    private String unitType;
    private double currentPrice;

    public Product() {}

    public Product(int id, String barcode, String name,
                   String unitType, double currentPrice) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.unitType = unitType;
        this.currentPrice = currentPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnitType() { return unitType; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}