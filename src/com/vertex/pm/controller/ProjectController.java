package com.vertex.pm.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vertex.pm.exception.ExceptionType;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Budget;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.ProjectStatus;
import com.vertex.pm.service.ProjectService;
import com.vertex.pm.util.AppLogger;
import com.vertex.pm.util.IdGenerator;
import com.vertex.pm.util.JsonUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.Map;

public class ProjectController implements HttpHandler {
    // GRASP Controller: this class receives UI/API requests and delegates work to the service layer.
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase();
            AppLogger.info(method + " " + path);

            if ("/api/projects".equals(path) && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, JsonUtil.toJson(projectService.getAllProjects()));
                return;
            }
            if ("/api/projects".equals(path) && "POST".equals(method)) {
                Map<String, Object> body = JsonUtil.parseBody(exchange);
                String projectId = IdGenerator.next("PRJ");
                Project project = new Project(
                        projectId,
                        stringValue(body.get("name")),
                        stringValue(body.get("description")),
                        stringValue(body.get("managerName")),
                        LocalDate.parse(stringValue(body.get("startDate"))),
                        LocalDate.parse(stringValue(body.get("endDate"))),
                        ProjectStatus.valueOf(stringValue(body.getOrDefault("status", "PLANNED"))),
                        stringValue(body.get("objectives")),
                        intValue(body.getOrDefault("progressPercent", 0)),
                        new Budget(projectId, doubleValue(body.getOrDefault("budgetTotal", 0)), doubleValue(body.getOrDefault("budgetSpent", 0)))
                );
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_CREATED, JsonUtil.toJson(projectService.createProject(project)));
                return;
            }
            if (path.matches("/api/projects/[^/]+") && "GET".equals(method)) {
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, JsonUtil.toJson(projectService.getProjectById(path.substring("/api/projects/".length()))));
                return;
            }
            if (path.matches("/api/projects/[^/]+") && "PATCH".equals(method)) {
                String projectId = path.substring("/api/projects/".length());
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, JsonUtil.toJson(projectService.patchProject(projectId, JsonUtil.parseBody(exchange))));
                return;
            }
            if (path.matches("/api/projects/[^/]+/progress") && "PATCH".equals(method)) {
                String projectId = path.substring("/api/projects/".length(), path.length() - "/progress".length());
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, JsonUtil.toJson(projectService.patchProjectProgress(projectId, JsonUtil.parseBody(exchange))));
                return;
            }
            if (path.matches("/api/projects/[^/]+/report") && "GET".equals(method)) {
                String projectId = path.substring("/api/projects/".length(), path.length() - "/report".length());
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, JsonUtil.toJson(projectService.getProjectReport(projectId)));
                return;
            }
            if (path.matches("/api/projects/[^/]+") && "DELETE".equals(method)) {
                projectService.deleteProject(path.substring("/api/projects/".length()));
                JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK, JsonUtil.toJson(Map.of("message", "Project deleted")));
                return;
            }

            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                    JsonUtil.toJson(Map.of("error", "Endpoint not found", "type", ExceptionType.INVALID_REQUEST.name())));
        } catch (ProjectManagementException exception) {
            AppLogger.warning("Project endpoint failed.", exception);
            JsonUtil.sendJson(exchange, exception.getStatusCode(), JsonUtil.errorJson(exception));
        } catch (Exception exception) {
            AppLogger.error("Unexpected project endpoint failure.", exception);
            JsonUtil.sendJson(exchange, 500, JsonUtil.toJson(Map.of("error", exception.getMessage(), "type", ExceptionType.DATABASE_ERROR.name())));
        }
    }

    private String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
    private int intValue(Object value) { return value instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(value)); }
    private double doubleValue(Object value) { return value instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(value)); }
}
