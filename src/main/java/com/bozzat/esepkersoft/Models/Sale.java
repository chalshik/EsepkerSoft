package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class Sale {
    private int id;
    private double totalAmount;
    private String paymentMethod;
    private LocalDateTime saleTime;
    private String comment;

    // Constructors
    public Sale() {}

    public Sale(String paymentMethod, double totalAmount) {
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.saleTime = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getSaleTime() { return saleTime; }
    public void setSaleTime(LocalDateTime saleTime) { this.saleTime = saleTime; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public String toString() {
        return String.format("Sale [id=%d, saleTime=%s, totalAmount=%.2f]",
                id, saleTime, totalAmount);
    }
}