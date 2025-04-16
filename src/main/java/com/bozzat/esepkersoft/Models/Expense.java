package com.bozzat.esepkersoft.Models;

import java.time.LocalDateTime;

public class Expense {
    private int id;
    private double amount;
    private int categoryId;
    private String description;
    private LocalDateTime expenseDate;
    private LocalDateTime createdAt;

    public Expense() {
    }

    public Expense(double amount, int categoryId, String description, LocalDateTime expenseDate) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
        this.expenseDate = expenseDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 