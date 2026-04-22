package com.vertex.pm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnectionManager {
    // Singleton Pattern: one shared access point is used for database connection configuration.
    private static final DatabaseConnectionManager INSTANCE = new DatabaseConnectionManager();
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnectionManager() {
        this.url = "jdbc:mysql://" + EnvConfig.get("DB_HOST", "localhost") + ":"
                + EnvConfig.get("DB_PORT", "3306") + "/"
                + EnvConfig.get("DB_NAME", "pm_db")
                + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        this.username = EnvConfig.get("DB_USER", "root");
        this.password = EnvConfig.get("DB_PASSWORD", "");
    }

    public static DatabaseConnectionManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
