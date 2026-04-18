package com.vertex.pm.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides centralized application logging.
 */
public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("vertex-pm");

    private AppLogger() {
    }

    /**
     * Logs a warning with a stack trace.
     */
    public static void warning(String message, Throwable throwable) {
        LOGGER.log(Level.WARNING, message, throwable);
    }

    /**
     * Logs an informational message.
     */
    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }
}
