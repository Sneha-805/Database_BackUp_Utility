# Database Backup Utility

A Java 17 command-line application for creating and restoring MySQL database backups. It uses the MySQL `mysqldump` and `mysql` command-line tools and stores backups as compressed `.sql.gz` files by default.

## Requirements

- Java 17 or newer
- Maven 3.8 or newer
- A reachable MySQL server
- MySQL client tools (`mysql` and `mysqldump`) installed on the computer running the utility

## Build and run

From the project directory:

```powershell
mvn clean package
java -jar target\database-backup-utility-1.0-SNAPSHOT.jar
```

The packaged JAR includes the application's Maven runtime dependencies. The MySQL command-line tools are external requirements and are not bundled.

At startup, enter the database host (default `localhost`), port (default `3306`), database name, username, and password. Password input is hidden when launched from a terminal that supports a Java console. The application locates MySQL tools using environment overrides first, then common Windows MySQL Server and XAMPP install paths:

| Environment variable | Purpose |
|---|---|
| `MYSQL_PATH` | Full path to `mysql.exe` |
| `MYSQLDUMP_PATH` | Full path to `mysqldump.exe` |
| `BACKUP_DIRECTORY` | Backup directory; defaults to `backups` in the current working directory |
| `BACKUP_RETENTION_COUNT` | Maximum number of managed backups to keep; defaults to `10`, minimum `1` |

Set environment variables before launching the JAR. The configured database host and port are passed to both MySQL tools. Credentials are supplied to the subprocess through `MYSQL_PWD`; do not share the process environment with untrusted users.

## Menu and behavior

- **Create Backup:** Runs `mysqldump`, compresses its output, checks the command's exit status, and reads the gzip file through to the end to detect corrupt/truncated output. Invalid or incomplete output is removed. After a successful backup, retention removes older files named `backup_*.sql` or `backup_*.sql.gz` in the selected directory; unrelated SQL files are preserved.
- **Restore Backup:** Lists `.sql` and `.sql.gz` files from the configured backup directory, asks for confirmation before modifying database contents, and streams the selected SQL into MySQL. If the destination database does not exist, the application asks separately before creating it. The MySQL account must have permission to create the database when needed.
- **Start Scheduled Backups:** Prompts for a positive interval in minutes and starts the first backup after that interval. Scheduled runs continue at that interval while the application remains open.
- **Stop Scheduled Backups:** Cancels future scheduled runs. A backup already in progress is allowed to finish.
- **Exit:** Stops scheduling and closes the application.

The scheduler is process-local: backups do not run after the application exits or the computer shuts down. Keep the application running for scheduled backups.

## Project layout

- `src/main/java/com/sneha/backup/Main.java` — CLI menu and scheduling
- `src/main/java/com/sneha/backup/config/` — database settings, environment configuration, and MySQL tool detection
- `src/main/java/com/sneha/backup/database/DatabaseConnection.java` — JDBC connections used for database checks and creation
- `src/main/java/com/sneha/backup/backup/` — dump generation, compression, listing, and retention
- `src/main/java/com/sneha/backup/restore/RestoreService.java` — database existence checks and restore execution
- `backups/` — default output directory (created automatically)

## Limitations

Verification checks successful dump completion and gzip integrity. It does not prove that the SQL can be restored successfully; for stronger assurance, periodically test a restore in a separate database. Scheduling only works while this program is running. The application does not encrypt backup files.
