package com.vertex.pm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a project budget and its expenses.
 * GRASP Information Expert: the budget computes totals and variance.
 */
public class Budget {
    private final int projectId;
    private final double totalAmount;
    private final List<Expense> expenses = new ArrayList<>();

    /**
     * Creates a budget.
     */
    public Budget(int projectId, double totalAmount) {
        this.projectId = projectId;
        this.totalAmount = totalAmount;
    }

    /**
     * Adds an expense into the budget.
     */
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    /**
     * Removes an expense from the budget.
     */
    public boolean removeExpense(int expenseId) {
        return expenses.removeIf(expense -> expense.getId() == expenseId);
    }

    /**
     * Calculates total spent amount.
     */
    public double getSpentAmount() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    /**
     * Calculates remaining budget amount.
     */
    public double getRemainingAmount() {
        return totalAmount - getSpentAmount();
    }

    public int getProjectId() {
        return projectId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<Expense> getExpenses() {
        return List.copyOf(expenses);
    }
}
