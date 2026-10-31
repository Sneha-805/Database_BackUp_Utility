package com.sneha.backup.restore;

import com.sneha.backup.config.DatabaseConfig;
import com.sneha.backup.database.DatabaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class RestoreService {
    private final DatabaseConfig databaseConfig;

    public RestoreService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public String getDatabaseName() {
        return databaseConfig.getDatabaseName();
    }

    public boolean databaseExists() throws SQLException {
        DatabaseConnection connectionProvider =
                new DatabaseConnection(databaseConfig);
        try (Connection connection = connectionProvider.getServerConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM information_schema.schemata "
                             + "WHERE schema_name = ?"
             )) {
            statement.setString(1, databaseConfig.getDatabaseName());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public void createDatabase() throws SQLException {
        DatabaseConnection connectionProvider =
                new DatabaseConnection(databaseConfig);
        String databaseName = databaseConfig.getDatabaseName()
                .replace("`", "``");
        try (Connection connection = connectionProvider.getServerConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE DATABASE IF NOT EXISTS `" + databaseName + "`"
            );
        }
    }

    public void restoreBackup(String backupFilePath)
            throws IOException, InterruptedException, SQLException {
        Path backupPath = Paths.get(backupFilePath);
        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file not found: " + backupFilePath);
        }

        if (!databaseExists()) {
            throw new SQLException(
                    "Destination database does not exist: "
                            + databaseConfig.getDatabaseName()
            );
        }

        boolean isCompressed = backupFilePath
                .toLowerCase(Locale.ROOT)
                .endsWith(".gz");
        ProcessBuilder processBuilder = new ProcessBuilder(
                databaseConfig.getMysqlPath(),
                "--host=" + databaseConfig.getHost(),
                "--port=" + databaseConfig.getPort(),
                "--user=" + databaseConfig.getUsername(),
                "--database=" + databaseConfig.getDatabaseName()
        );
        processBuilder.environment().put(
                "MYSQL_PWD",
                databaseConfig.getPassword()
        );
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();
        try (
                InputStream inputStream = isCompressed
                        ? new GZIPInputStream(Files.newInputStream(backupPath))
                        : Files.newInputStream(backupPath);
                OutputStream outputStream = process.getOutputStream()
        ) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            process.destroyForcibly();
            throw e;
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Database restore failed. Exit code: "
                    + exitCode);
        }
        System.out.println("Database restored successfully!");
    }
}