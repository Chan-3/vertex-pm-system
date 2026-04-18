package com.vertex.pm.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Budget;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.ProjectStatus;
import com.vertex.pm.service.ProjectService;
import com.vertex.pm.util.JsonUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.Map;

/**
 * Handles project-related HTTP requests.
 * GRASP Controller: it receives UI requests and delegates to the service layer.
 */
public class ProjectController implements HttpHandler {
    private final ProjectService projectService;

    /**
     * Creates the controller.
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleGet(exchange);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePost(exchange);
                return;
            }
            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePut(exchange);
                return;
            }
            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleDelete(exchange);
                return;
            }
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                    "{\"error\":\"Unsupported method\"}");
        } catch (ProjectManagementException ex) {
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                    "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
        }
    }

    /**
     * Handles GET requests for projects.
     */
    private void handleGet(HttpExchange exchange) throws IOException, ProjectManagementException {
        String path = exchange.getRequestURI().getPath();
        if ("/api/projects".equals(path)) {
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                    JsonUtil.projectsToJson(projectService.getAllProjects()));
            return;
        }
        if (path.startsWith("/api/projects/")) {
            int projectId = Integer.parseInt(path.substring("/api/projects/".length()));
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                    JsonUtil.projectToJson(projectService.getProjectById(projectId)));
            return;
        }
        JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND, "{\"error\":\"Endpoint not found\"}");
    }

    /**
     * Handles project creation requests.
     */
    private void handlePost(HttpExchange exchange) throws IOException, ProjectManagementException {
        if (!"/api/projects".equals(exchange.getRequestURI().getPath())) {
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND, "{\"error\":\"Endpoint not found\"}");
            return;
        }
        Map<String, String> payload = JsonUtil.parseBody(exchange);
        Project project = new Project(
                Integer.parseInt(payload.getOrDefault("id", "0")),
                payload.get("name"),
                payload.getOrDefault("description", ""),
                LocalDate.parse(payload.get("startDate")),
                LocalDate.parse(payload.get("endDate")),
                ProjectStatus.valueOf(payload.getOrDefault("status", "PLANNED")),
                new Budget(Integer.parseInt(payload.getOrDefault("id", "0")),
                        Double.parseDouble(payload.getOrDefault("budget", "0")))
        );
        projectService.createProject(project);
        JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_CREATED,
                "{\"message\":\"Project created successfully\"}");
    }

    /**
     * Handles project update requests.
     */
    private void handlePut(HttpExchange exchange) throws IOException, ProjectManagementException {
        if (!exchange.getRequestURI().getPath().startsWith("/api/projects/")) {
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND, "{\"error\":\"Endpoint not found\"}");
            return;
        }
        int projectId = Integer.parseInt(exchange.getRequestURI().getPath().substring("/api/projects/".length()));
        Map<String, String> payload = JsonUtil.parseBody(exchange);
        Project project = new Project(
                projectId,
                payload.get("name"),
                payload.getOrDefault("description", ""),
                LocalDate.parse(payload.get("startDate")),
                LocalDate.parse(payload.get("endDate")),
                ProjectStatus.valueOf(payload.getOrDefault("status", "PLANNED")),
                new Budget(projectId, Double.parseDouble(payload.getOrDefault("budget", "0")))
        );
        projectService.updateProject(project);
        JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                "{\"message\":\"Project updated successfully\"}");
    }

    /**
     * Handles project delete requests.
     */
    private void handleDelete(HttpExchange exchange) throws IOException, ProjectManagementException {
        if (!exchange.getRequestURI().getPath().startsWith("/api/projects/")) {
            JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_NOT_FOUND, "{\"error\":\"Endpoint not found\"}");
            return;
        }
        int projectId = Integer.parseInt(exchange.getRequestURI().getPath().substring("/api/projects/".length()));
        projectService.deleteProject(projectId);
        JsonUtil.sendJson(exchange, HttpURLConnection.HTTP_OK,
                "{\"message\":\"Project deleted successfully\"}");
    }
}
