package com.sneha.backup.config;

import java.io.Console;
import java.util.Scanner;

public class ConfigManager {

    public static DatabaseConfig getDatabaseConfig(Scanner scanner) {



        System.out.println();
        System.out.println("================================");
        System.out.println("     DATABASE CONFIGURATION     ");
        System.out.println("================================");

        System.out.print("Database host [localhost]: ");
        String host = scanner.nextLine();

        if (host.isBlank()) {
            host = "localhost";
        }

        System.out.print("Database port [3306]: ");
        String port = scanner.nextLine();

        if (port.isBlank()) {
            port = "3306";
        }

        try {
            int parsedPort = Integer.parseInt(port);
            if (parsedPort < 1 || parsedPort > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid database port. Use a value from 1 to 65535.");
            return null;
        }

        System.out.print("Database name: ");
        String databaseName = scanner.nextLine();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        if (databaseName.isBlank() || username.isBlank()) {
            System.out.println("Database name and username are required.");
            return null;
        }

        String password;

        Console console = System.console();

        if (console != null) {

            char[] passwordChars =
                    console.readPassword("Password: ");

            password = new String(passwordChars);

        } else {

            System.out.print("Password: ");
            password = scanner.nextLine();
        }

        String mysqlPath =
                MySQLDetector.findMySQL();

        String mysqldumpPath =
                MySQLDetector.findMySQLDump();

        if (mysqlPath == null ||
                mysqldumpPath == null) {

            System.out.println();
            System.out.println(
                    "MySQL installation was not found."
            );

            System.out.println(
                    "Please install MySQL and try again."
            );

            return null;
        }

        System.out.println();
        System.out.println("MySQL installation detected.");

        return new DatabaseConfig(
                host,
                port,
                databaseName,
                username,
                password,
                mysqlPath,
                mysqldumpPath
        );
    }
}