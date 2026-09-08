package com.sneha.backup;

import com.sneha.backup.backup.BackupService;

public class Main {

    public static void main(String[] args) {

        System.out.println(
                "Database Backup Utility"
        );

        BackupService backupService =
                new BackupService();

        backupService.createBackup();
    }
}