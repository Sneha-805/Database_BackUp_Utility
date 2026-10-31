package com.sneha.backup.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupListServiceTest {

    @TempDir
    Path backupDirectory;

    @Test
    void listsSqlBackupsNewestFirstAndPrunesOnlyManagedBackups()
            throws IOException {
        Path older = createFile("backup_older.sql.gz", 1);
        Path newest = createFile("backup_newest.sql.gz", 3);
        Path middle = createFile("backup_middle.sql", 2);
        Path manual = createFile("manual.sql", 4);
        createFile("notes.txt", 5);

        BackupListService service = new BackupListService(backupDirectory);

        List<Path> listed = service.listBackups();
        assertEquals(List.of(manual, newest, middle, older), listed);

        service.pruneBackups(2);

        assertTrue(Files.exists(newest));
        assertTrue(Files.exists(middle));
        assertFalse(Files.exists(older));
        assertTrue(Files.exists(manual));
    }

    @Test
    void rejectsZeroRetentionCount() {
        BackupListService service = new BackupListService(backupDirectory);

        assertThrows(IllegalArgumentException.class,
                () -> service.pruneBackups(0));
    }

    private Path createFile(String name, long seconds) throws IOException {
        Path file = Files.createFile(backupDirectory.resolve(name));
        Files.setLastModifiedTime(
                file,
                FileTime.from(Instant.ofEpochSecond(seconds))
        );
        return file;
    }
}
