package com.sneha.backup.backup;

import com.sneha.backup.config.BackupConfig;
import com.sneha.backup.config.DatabaseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BackupService {

    private final DatabaseConfig databaseConfig;

    public BackupService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public boolean createBackup() {
        Path backupFile = null;
        Process process = null;

        try {
            Path backupDirectory = BackupConfig.getBackupDirectory();
            Files.createDirectories(backupDirectory);

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss_SSS")
            );
            backupFile = backupDirectory.resolve(
                    "backup_" + timestamp + ".sql.gz"
            );

            ProcessBuilder processBuilder = new ProcessBuilder(
                    databaseConfig.getMysqldumpPath(),
                    "--host=" + databaseConfig.getHost(),
                    "--port=" + databaseConfig.getPort(),
                    "--user=" + databaseConfig.getUsername(),
                    "--",
                    databaseConfig.getDatabaseName()
            );
            processBuilder.environment().put(
                    "MYSQL_PWD",
                    databaseConfig.getPassword()
            );
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            System.out.println("Creating database backup...");
            process = processBuilder.start();
            try (
                    InputStream inputStream = process.getInputStream();
                    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
                            Files.newOutputStream(
                                    backupFile,
                                    StandardOpenOption.CREATE_NEW,
                                    StandardOpenOption.WRITE
                            )
                    )
            ) {
                inputStream.transferTo(gzipOutputStream);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Backup failed. Exit code: " + exitCode);
                removeIncompleteBackup(backupFile);
                return false;
            }

            verifyCompressedBackup(backupFile);
            try {
                new BackupListService(backupDirectory).pruneBackups(
                        BackupConfig.getRetentionCount()
                );
            } catch (IOException e) {
                System.out.println(
                        "Backup was created, but retention cleanup failed: "
                                + e.getMessage()
                );
            }

            System.out.println("Backup completed successfully!");
            System.out.println("Backup file: " + backupFile.toAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("Error while creating or verifying backup: "
                    + e.getMessage());
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            removeIncompleteBackup(backupFile);
            return false;
        } catch (InterruptedException e) {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            removeIncompleteBackup(backupFile);
            Thread.currentThread().interrupt();
            System.out.println("Backup process was interrupted.");
            return false;
        }
    }

    private static void verifyCompressedBackup(Path backupFile)
            throws IOException {
        try (
                InputStream fileInput = Files.newInputStream(backupFile);
                GZIPInputStream gzipInput = new GZIPInputStream(fileInput)
        ) {
            gzipInput.transferTo(OutputStream.nullOutputStream());
        }
    }

    private static void removeIncompleteBackup(Path backupFile) {
        if (backupFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(backupFile);
        } catch (IOException e) {
            System.out.println("Could not remove incomplete backup "
                    + backupFile + ": " + e.getMessage());
        }
    }
}