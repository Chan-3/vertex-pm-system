package com.vertex.pm.util;

import com.sun.net.httpserver.HttpExchange;
import com.vertex.pm.model.Budget;
import com.vertex.pm.model.Dependency;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Risk;
import com.vertex.pm.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides minimal JSON formatting and parsing helpers for the local project.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    /**
     * Sends a JSON response.
     */
    public static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    /**
     * Escapes a string for JSON output.
     */
    public static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Serializes a project list into JSON.
     */
    public static String projectsToJson(List<Project> projects) {
        return "[" + projects.stream().map(JsonUtil::projectToJson).collect(Collectors.joining(",")) + "]";
    }

    /**
     * Serializes tasks into JSON.
     */
    public static String tasksToJson(List<Task> tasks) {
        return "[" + tasks.stream().map(JsonUtil::taskToJson).collect(Collectors.joining(",")) + "]";
    }

    /**
     * Serializes resources into JSON.
     */
    public static String resourcesToJson(List<Resource> resources) {
        return "[" + resources.stream().map(JsonUtil::resourceToJson).collect(Collectors.joining(",")) + "]";
    }

    /**
     * Serializes milestones into JSON.
     */
    public static String milestonesToJson(List<Milestone> milestones) {
        return "[" + milestones.stream().map(JsonUtil::milestoneToJson).collect(Collectors.joining(",")) + "]";
    }

    /**
     * Serializes expenses into JSON.
     */
    public static String expensesToJson(List<Expense> expenses) {
        return "[" + expenses.stream().map(JsonUtil::expenseToJson).collect(Collectors.joining(",")) + "]";
    }

    /**
     * Serializes a map into JSON.
     */
    public static String mapToJson(Map<String, Object> values) {
        return "{"
                + values.entrySet().stream()
                .map(entry -> "\"" + escape(entry.getKey()) + "\":" + toJsonValue(entry.getValue()))
                .collect(Collectors.joining(","))
                + "}";
    }

    /**
     * Serializes one project into JSON.
     */
    public static String projectToJson(Project project) {
        return "{"
                + "\"id\":" + project.getId() + ","
                + "\"name\":\"" + escape(project.getName()) + "\","
                + "\"description\":\"" + escape(project.getDescription()) + "\","
                + "\"startDate\":\"" + project.getStartDate() + "\","
                + "\"endDate\":\"" + project.getEndDate() + "\","
                + "\"status\":\"" + project.getStatus() + "\","
                + "\"completion\":" + Math.round(project.getCompletionRatio() * 100) + ","
                + "\"budget\":" + budgetToJson(project.getBudget()) + ","
                + "\"tasks\":[" + project.getTasks().stream().map(JsonUtil::taskToJson).collect(Collectors.joining(",")) + "],"
                + "\"resources\":[" + project.getResources().stream().map(JsonUtil::resourceToJson).collect(Collectors.joining(",")) + "],"
                + "\"milestones\":[" + project.getMilestones().stream().map(JsonUtil::milestoneToJson).collect(Collectors.joining(",")) + "],"
                + "\"dependencies\":[" + project.getDependencies().stream().map(JsonUtil::dependencyToJson).collect(Collectors.joining(",")) + "],"
                + "\"risks\":[" + project.getRisks().stream().map(JsonUtil::riskToJson).collect(Collectors.joining(",")) + "]"
                + "}";
    }

    private static String taskToJson(Task task) {
        return "{"
                + "\"id\":" + task.getId() + ","
                + "\"projectId\":" + task.getProjectId() + ","
                + "\"name\":\"" + escape(task.getName()) + "\","
                + "\"startDate\":\"" + task.getStartDate() + "\","
                + "\"endDate\":\"" + task.getEndDate() + "\","
                + "\"status\":\"" + task.getStatus() + "\""
                + "}";
    }

    private static String resourceToJson(Resource resource) {
        return "{"
                + "\"id\":" + resource.getId() + ","
                + "\"name\":\"" + escape(resource.getName()) + "\","
                + "\"role\":\"" + escape(resource.getRole()) + "\","
                + "\"available\":" + resource.isAvailable()
                + "}";
    }

    private static String milestoneToJson(Milestone milestone) {
        return "{"
                + "\"id\":" + milestone.getId() + ","
                + "\"projectId\":" + milestone.getProjectId() + ","
                + "\"name\":\"" + escape(milestone.getName()) + "\","
                + "\"targetDate\":\"" + milestone.getTargetDate() + "\","
                + "\"completed\":" + milestone.isCompleted()
                + "}";
    }

    private static String dependencyToJson(Dependency dependency) {
        return "{"
                + "\"taskId\":" + dependency.getTaskId() + ","
                + "\"dependsOnTaskId\":" + dependency.getDependsOnTaskId()
                + "}";
    }

    private static String riskToJson(Risk risk) {
        return "{"
                + "\"id\":" + risk.getId() + ","
                + "\"projectId\":" + risk.getProjectId() + ","
                + "\"description\":\"" + escape(risk.getDescription()) + "\","
                + "\"severity\":\"" + risk.getSeverity() + "\""
                + "}";
    }

    private static String budgetToJson(Budget budget) {
        return "{"
                + "\"projectId\":" + budget.getProjectId() + ","
                + "\"totalAmount\":" + budget.getTotalAmount() + ","
                + "\"spentAmount\":" + budget.getSpentAmount() + ","
                + "\"remainingAmount\":" + budget.getRemainingAmount() + ","
                + "\"expenses\":[" + budget.getExpenses().stream().map(JsonUtil::expenseToJson).collect(Collectors.joining(",")) + "]"
                + "}";
    }

    /**
     * Exposes budget serialization for budget endpoints.
     */
    public static String budgetToJsonPublic(Budget budget) {
        return budgetToJson(budget);
    }

    private static String expenseToJson(Expense expense) {
        return "{"
                + "\"id\":" + expense.getId() + ","
                + "\"projectId\":" + expense.getProjectId() + ","
                + "\"amount\":" + expense.getAmount() + ","
                + "\"category\":\"" + escape(expense.getCategory()) + "\","
                + "\"date\":\"" + expense.getDate() + "\""
                + "}";
    }

    /**
     * Parses a flat JSON object used by the create-project form.
     */
    public static Map<String, String> parseFlatJson(String json) {
        Map<String, String> values = new HashMap<>();
        String cleaned = json.trim();
        if (cleaned.startsWith("{")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("}")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.isBlank()) {
            return values;
        }
        String[] pairs = cleaned.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                values.put(key, value);
            }
        }
        return values;
    }

    /**
     * Parses an exchange body into a flat JSON map.
     */
    public static Map<String, String> parseBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return parseFlatJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static String toJsonValue(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }
}
