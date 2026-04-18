package com.vertex.pm.model;

import java.time.LocalDate;

/**
 * Represents a budget expense entry.
 */
public class Expense {
    private final int id;
    private final int projectId;
    private double amount;
    private String category;
    private LocalDate date;

    /**
     * Creates an expense.
     */
    public Expense(int id, int projectId, double amount, String category, LocalDate date) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public double getAmount() {
        return amount;
    }

    /**
     * Updates the expense amount.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Updates the expense category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * Updates the expense date.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
