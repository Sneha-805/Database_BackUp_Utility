package com.sneha.backup.config;

public class DatabaseConfig {

    public static final String URL =
            "jdbc:mysql://localhost:3306/backup_demo";

    public static final String DATABASE_NAME =
            "backup_demo";

    public static final String USERNAME =
            "root";

    public static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static final String DATABASE =
            "backup_demo";
    public static final String MYSQL_PATH =
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
}