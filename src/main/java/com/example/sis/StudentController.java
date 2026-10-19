package com.example.sis;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final String DB_URL =
            "jdbc:sqlite:students.db";

    public record StudentInput(
            String rollNumber,
            String studentName,
            String email,
            String department,
            double marks
    ) {}

    public record InsertResult(int studentId, String message) {}

    @PostMapping
    public InsertResult insertStudent(@RequestBody StudentInput s) {

        if (s.rollNumber() == null || s.rollNumber().isBlank()
                || s.studentName() == null || s.studentName().isBlank()
                || s.email() == null || s.email().isBlank()
                || s.department() == null || s.department().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "All fields are required.");
        }

        if (s.marks() < 0 || s.marks() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Marks must be between 0 and 100.");
        }

        String sql = """
            INSERT INTO Students
                (RollNumber, StudentName, Email, Department, Marks)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.rollNumber().trim());
            ps.setString(2, s.studentName().trim());
            ps.setString(3, s.email().trim());
            ps.setString(4, s.department().trim());
            ps.setDouble(5, s.marks());

            ps.executeUpdate();

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT last_insert_rowid()")) {
                rs.next();
                return new InsertResult(
                        rs.getInt(1), "Student inserted successfully.");
            }

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Roll number or email already exists.");
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database insertion failed.");
        }
    }
}
