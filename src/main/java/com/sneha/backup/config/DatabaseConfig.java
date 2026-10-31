package com.sneha.backup.config;

public class DatabaseConfig {

    private final String host;
    private final String port;
    private final String databaseName;
    private final String username;
    private final String password;
    private final String mysqlPath;
    private final String mysqldumpPath;

    public DatabaseConfig(
            String host,
            String port,
            String databaseName,
            String username,
            String password,
            String mysqlPath,
            String mysqldumpPath
    ) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.mysqlPath = mysqlPath;
        this.mysqldumpPath = mysqldumpPath;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMysqlPath() {
        return mysqlPath;
    }

    public String getMysqldumpPath() {
        return mysqldumpPath;
    }

    public String getUrl() {
        return "jdbc:mysql://"
                + host
                + ":"
                + port
                + "/"
                + databaseName;
    }
}