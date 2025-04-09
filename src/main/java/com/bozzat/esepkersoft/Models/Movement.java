package com.bozzat.esepkersoft.Models;
public class Movement {
    private int id;
    private int productId;
    private String type;
    private double quantity;
    private Double pricePerUnit;
    private String referenceId;
    private String movementDate;
    private String notes;

    public Movement() {}

    public Movement(int id, int productId, String type, double quantity,
                    Double pricePerUnit, String referenceId, String movementDate, String notes) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.referenceId = referenceId;
        this.movementDate = movementDate;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public Double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(Double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getMovementDate() { return movementDate; }
    public void setMovementDate(String movementDate) { this.movementDate = movementDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}