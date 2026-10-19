package com.example.sis;

import java.sql.*;
import java.util.Scanner;

public class StudentInformationSystem1 {

    private static final String DB_URL = "jdbc:sqlite:students.db";
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(DB_URL)) {

            // Enable foreign-key enforcement for this connection.
            try (Statement st = con.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }

            // 1. CREATE TABLE
            createTable(con);

            boolean running = true;

            while (running) {
                System.out.println("\n===== STUDENT INFORMATION SYSTEM =====");
                System.out.println("1. Insert Student");
                System.out.println("2. Display All Students");
                System.out.println("3. Search Student (WHERE, IN, BETWEEN, LIKE)");
                System.out.println("4. Update Student");
                System.out.println("5. Delete Student");
                System.out.println("6. Exit");

                int choice = readInt("Enter your choice: ");

                switch (choice) {
                    case 1 -> insertStudent(con);
                    case 2 -> displayStudents(con);
                    case 3 -> searchStudents(con);
                    case 4 -> updateStudent(con);
                    case 5 -> deleteStudent(con);
                    case 6 -> {
                        running = false;
                        System.out.println("Application closed.");
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        sc.close();
    }

    // 1. CREATE: Create Students table
    private static void createTable(Connection con) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS Students (
                    StudentID INTEGER PRIMARY KEY AUTOINCREMENT,
                    RollNumber TEXT NOT NULL UNIQUE,
                    StudentName TEXT NOT NULL,
                    Email TEXT NOT NULL UNIQUE,
                    Department TEXT NOT NULL,
                    Marks REAL NOT NULL CHECK (Marks BETWEEN 0 AND 100)
                )
                """;

        try (Statement st = con.createStatement()) {
            st.execute(sql);
            System.out.println("Students table is ready.");
        }
    }

    // 2. INSERT: Add a new student
    private static void insertStudent(Connection con) throws SQLException {

        String sql = """
                INSERT INTO Students
                (RollNumber, StudentName, Email, Department, Marks)
                VALUES (?, ?, ?, ?, ?)
                """;

        String roll = readText("Enter roll number: ");
        String name = readText("Enter student name: ");
        String email = readText("Enter email: ");
        String department = readText("Enter department: ");
        double marks = readDouble("Enter marks (0-100): ");

        if (marks < 0 || marks > 100) {
            System.out.println("Marks must be between 0 and 100.");
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, department);
            ps.setDouble(5, marks);

            int rows = ps.executeUpdate();
            System.out.println(rows + " student inserted successfully.");

        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    // 3. SELECT: Display all students
    private static void displayStudents(Connection con) throws SQLException {

        String sql = "SELECT * FROM Students ORDER BY StudentID";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            printStudents(rs);
        }
    }

    // 3. SELECT with WHERE, IN, BETWEEN and LIKE
    private static void searchStudents(Connection con) throws SQLException {

        System.out.println("\n--- SEARCH OPTIONS ---");
        System.out.println("1. Search by Student ID (WHERE)");
        System.out.println("2. Search by Department (WHERE)");
        System.out.println("3. Search multiple IDs (IN)");
        System.out.println("4. Search marks range (BETWEEN)");
        System.out.println("5. Search name (LIKE)");

        int choice = readInt("Choose search option: ");

        switch (choice) {

            case 1 -> {
                String sql = "SELECT * FROM Students WHERE StudentID = ?";

                int id = readInt("Enter student ID: ");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        printStudents(rs);
                    }
                }
            }

            case 2 -> {
                String sql = "SELECT * FROM Students WHERE Department = ?";

                String department = readText("Enter department: ");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, department);

                    try (ResultSet rs = ps.executeQuery()) {
                        printStudents(rs);
                    }
                }
            }

            case 3 -> {
                String input = readText(
                        "Enter student IDs separated by commas (e.g. 1,2,3): "
                );

                String[] ids = input.split(",");

                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < ids.length; i++) {
                    if (i > 0) {
                        placeholders.append(", ");
                    }
                    placeholders.append("?");
                }

                String sql = "SELECT * FROM Students WHERE StudentID IN ("
                        + placeholders + ") ORDER BY StudentID";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    for (int i = 0; i < ids.length; i++) {
                        ps.setInt(i + 1, Integer.parseInt(ids[i].trim()));
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        printStudents(rs);
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Enter valid numeric IDs.");
                }
            }

            case 4 -> {
                String sql = """
                        SELECT * FROM Students
                        WHERE Marks BETWEEN ? AND ?
                        ORDER BY Marks DESC
                        """;

                double min = readDouble("Enter minimum marks: ");
                double max = readDouble("Enter maximum marks: ");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, min);
                    ps.setDouble(2, max);

                    try (ResultSet rs = ps.executeQuery()) {
                        printStudents(rs);
                    }
                }
            }

            case 5 -> {
                String sql = """
                        SELECT * FROM Students
                        WHERE StudentName LIKE ? COLLATE NOCASE
                        """;

                String name = readText("Enter full or partial name: ");

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, "%" + name + "%");

                    try (ResultSet rs = ps.executeQuery()) {
                        printStudents(rs);
                    }
                }
            }

            default -> System.out.println("Invalid search option.");
        }
    }

    // 4. UPDATE: Modify an existing student
    private static void updateStudent(Connection con) throws SQLException {

        int id = readInt("Enter Student ID to update: ");

        String name = readText("Enter new student name: ");
        String email = readText("Enter new email: ");
        String department = readText("Enter new department: ");
        double marks = readDouble("Enter new marks (0-100): ");

        if (marks < 0 || marks > 100) {
            System.out.println("Marks must be between 0 and 100.");
            return;
        }

        String sql = """
                UPDATE Students
                SET StudentName = ?,
                    Email = ?,
                    Department = ?,
                    Marks = ?
                WHERE StudentID = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, department);
            ps.setDouble(4, marks);
            ps.setInt(5, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student updated successfully.");
            } else {
                System.out.println("Student ID not found.");
            }

        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    // 5. DELETE: Remove a student
    private static void deleteStudent(Connection con) throws SQLException {

        int id = readInt("Enter Student ID to delete: ");
        String confirmation = readText("Type DELETE to confirm: ");

        if (!confirmation.equals("DELETE")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        String sql = "DELETE FROM Students WHERE StudentID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student deleted successfully.");
            } else {
                System.out.println("Student ID not found.");
            }
        }
    }

    // Display query results
    private static void printStudents(ResultSet rs) throws SQLException {

        boolean found = false;

        System.out.printf("%-5s %-12s %-20s %-28s %-12s %-7s%n",
                "ID", "Roll No.", "Name", "Email", "Department", "Marks");

        System.out.println("-".repeat(90));

        while (rs.next()) {
            found = true;

            System.out.printf("%-5d %-12s %-20s %-28s %-12s %-7.2f%n",
                    rs.getInt("StudentID"),
                    rs.getString("RollNumber"),
                    rs.getString("StudentName"),
                    rs.getString("Email"),
                    rs.getString("Department"),
                    rs.getDouble("Marks"));
        }

        if (!found) {
            System.out.println("No matching students found.");
        }
    }

    // Input helpers
    private static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}