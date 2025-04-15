package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class Return {
    private int id;
    private int saleId;
    private LocalDateTime returnTime;
    private double totalRefund;
    private String reason;

    public Return() {}

    public Return(int saleId, double totalRefund, String reason) {
        this.saleId = saleId;
        this.totalRefund = totalRefund;
        this.reason = reason;
        this.returnTime = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }

    public double getTotalRefund() { return totalRefund; }
    public void setTotalRefund(double totalRefund) { this.totalRefund = totalRefund; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
} 