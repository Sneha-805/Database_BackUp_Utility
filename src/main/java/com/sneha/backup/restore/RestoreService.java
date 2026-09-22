package com.sneha.backup.restore;

import com.sneha.backup.config.DatabaseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class RestoreService{
    public void restoreBackup(String backupFilePath) throws IOException, InterruptedException{
        Path backupPath=Paths.get(backupFilePath);
        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file not found: " + backupFilePath);
        }
        boolean isCompressed =
                backupFilePath.toLowerCase().endsWith(".gz");
        InputStream inputStream;
        if (isCompressed) {
            inputStream =
                    new GZIPInputStream(
                            Files.newInputStream(backupPath)
                    );
        } else {
            inputStream =
                    Files.newInputStream(backupPath);
        }
        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        DatabaseConfig.MYSQL_PATH,
                        "-u",
                        DatabaseConfig.USERNAME,
                        "-p"+ DatabaseConfig.PASSWORD,
                        DatabaseConfig.DATABASE
                );
        Process process = processBuilder.start();

        OutputStream outputStream =
                process.getOutputStream();
        try(inputStream;outputStream) {
            inputStream.transferTo(outputStream);
        }
        int exitCode =
                process.waitFor();
        if (exitCode == 0) {
            System.out.println("Database restored successfully!");
        } else {
            System.out.println("Database restore failed!");
        }
    }
}