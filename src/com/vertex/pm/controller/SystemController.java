package com.vertex.pm.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vertex.pm.exception.ExceptionType;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Task;
import com.vertex.pm.model.TaskStatus;
import com.vertex.pm.service.ProjectService;
import com.vertex.pm.util.AppLogger;
import com.vertex.pm.util.IdGenerator;
import com.vertex.pm.util.JsonUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.Map;

public class SystemController implements HttpHandler {
    // GRASP Controller: this class routes dashboard, task, resource, milestone, and expense requests.
    private final ProjectService projectService;

    public SystemController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    /**
     * Handles non-project routes such as dashboard, monitoring, tasks, resources,
     * milestones, and expenses.
     */
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase();
            AppLogger.info(method + " " + path);

            if ("/api/dashboard".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.getDashboardSummary()));
                return;
            }
            if ("/api/monitoring".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.getMonitoringSummary()));
                return;
            }
            if ("/api/tasks".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.getAllTasks()));
                return;
            }
            if ("/api/tasks".equals(path) && "POST".equals(method)) {
                Map<String, Object> body = JsonUtil.parseBody(exchange);
                Task task = new Task(
                        IdGenerator.next("TSK"),
                        stringValue(body.get("projectId")),
                        stringValue(body.get("name")),
                        stringValue(body.get("description")),
                        LocalDate.parse(stringValue(body.get("startDate"))),
                        LocalDate.parse(stringValue(body.get("dueDate"))),
                        TaskStatus.valueOf(stringValue(body.getOrDefault("status", "PLANNED"))),
                        stringValue(body.get("priority")),
                        stringValue(body.get("assignedTo"))
                );
                JsonUtil.sendJson(exchange, 201, JsonUtil.toJson(projectService.createTask(task)));
                return;
            }
            if (path.matches("/api/tasks/[^/]+") && "PATCH".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.patchTask(path.substring("/api/tasks/".length()), JsonUtil.parseBody(exchange))));
                return;
            }
            if (path.matches("/api/tasks/[^/]+") && "DELETE".equals(method)) {
                projectService.deleteTask(path.substring("/api/tasks/".length()));
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(Map.of("message", "Task deleted")));
                return;
            }
            if ("/api/resources".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.getAllResources()));
                return;
            }
            if ("/api/resources".equals(path) && "POST".equals(method)) {
                Map<String, Object> body = JsonUtil.parseBody(exchange);
                Resource resource = new Resource(
                        IdGenerator.next("RES"),
                        stringValue(body.get("projectId")),
                        stringValue(body.get("name")),
                        stringValue(body.get("role")),
                        booleanValue(body.getOrDefault("availability", true)),
                        stringValue(body.get("skillSet"))
                );
                JsonUtil.sendJson(exchange, 201, JsonUtil.toJson(projectService.createResource(resource)));
                return;
            }
            if (path.matches("/api/resources/[^/]+") && "PATCH".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.patchResource(path.substring("/api/resources/".length()), JsonUtil.parseBody(exchange))));
                return;
            }
            if (path.matches("/api/resources/[^/]+") && "DELETE".equals(method)) {
                projectService.deleteResource(path.substring("/api/resources/".length()));
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(Map.of("message", "Resource deleted")));
                return;
            }
            if ("/api/milestones".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.getAllMilestones()));
                return;
            }
            if ("/api/milestones".equals(path) && "POST".equals(method)) {
                Map<String, Object> body = JsonUtil.parseBody(exchange);
                Milestone milestone = new Milestone(
                        IdGenerator.next("MLS"),
                        stringValue(body.get("projectId")),
                        stringValue(body.get("name")),
                        LocalDate.parse(stringValue(body.get("targetDate"))),
                        booleanValue(body.getOrDefault("completionStatus", false)),
                        stringValue(body.get("description"))
                );
                JsonUtil.sendJson(exchange, 201, JsonUtil.toJson(projectService.createMilestone(milestone)));
                return;
            }
            if (path.matches("/api/milestones/[^/]+") && "PATCH".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.patchMilestone(path.substring("/api/milestones/".length()), JsonUtil.parseBody(exchange))));
                return;
            }
            if (path.matches("/api/milestones/[^/]+") && "DELETE".equals(method)) {
                projectService.deleteMilestone(path.substring("/api/milestones/".length()));
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(Map.of("message", "Milestone deleted")));
                return;
            }
            if ("/api/expenses".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.getAllExpenses()));
                return;
            }
            if ("/api/expenses".equals(path) && "POST".equals(method)) {
                Map<String, Object> body = JsonUtil.parseBody(exchange);
                Expense expense = new Expense(
                        IdGenerator.next("EXP"),
                        stringValue(body.get("projectId")),
                        LocalDate.parse(stringValue(body.get("expenseDate"))),
                        stringValue(body.get("description")),
                        stringValue(body.get("category")),
                        doubleValue(body.get("amount"))
                );
                JsonUtil.sendJson(exchange, 201, JsonUtil.toJson(projectService.createExpense(expense)));
                return;
            }
            if (path.matches("/api/expenses/[^/]+") && "PATCH".equals(method)) {
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(projectService.patchExpense(path.substring("/api/expenses/".length()), JsonUtil.parseBody(exchange))));
                return;
            }
            if (path.matches("/api/expenses/[^/]+") && "DELETE".equals(method)) {
                projectService.deleteExpense(path.substring("/api/expenses/".length()));
                JsonUtil.sendJson(exchange, 200, JsonUtil.toJson(Map.of("message", "Expense deleted")));
                return;
            }

            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                    JsonUtil.toJson(Map.of("error", "Endpoint not found", "type", ExceptionType.INVALID_REQUEST.name())));
        } catch (ProjectManagementException exception) {
            AppLogger.warning("System endpoint failed.", exception);
            JsonUtil.sendJson(exchange, exception.getStatusCode(), JsonUtil.errorJson(exception));
        } catch (Exception exception) {
            AppLogger.error("Unexpected system endpoint failure.", exception);
            JsonUtil.sendJson(exchange, 500, JsonUtil.toJson(Map.of("error", exception.getMessage(), "type", ExceptionType.DATABASE_ERROR.name())));
        }
    }

    /** Normalizes request values into strings before model creation. */
    private String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
    /** Normalizes numeric request values used for money-related fields. */
    private double doubleValue(Object value) { return value instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(value)); }
    /** Parses request values into booleans for flags like availability and completion status. */
    private boolean booleanValue(Object value) { return value instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(value)); }
}
