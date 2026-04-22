package com.vertex.pm.util;

import com.sun.net.httpserver.HttpExchange;
import com.vertex.pm.exception.ProjectManagementException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    public static Map<String, Object> parseBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return parseFlatJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    public static String errorJson(ProjectManagementException exception) {
        return toJson(Map.of(
                "error", exception.getMessage(),
                "type", exception.getType().name()
        ));
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Enum<?> enumValue) {
            return "\"" + escape(enumValue.name()) + "\"";
        }
        if (value instanceof TemporalAccessor) {
            return "\"" + escape(String.valueOf(value)) + "\"";
        }
        if (value instanceof Map<?, ?> map) {
            List<String> items = new ArrayList<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                items.add("\"" + escape(String.valueOf(entry.getKey())) + "\":" + toJson(entry.getValue()));
            }
            return "{" + String.join(",", items) + "}";
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> items = new ArrayList<>();
            for (Object item : iterable) {
                items.add(toJson(item));
            }
            return "[" + String.join(",", items) + "]";
        }
        if (value.getClass().isRecord()) {
            List<String> items = new ArrayList<>();
            try {
                for (Field field : value.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    items.add("\"" + escape(field.getName()) + "\":" + toJson(field.get(value)));
                }
            } catch (IllegalAccessException exception) {
                return "\"" + escape(String.valueOf(value)) + "\"";
            }
            return "{" + String.join(",", items) + "}";
        }
        if (!value.getClass().getName().startsWith("java.")) {
            List<String> items = new ArrayList<>();
            try {
                for (Field field : value.getClass().getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    items.add("\"" + escape(field.getName()) + "\":" + toJson(field.get(value)));
                }
            } catch (IllegalAccessException exception) {
                return "\"" + escape(String.valueOf(value)) + "\"";
            }
            return "{" + String.join(",", items) + "}";
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    public static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static Map<String, Object> parseFlatJson(String json) {
        Map<String, Object> values = new LinkedHashMap<>();
        String cleaned = json == null ? "" : json.trim();
        if (cleaned.isBlank() || "{}".equals(cleaned)) {
            return values;
        }
        if (cleaned.startsWith("{")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("}")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        for (String pair : splitTopLevel(cleaned)) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }
            String key = stripQuotes(keyValue[0].trim());
            String rawValue = keyValue[1].trim();
            values.put(key, parseValue(rawValue));
        }
        return values;
    }

    private static List<String> splitTopLevel(String value) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < value.length(); index++) {
            char currentChar = value.charAt(index);
            if (currentChar == '"' && (index == 0 || value.charAt(index - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (currentChar == ',' && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(currentChar);
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static Object parseValue(String rawValue) {
        if (rawValue == null || rawValue.equals("null")) {
            return null;
        }
        if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            return stripQuotes(rawValue);
        }
        if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue)) {
            return Boolean.parseBoolean(rawValue);
        }
        try {
            if (rawValue.contains(".")) {
                return Double.parseDouble(rawValue);
            }
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException ignored) {
            return stripQuotes(rawValue);
        }
    }

    private static String stripQuotes(String value) {
        String cleaned = value;
        if (cleaned.startsWith("\"")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }
}
