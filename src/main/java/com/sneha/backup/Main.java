package com.sneha.backup;

import com.sneha.backup.backup.BackupListService;
import com.sneha.backup.backup.BackupService;
import com.sneha.backup.config.BackupConfig;
import com.sneha.backup.config.ConfigManager;
import com.sneha.backup.config.DatabaseConfig;
import com.sneha.backup.restore.RestoreService;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DatabaseConfig databaseConfig =
                ConfigManager.getDatabaseConfig(scanner);

        if (databaseConfig == null) {
            System.out.println("Unable to configure the database.");
            scanner.close();
            return;
        }

        try {
            BackupConfig.getBackupDirectory();
            BackupConfig.getRetentionCount();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid backup configuration: " + e.getMessage());
            scanner.close();
            return;
        }

        BackupService backupService = new BackupService(databaseConfig);
        BackupListService backupListService = new BackupListService();
        RestoreService restoreService = new RestoreService(databaseConfig);
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor(runnable -> {
                    Thread thread = new Thread(runnable, "scheduled-backups");
                    thread.setDaemon(true);
                    return thread;
                });
        ScheduledFuture<?> scheduledBackup = null;

        while (true) {
            System.out.println();
            System.out.println("================================");
            System.out.println("     DATABASE BACKUP UTILITY    ");
            System.out.println("================================");
            System.out.println("1. Create Backup");
            System.out.println("2. Restore Backup");
            System.out.println("3. Start Scheduled Backups");
            System.out.println("4. Stop Scheduled Backups");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                if (scanner.hasNext()) {
                    scanner.next();
                } else {
                    break;
                }
                continue;
            }

            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> backupService.createBackup();
                case 2 -> restoreBackup(
                        scanner,
                        backupListService,
                        restoreService
                );
                case 3 -> {
                    if (scheduledBackup != null && !scheduledBackup.isDone()) {
                        System.out.println("Scheduled backups are already running.");
                        break;
                    }
                    System.out.print("Run a backup every how many minutes? ");
                    if (!scanner.hasNextLong()) {
                        System.out.println("Invalid interval. Enter a positive number of minutes.");
                        if (scanner.hasNext()) {
                            scanner.next();
                        }
                        break;
                    }
                    long intervalMinutes = scanner.nextLong();
                    if (intervalMinutes < 1) {
                        System.out.println("The interval must be at least one minute.");
                        break;
                    }
                    scheduledBackup = scheduler.scheduleAtFixedRate(
                            () -> backupService.createBackup(),
                            intervalMinutes,
                            intervalMinutes,
                            TimeUnit.MINUTES
                    );
                    System.out.println(
                            "Scheduled backups will run every "
                                    + intervalMinutes
                                    + " minute(s) while this application stays open."
                    );
                }
                case 4 -> {
                    if (scheduledBackup == null || scheduledBackup.isDone()) {
                        System.out.println("No scheduled backups are running.");
                    } else {
                        scheduledBackup.cancel(false);
                        scheduledBackup = null;
                        System.out.println("Scheduled backups stopped.");
                    }
                }
                case 5 -> {
                    if (scheduledBackup != null) {
                        scheduledBackup.cancel(false);
                    }
                    scheduler.shutdownNow();
                    System.out.println("Exiting Database Backup Utility...");
                    scanner.close();
                    return;
                }
                default -> System.out.println(
                        "Invalid choice. Please enter a number from 1 to 5."
                );
            }
        }

        if (scheduledBackup != null) {
            scheduledBackup.cancel(false);
        }
        scheduler.shutdownNow();
        scanner.close();
    }

    private static void restoreBackup(
            Scanner scanner,
            BackupListService backupListService,
            RestoreService restoreService
    ) {
        try {
            List<Path> backupFiles = backupListService.listBackups();
            if (backupFiles.isEmpty()) {
                System.out.println("No backup files found.");
                return;
            }

            System.out.println();
            System.out.println("Available Backups:");
            for (int i = 0; i < backupFiles.size(); i++) {
                System.out.println(
                        (i + 1) + ". " + backupFiles.get(i).getFileName()
                );
            }
            System.out.print("Enter backup number: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                if (scanner.hasNext()) {
                    scanner.next();
                }
                return;
            }

            int backupChoice = scanner.nextInt();
            if (backupChoice < 1 || backupChoice > backupFiles.size()) {
                System.out.println("Invalid backup selection.");
                return;
            }

            Path selectedBackup = backupFiles.get(backupChoice - 1);
            System.out.println("Selected backup: " + selectedBackup.getFileName());
            System.out.print(
                    "Restoring can modify database contents. Continue? (y/n): "
            );
            if (!scanner.next().equalsIgnoreCase("y")) {
                System.out.println("Restore cancelled.");
                return;
            }

            if (!restoreService.databaseExists()) {
                System.out.println(
                        "Destination database does not exist: "
                                + restoreService.getDatabaseName()
                );
                System.out.print("Create it before restoring? (y/n): ");
                if (!scanner.next().equalsIgnoreCase("y")) {
                    System.out.println("Restore cancelled.");
                    return;
                }
                restoreService.createDatabase();
            }

            restoreService.restoreBackup(selectedBackup.toString());
        } catch (IOException | SQLException e) {
            System.out.println("Restore failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Restore was interrupted.");
        }
    }
}
