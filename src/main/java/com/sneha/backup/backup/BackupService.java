package com.sneha.backup.backup;

import com.sneha.backup.config.BackupConfig;
import com.sneha.backup.config.DatabaseConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BackupService {

    public void createBackup() {

        try {

            // 1. Create the backup directory
            Path backupDirectory =
                    Paths.get(BackupConfig.BACKUP_DIRECTORY);

            Files.createDirectories(backupDirectory);

            // 2. Create the backup file
            Path backupFile =
                    backupDirectory.resolve("backup.sql");

            // 3. Build the mysqldump command
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            BackupConfig.MYSQLDUMP_PATH,
                            "-u",
                            DatabaseConfig.USERNAME,
                            DatabaseConfig.DATABASE_NAME
                    );

            // 4. Pass the password through the process environment
            processBuilder.environment().put(
                    "MYSQL_PWD",
                    DatabaseConfig.PASSWORD
            );

            // 5. Send mysqldump output directly to the backup file
            processBuilder.redirectOutput(
                    backupFile.toFile()
            );

            // 6. Show errors in IntelliJ console
            processBuilder.redirectError(
                    ProcessBuilder.Redirect.INHERIT
            );

            System.out.println(
                    "Creating database backup..."
            );

            // 7. Start mysqldump
            Process process =
                    processBuilder.start();

            // 8. Wait until mysqldump finishes
            int exitCode =
                    process.waitFor();

            // 9. Check whether backup succeeded
            if (exitCode == 0) {

                System.out.println(
                        "Backup completed successfully!"
                );

                System.out.println(
                        "Backup file: "
                                + backupFile.toAbsolutePath()
                );

            } else {

                System.out.println(
                        "Backup failed. Exit code: "
                                + exitCode
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Error while creating backup."
            );

            e.printStackTrace();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    "Backup process was interrupted."
            );
        }
    }
}