package com.vertex.pm.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves frontend files from the ui directory.
 */
public class StaticFileHandler implements HttpHandler {
    private final Path uiRoot;

    /**
     * Creates the static file handler.
     */
    public StaticFileHandler(Path uiRoot) {
        this.uiRoot = uiRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if ("/".equals(requestPath)) {
            requestPath = "/index.html";
        }
        Path filePath = uiRoot.resolve(requestPath.substring(1)).normalize();
        if (!filePath.startsWith(uiRoot) || Files.notExists(filePath)) {
            byte[] payload = "Not Found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, payload.length);
            exchange.getResponseBody().write(payload);
            exchange.close();
            return;
        }
        String contentType = requestPath.endsWith(".css") ? "text/css; charset=utf-8"
                : requestPath.endsWith(".js") ? "application/javascript; charset=utf-8"
                : "text/html; charset=utf-8";
        byte[] payload = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }
}
