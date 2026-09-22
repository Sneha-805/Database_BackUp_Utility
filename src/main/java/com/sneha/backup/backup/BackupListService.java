package com.sneha.backup.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BackupListService {
    public List<Path> listBackups() throws IOException {

        Path backupDirectory = Paths.get("backups");

        if (!Files.exists(backupDirectory)) {
            return new ArrayList<>();
        }

        try (var files = Files.list(backupDirectory)) {

            return files
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString().endsWith(".sql") ||
                                    path.toString().endsWith(".sql.gz"))
                    .toList();
        }
    }
}