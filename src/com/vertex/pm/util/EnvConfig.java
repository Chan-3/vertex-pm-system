package com.vertex.pm.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads environment configuration from process variables and optional .env file.
 */
public final class EnvConfig {
    private static final Map<String, String> VALUES = loadValues();

    private EnvConfig() {
    }

    /**
     * Returns a configuration value with fallback.
     */
    public static String get(String key, String fallback) {
        return VALUES.getOrDefault(key, fallback);
    }

    /** Loads values from system environment first and then fills missing keys from .env. */
    private static Map<String, String> loadValues() {
        Map<String, String> values = new HashMap<>(System.getenv());
        Path envFile = Path.of(".env");
        if (Files.exists(envFile)) {
            try {
                List<String> lines = Files.readAllLines(envFile);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isBlank() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                        continue;
                    }
                    String[] parts = trimmed.split("=", 2);
                    values.putIfAbsent(parts[0].trim(), parts[1].trim());
                }
            } catch (IOException ex) {
                AppLogger.warning("Unable to read .env configuration file.", ex);
            }
        }
        return values;
    }
}
