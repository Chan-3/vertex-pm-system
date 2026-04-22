package com.vertex.pm.model;

import java.time.LocalDate;

public class Expense {
    private final String id;
    private final String projectId;
    private LocalDate expenseDate;
    private String description;
    private String category;
    private double amount;

    public Expense(String id, String projectId, LocalDate expenseDate, String description, String category, double amount) {
        this.id = id;
        this.projectId = projectId;
        this.expenseDate = expenseDate;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
