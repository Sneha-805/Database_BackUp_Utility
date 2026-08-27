package com.sneha.backup;

import com.sneha.backup.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        System.out.println("Database Backup Utility");

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            System.out.println("Database connection successful!");

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}