package com.sneha.backup;
import java.io.IOException;
import com.sneha.backup.backup.BackupService;

import com.sneha.backup.restore.RestoreService;

public class Main {

    public static void main(String[] args) throws IOException,InterruptedException {

        System.out.println(
                "Database Backup Utility"
        );

        BackupService backupService = new BackupService();

        backupService.createBackup();

        RestoreService restoreService = new RestoreService();
        String backupFilePath = "backups/backup_2026-09-12_215355.sql.gz";
        restoreService.restoreBackup(backupFilePath);
    }
}