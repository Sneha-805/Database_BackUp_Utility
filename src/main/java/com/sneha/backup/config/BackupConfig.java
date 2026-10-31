package com.sneha.backup.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BackupConfig {

    private static final String DEFAULT_BACKUP_DIRECTORY = "backups";
    private static final int DEFAULT_RETENTION_COUNT = 10;

    public static Path getBackupDirectory() {
        String value = System.getenv("BACKUP_DIRECTORY");
        if (value == null || value.isBlank()) {
            value = DEFAULT_BACKUP_DIRECTORY;
        }
        return Paths.get(value).toAbsolutePath().normalize();
    }

    public static int getRetentionCount() {
        String value = System.getenv("BACKUP_RETENTION_COUNT");
        if (value == null || value.isBlank()) {
            return DEFAULT_RETENTION_COUNT;
        }

        try {
            int count = Integer.parseInt(value);
            if (count < 1) {
                throw new IllegalArgumentException(
                        "BACKUP_RETENTION_COUNT must be at least 1."
                );
            }
            return count;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "BACKUP_RETENTION_COUNT must be a positive integer.",
                    e
            );
        }
    }
}