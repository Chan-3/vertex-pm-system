package com.vertex.pm.model;

public class Budget {
    private final String projectId;
    private double totalAmount;
    private double spentAmount;

    public Budget(String projectId, double totalAmount, double spentAmount) {
        this.projectId = projectId;
        this.totalAmount = totalAmount;
        this.spentAmount = spentAmount;
    }

    public String getProjectId() {
        return projectId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return totalAmount - spentAmount;
    }
}
