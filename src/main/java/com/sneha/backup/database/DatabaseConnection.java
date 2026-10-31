package com.sneha.backup.database;

import com.sneha.backup.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private final DatabaseConfig databaseConfig;

    public DatabaseConnection(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                databaseConfig.getUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }

    public Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + databaseConfig.getHost()
                        + ":" + databaseConfig.getPort() + "/",
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }
}