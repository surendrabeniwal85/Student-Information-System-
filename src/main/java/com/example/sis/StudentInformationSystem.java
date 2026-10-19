package com.example.sis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class StudentInformationSystem {

    private static final String DB_URL =
            "jdbc:sqlite:students.db";

    public static void main(String[] args) {

        String sql = """
                CREATE TABLE IF NOT EXISTS Students (
                    StudentID INTEGER PRIMARY KEY AUTOINCREMENT,
                    RollNumber TEXT NOT NULL UNIQUE,
                    StudentName TEXT NOT NULL,
                    Email TEXT NOT NULL UNIQUE,
                    Department TEXT NOT NULL,
                    Marks REAL NOT NULL
                )
                """;

        try (Connection con =
                     DriverManager.getConnection(DB_URL);
             Statement stmt = con.createStatement()) {

            stmt.execute(sql);

            System.out.println(
                    "Students table created successfully!"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTable() {

    }
}
