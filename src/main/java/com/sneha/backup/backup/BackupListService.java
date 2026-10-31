package com.sneha.backup.backup;

import com.sneha.backup.config.BackupConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class BackupListService {

    private final Path backupDirectory;

    public BackupListService() {
        this(BackupConfig.getBackupDirectory());
    }

    public BackupListService(Path backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    public List<Path> listBackups() throws IOException {

        if (!Files.exists(backupDirectory)) {
            return new ArrayList<>();
        }

        List<BackupFile> backups = new ArrayList<>();
        try (Stream<Path> files = Files.list(backupDirectory)) {
            for (Path path : files
                    .filter(file -> Files.isRegularFile(
                            file,
                            LinkOption.NOFOLLOW_LINKS
                    ))
                    .filter(BackupListService::isBackupFile)
                    .toList()) {
                backups.add(new BackupFile(
                        path,
                        Files.getLastModifiedTime(path)
                ));
            }
        }

        backups.sort(Comparator.comparing(
                BackupFile::modified,
                Comparator.reverseOrder()
        ));
        return backups.stream().map(BackupFile::path).toList();
    }

    public void pruneBackups(int retentionCount) throws IOException {
        if (retentionCount < 1) {
            throw new IllegalArgumentException(
                    "Retention count must be at least 1."
            );
        }

        List<BackupFile> backups = new ArrayList<>();
        if (!Files.exists(backupDirectory)) {
            return;
        }

        try (Stream<Path> files = Files.list(backupDirectory)) {
            for (Path path : files
                    .filter(file -> Files.isRegularFile(
                            file,
                            LinkOption.NOFOLLOW_LINKS
                    ))
                    .filter(BackupListService::isManagedBackupFile)
                    .toList()) {
                backups.add(new BackupFile(
                        path,
                        Files.getLastModifiedTime(path)
                ));
            }
        }

        backups.sort(Comparator.comparing(
                BackupFile::modified,
                Comparator.reverseOrder()
        ));
        for (int i = retentionCount; i < backups.size(); i++) {
            Files.delete(backups.get(i).path());
        }
    }

    private static boolean isBackupFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".sql") || name.endsWith(".sql.gz");
    }

    private static boolean isManagedBackupFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.startsWith("backup_") &&
                (name.endsWith(".sql") || name.endsWith(".sql.gz"));
    }

    private record BackupFile(Path path, FileTime modified) {
    }
}