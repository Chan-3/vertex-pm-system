package com.vertex.pm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connectivity.
 * Singleton pattern: one shared connection manager for the application.
 */
public final class DatabaseConnectionManager {
    private static final DatabaseConnectionManager INSTANCE = new DatabaseConnectionManager();
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates the connection manager using environment variables or defaults.
     */
    private DatabaseConnectionManager() {
        this.url = EnvConfig.get("PM_DB_URL",
                "jdbc:mysql://" + EnvConfig.get("DB_HOST", "localhost") + ":"
                        + EnvConfig.get("DB_PORT", "3306") + "/"
                        + EnvConfig.get("DB_NAME", "vertex_pm"));
        this.username = EnvConfig.get("PM_DB_USER", EnvConfig.get("DB_USER", "root"));
        this.password = EnvConfig.get("PM_DB_PASSWORD", EnvConfig.get("DB_PASSWORD", "root"));
    }

    /**
     * Returns the singleton instance.
     */
    public static DatabaseConnectionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Attempts to open a database connection.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Checks whether the local database is available.
     */
    public boolean isDatabaseAvailable() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException ex) {
            AppLogger.warning("Local MySQL database is unavailable. API fallback will be used.", ex);
            return false;
        }
    }
}
