package com.sneha.backup;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Path;
import java.util.List;

import com.sneha.backup.backup.BackupService;
import com.sneha.backup.restore.RestoreService;
import com.sneha.backup.backup.BackupListService;


public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        BackupService backupService = new BackupService();
        while(true) {
            System.out.println("================================");
            System.out.println("     DATABASE BACKUP UTILITY    ");
            System.out.println("================================");
            System.out.println("1. Create Backup");
            System.out.println("2. Restore Backup");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
                continue;
            }
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Create Backup selected");
                    backupService.createBackup();
                    break;

                case 2:
                    System.out.println("Restore Backup selected");
                    BackupListService backupListService =
                            new BackupListService();

                    List<Path> backupFiles =
                            backupListService.listBackups();
                    if (backupFiles.isEmpty()) {
                        System.out.println("No backup files found.");
                        break;
                    }

                    System.out.println("\nAvailable Backups:");

                    for (int i = 0; i < backupFiles.size(); i++) {
                        System.out.println(
                                (i + 1) + ". " + backupFiles.get(i).getFileName()
                        );
                    }
                    System.out.print("Enter backup number: ");
                    int backupChoice = scanner.nextInt();
                    if (backupChoice < 1 || backupChoice > backupFiles.size()) {
                        System.out.println("Invalid backup number.");
                        break;
                    }
                    Path selectedBackup =
                            backupFiles.get(backupChoice - 1);

                    System.out.println(
                            "Selected backup: " + selectedBackup.getFileName()
                    );
                    System.out.print(
                            "Are you sure you want to restore this backup? (y/n): "
                    );

                    String confirmation = scanner.next();

                    if (!confirmation.equalsIgnoreCase("y")) {
                        System.out.println("Restore cancelled.");
                        break;
                    }
                    RestoreService restoreService = new RestoreService();

                    restoreService.restoreBackup(selectedBackup.toString());
                    break;

                case 3:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please enter 1, 2, or 3."
                    );
                    break;
            }
        }


    }
}