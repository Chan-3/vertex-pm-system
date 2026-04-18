package com.vertex.pm.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Task;
import com.vertex.pm.model.TaskStatus;
import com.vertex.pm.service.ProjectService;
import com.vertex.pm.util.JsonUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.Map;

/**
 * Handles dashboard, task, resource, budget, monitoring, and report endpoints.
 * GRASP Controller: receives module-specific requests from the web UI.
 */
public class SystemController implements HttpHandler {
    private final ProjectService projectService;

    /**
     * Creates the controller.
     */
    public SystemController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase();

            if ("/api/dashboard".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.mapToJson(projectService.getDashboardSummary()));
                return;
            }
            if ("/api/tasks".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.tasksToJson(projectService.getAllTasks()));
                return;
            }
            if ("/api/tasks".equals(path) && "POST".equals(method)) {
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                Task task = new Task(
                        Integer.parseInt(payload.get("id")),
                        Integer.parseInt(payload.get("projectId")),
                        payload.get("name"),
                        LocalDate.parse(payload.get("startDate")),
                        LocalDate.parse(payload.get("endDate")),
                        TaskStatus.valueOf(payload.get("status"))
                );
                projectService.addTask(task);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_CREATED, "{\"message\":\"Task created\"}");
                return;
            }
            if (path.startsWith("/api/tasks/") && "PUT".equals(method)) {
                int taskId = Integer.parseInt(path.substring("/api/tasks/".length()));
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                if (payload.containsKey("name")) {
                    Task task = new Task(
                            taskId,
                            Integer.parseInt(payload.get("projectId")),
                            payload.get("name"),
                            LocalDate.parse(payload.get("startDate")),
                            LocalDate.parse(payload.get("endDate")),
                            TaskStatus.valueOf(payload.get("status"))
                    );
                    projectService.updateTask(task);
                } else {
                    projectService.updateTaskStatus(taskId, TaskStatus.valueOf(payload.get("status")));
                }
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Task updated\"}");
                return;
            }
            if (path.startsWith("/api/tasks/") && "DELETE".equals(method)) {
                int taskId = Integer.parseInt(path.substring("/api/tasks/".length()));
                projectService.deleteTask(taskId);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Task deleted\"}");
                return;
            }
            if ("/api/resources".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.resourcesToJson(projectService.getAllResources()));
                return;
            }
            if ("/api/resources".equals(path) && "POST".equals(method)) {
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                Resource resource = new Resource(
                        Integer.parseInt(payload.get("id")),
                        payload.get("name"),
                        payload.get("role"),
                        Boolean.parseBoolean(payload.getOrDefault("available", "true"))
                );
                projectService.addResource(Integer.parseInt(payload.get("projectId")), resource);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_CREATED, "{\"message\":\"Resource created\"}");
                return;
            }
            if (path.startsWith("/api/resources/") && "PUT".equals(method)) {
                int resourceId = Integer.parseInt(path.substring("/api/resources/".length()));
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                Resource resource = new Resource(
                        resourceId,
                        payload.get("name"),
                        payload.get("role"),
                        Boolean.parseBoolean(payload.getOrDefault("available", "true"))
                );
                projectService.updateResource(resource);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Resource updated\"}");
                return;
            }
            if (path.startsWith("/api/resources/") && "DELETE".equals(method)) {
                int resourceId = Integer.parseInt(path.substring("/api/resources/".length()));
                projectService.deleteResource(resourceId);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Resource deleted\"}");
                return;
            }
            if (path.startsWith("/api/budget/") && "GET".equals(method)) {
                int projectId = Integer.parseInt(path.substring("/api/budget/".length()));
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.budgetToJsonPublic(projectService.getProjectById(projectId).getBudget()));
                return;
            }
            if ("/api/expenses".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.expensesToJson(projectService.getAllExpenses()));
                return;
            }
            if ("/api/expenses".equals(path) && "POST".equals(method)) {
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                Expense expense = new Expense(
                        Integer.parseInt(payload.get("id")),
                        Integer.parseInt(payload.get("projectId")),
                        Double.parseDouble(payload.get("amount")),
                        payload.get("category"),
                        LocalDate.parse(payload.get("date"))
                );
                projectService.addExpense(expense);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_CREATED, "{\"message\":\"Expense created\"}");
                return;
            }
            if (path.startsWith("/api/expenses/") && "PUT".equals(method)) {
                int expenseId = Integer.parseInt(path.substring("/api/expenses/".length()));
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                Expense expense = new Expense(
                        expenseId,
                        Integer.parseInt(payload.get("projectId")),
                        Double.parseDouble(payload.get("amount")),
                        payload.get("category"),
                        LocalDate.parse(payload.get("date"))
                );
                projectService.updateExpense(expense);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Expense updated\"}");
                return;
            }
            if (path.startsWith("/api/expenses/") && "DELETE".equals(method)) {
                int expenseId = Integer.parseInt(path.substring("/api/expenses/".length()));
                projectService.deleteExpense(expenseId);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Expense deleted\"}");
                return;
            }
            if ("/api/milestones".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.milestonesToJson(projectService.getAllMilestones()));
                return;
            }
            if ("/api/milestones".equals(path) && "POST".equals(method)) {
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                Milestone milestone = new Milestone(
                        Integer.parseInt(payload.get("id")),
                        Integer.parseInt(payload.get("projectId")),
                        payload.get("name"),
                        LocalDate.parse(payload.get("targetDate")),
                        Boolean.parseBoolean(payload.getOrDefault("completed", "false"))
                );
                projectService.addMilestone(milestone);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_CREATED, "{\"message\":\"Milestone created\"}");
                return;
            }
            if (path.startsWith("/api/milestones/") && "PUT".equals(method)) {
                int milestoneId = Integer.parseInt(path.substring("/api/milestones/".length()));
                Map<String, String> payload = JsonUtil.parseBody(exchange);
                if (payload.isEmpty()) {
                    projectService.completeMilestone(milestoneId);
                } else {
                    Milestone milestone = new Milestone(
                            milestoneId,
                            Integer.parseInt(payload.get("projectId")),
                            payload.get("name"),
                            LocalDate.parse(payload.get("targetDate")),
                            Boolean.parseBoolean(payload.getOrDefault("completed", "false"))
                    );
                    projectService.updateMilestone(milestone);
                }
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Milestone completed\"}");
                return;
            }
            if (path.startsWith("/api/milestones/") && "DELETE".equals(method)) {
                int milestoneId = Integer.parseInt(path.substring("/api/milestones/".length()));
                projectService.deleteMilestone(milestoneId);
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, "{\"message\":\"Milestone deleted\"}");
                return;
            }
            if ("/api/monitoring".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.mapToJson(projectService.getMonitoringSummary()));
                return;
            }
            if ("/api/reports".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                        JsonUtil.mapToJson(projectService.getReportsSummary()));
                return;
            }

            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND, "{\"error\":\"Endpoint not found\"}");
        } catch (ProjectManagementException ex) {
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                    "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
        }
    }
}
