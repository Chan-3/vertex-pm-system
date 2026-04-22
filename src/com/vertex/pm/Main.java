package com.vertex.pm;

import com.sun.net.httpserver.HttpServer;
import com.vertex.pm.controller.ProjectController;
import com.vertex.pm.controller.SystemController;
import com.vertex.pm.factory.RepositoryFactory;
import com.vertex.pm.service.ProjectService;
import com.vertex.pm.util.AppLogger;
import com.vertex.pm.util.EnvConfig;
import com.vertex.pm.util.StaticFileHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(EnvConfig.get("PM_PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ProjectService projectService = new ProjectService(RepositoryFactory.getProjectRepository());

        server.createContext("/api/projects", new ProjectController(projectService));
        server.createContext("/api/projects/", new ProjectController(projectService));
        server.createContext("/api/dashboard", new SystemController(projectService));
        server.createContext("/api/monitoring", new SystemController(projectService));
        server.createContext("/api/tasks", new SystemController(projectService));
        server.createContext("/api/tasks/", new SystemController(projectService));
        server.createContext("/api/resources", new SystemController(projectService));
        server.createContext("/api/resources/", new SystemController(projectService));
        server.createContext("/api/milestones", new SystemController(projectService));
        server.createContext("/api/milestones/", new SystemController(projectService));
        server.createContext("/api/expenses", new SystemController(projectService));
        server.createContext("/api/expenses/", new SystemController(projectService));
        server.createContext("/", new StaticFileHandler(Path.of("ui")));
        server.setExecutor(null);
        server.start();

        AppLogger.info("Vertex PM server started at http://localhost:" + port);
        System.out.println("Vertex PM server started at http://localhost:" + port);
    }
}
