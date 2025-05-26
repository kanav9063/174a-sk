package org.ivc.dbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Encapsulates basic database operations for IVC.
 */
public class DatabaseManager {

    private final Connection conn;

    // Constructor to initialize with open JDBC connection
    public DatabaseManager(Connection conn) {
        this.conn = conn;
    }

    // Close the connection if not already closed
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // ✅ Basic test method — lists all students
    public void listAllStudents() throws SQLException {
        String sql = "SELECT Perm, Name FROM Student";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Students:");
            while (rs.next()) {
                String perm = rs.getString("Perm");
                String name = rs.getString("Name");
                System.out.printf("  %s – %s%n", perm, name);
            }
        }
    }

    /**
     * Enro
     * lls student `perm` in course `cno` (for quarter_id = 1).
     * Returns true on success, false otherwise.
     */
    public boolean addCourse(String perm, String cno) throws SQLException {
        final int QUARTER_ID = 1;

        // Step 1: Find course offering
        String findSql =
            "SELECT Enrollment_id, Act_enrolled, Max_enrollment " +
            "FROM CourseOffering_offeredin " +
            "WHERE TRIM(cno) = ? AND Quarter_id = ?";


        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, cno.trim());
            ps.setInt(2, QUARTER_ID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Course not offered this quarter.");
                    return false;
                }

                int enrollmentId = rs.getInt("Enrollment_id");
                int enrolled = rs.getInt("Act_enrolled");
                int capacity = rs.getInt("Max_enrollment");

                // Step 2: Check if full
                if (enrolled >= capacity) {
                    System.out.println("Course is full.");
                    return false;
                }

                // Step 3: Already enrolled check
                String dupSql = "SELECT 1 FROM Enrolled WHERE Perm = ? AND Enrollment_id = ?";
                try (PreparedStatement psDup = conn.prepareStatement(dupSql)) {
                    psDup.setString(1, perm);
                    psDup.setInt(2, enrollmentId);
                    try (ResultSet rsDup = psDup.executeQuery()) {
                        if (rsDup.next()) {
                            System.out.println("Already enrolled.");
                            return false;
                        }
                    }
                }

                // Step 4: Enrolled in 5 or more?
                String countSql = "SELECT COUNT(*) FROM Enrolled WHERE Perm = ?";
                try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                    psCount.setString(1, perm);
                    try (ResultSet rsCount = psCount.executeQuery()) {
                        rsCount.next();
                        if (rsCount.getInt(1) >= 5) {
                            System.out.println("Cannot enroll in more than 5 courses.");
                            return false;
                        }
                    }
                }

                // Step 5: Insert enrollment
                String insertSql = "INSERT INTO Enrolled (Perm, Enrollment_id) VALUES (?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, perm);
                    psInsert.setInt(2, enrollmentId);
                    psInsert.executeUpdate();
                }

                // Step 6: Update act_enrolled
                String updateSql = "UPDATE CourseOffering_offeredin SET Act_enrolled = Act_enrolled + 1 WHERE Enrollment_id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, enrollmentId);
                    psUpdate.executeUpdate();
                }

                System.out.println("✅ Successfully enrolled " + perm + " in " + cno);
                return true;
            }
        }
    }

    /**
 * copying structure from addCourse, idk if this is correct lol.
 */
public boolean dropCourse(String perm, String cno) throws SQLException {
    final int QUARTER_ID = 1;

        // Step 1: Find enrollment record
        String findSql = 
            "SELECT e.Enrollment_id FROM Enrolled e " +
            "WHERE e.Perm = ? AND e.Enrollment_id IN " + 
            "(SELECT Enrollment_id FROM CourseOffering_offeredin " +
            "WHERE TRIM(cno) = ? AND Quarter_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(findSql)) {
        ps.setString(1, perm);
        ps.setString(2, cno.trim());
        ps.setInt(3, QUARTER_ID);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                System.out.println("Not enrolled in " + cno);
                return false;
            }

            int enrollmentId = rs.getInt("Enrollment_id");

            // Step 2: Check if this is the student's only course
            String countSql = "SELECT COUNT(*) FROM Enrolled WHERE Perm = ?";
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                psCount.setString(1, perm);
                try (ResultSet rsCount = psCount.executeQuery()) {
                    rsCount.next();
                    if (rsCount.getInt(1) <= 1) {
                        System.out.println("Cannot drop - must be enrolled in at least one course.");
                        return false;
                    }
                }
            }

            // Step 3: Remove enrollment record
            String deleteSql = "DELETE FROM Enrolled WHERE Perm = ? AND Enrollment_id = ?";
            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psDelete.setString(1, perm);
                psDelete.setInt(2, enrollmentId);
                psDelete.executeUpdate();
            }

            // Step 4: Update act_enrolled count
            String updateSql = "UPDATE CourseOffering_offeredin SET Act_enrolled = Act_enrolled - 1 WHERE Enrollment_id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, enrollmentId);
                psUpdate.executeUpdate();
            }

            System.out.println("Successfully dropped " + cno);
            return true;
        }
    }
}
    
}
