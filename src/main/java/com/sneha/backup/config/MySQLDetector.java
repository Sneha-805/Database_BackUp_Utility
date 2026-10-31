package com.sneha.backup.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MySQLDetector {

    public static String findMySQL() {

        String mysqlPath =
                System.getenv("MYSQL_PATH");

        if (mysqlPath != null &&
                Files.exists(Paths.get(mysqlPath))) {

            return mysqlPath;
        }

        String[] possiblePaths = {

                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",

                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe",

                "C:\\Program Files\\MySQL\\MySQL Server 9.0\\bin\\mysql.exe",

                "C:\\xampp\\mysql\\bin\\mysql.exe"
        };

        for (String path : possiblePaths) {

            Path mysql =
                    Paths.get(path);

            if (Files.exists(mysql)) {

                return path;
            }
        }

        return null;
    }

    public static String findMySQLDump() {

        String mysqlDumpPath =
                System.getenv("MYSQLDUMP_PATH");

        if (mysqlDumpPath != null &&
                Files.exists(Paths.get(mysqlDumpPath))) {

            return mysqlDumpPath;
        }

        String[] possiblePaths = {

                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",

                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe",

                "C:\\Program Files\\MySQL\\MySQL Server 9.0\\bin\\mysqldump.exe",

                "C:\\xampp\\mysql\\bin\\mysqldump.exe"
        };

        for (String path : possiblePaths) {

            Path mysqlDump =
                    Paths.get(path);

            if (Files.exists(mysqlDump)) {

                return path;
            }
        }

        return null;
    }
}