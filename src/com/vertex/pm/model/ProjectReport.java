package com.vertex.pm.model;

import java.util.List;
import java.util.Map;

public record ProjectReport(
        String projectId,
        String projectName,
        int totalTasks,
        int completedTasks,
        int inProgressTasks,
        int plannedTasks,
        int overdueTasks,
        int overallProgress,
        double budgetTotal,
        double totalExpenses,
        double remainingBudget,
        List<Map<String, Object>> milestoneStatus,
        List<Map<String, Object>> taskDetails
) {
}
