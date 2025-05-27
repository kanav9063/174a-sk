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

    // Basic test method — lists all students
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
     * Enro lls student `perm` in course `cno` (for quarter_id = 1). Returns
     * true on success, false otherwise.
     */
    public boolean addCourse(String perm, String cno) throws SQLException {

        final int QUARTER_ID = 1;

        // Step 1: Find course offering
        String findSql
                = "SELECT Enrollment_id, Act_enrolled, Max_enrollment "
                + "FROM CourseOffering_offeredin "
                + "WHERE TRIM(cno) = ? AND Quarter_id = ?";

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
     * Drops student `perm` from course `cno` in quarter 1. Returns true on
     * success, false otherwise.
     */
    public boolean dropCourse(String perm, String cno) throws SQLException {
        final int QUARTER_ID = 1;
        conn.setAutoCommit(false);
        try {
            // 1. Find the offering
            String findSql
                    = "SELECT Enrollment_id, Act_enrolled "
                    + "FROM CourseOffering_offeredin "
                    + "WHERE TRIM(cno)=? AND quarter_id=?";
            int enrollmentId, enrolledCount;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, cno.trim());
                ps.setInt(2, QUARTER_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Course not offered this quarter.");
                        conn.rollback();
                        return false;
                    }
                    enrollmentId = rs.getInt("Enrollment_id");
                    enrolledCount = rs.getInt("Act_enrolled");
                }
            }

            // 2. Ensure student is enrolled
            String checkSql = "SELECT 1 FROM Enrolled WHERE Perm=? AND Enrollment_id=?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, perm);
                ps.setInt(2, enrollmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Not enrolled in " + cno + ".");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 3. Prevent dropping last course
            String countSql = "SELECT COUNT(*) FROM Enrolled WHERE Perm=?";
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setString(1, perm);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) <= 1) {
                        System.out.println("Cannot drop your last course.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 4. Delete enrollment
            String delSql = "DELETE FROM Enrolled WHERE Perm=? AND Enrollment_id=?";
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.setString(1, perm);
                ps.setInt(2, enrollmentId);
                ps.executeUpdate();
            }

            // 5. Decrement count
            String updSql
                    = "UPDATE CourseOffering_offeredin "
                    + "SET Act_enrolled = Act_enrolled - 1 "
                    + "WHERE Enrollment_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updSql)) {
                ps.setInt(1, enrollmentId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Dropped " + cno + " for " + perm);
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void listCurrentCourses(String perm) throws SQLException {
        final int QUARTER_ID = 1;

        String sql = """
        SELECT
          co.cno,
          c.title,
          co.time_slot,
          co.building_code,
          co.room_num,
          co.plast_name,
          co.pfirst_name
        FROM
          Enrolled e
        JOIN CourseOffering_offeredin co ON e.Enrollment_id = co.Enrollment_id
        JOIN Course c ON co.cno = c.cno
        WHERE
          e.perm = ? AND co.quarter_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, QUARTER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("📚 Current Courses:");
                int count = 0;
                while (rs.next()) {
                    String cno = rs.getString("cno");
                    String title = rs.getString("title");
                    String time = rs.getString("time_slot");
                    String room = rs.getString("building_code") + " " + rs.getString("room_num");
                    String prof = rs.getString("pfirst_name") + " " + rs.getString("plast_name");

                    System.out.printf(" - %s — %s | %s | %s | Prof. %s%n", cno, title, time, room, prof);
                    count++;
                }
                if (count == 0) {
                    System.out.println("No current enrollments found.");
                }
            }
        }
    }

    public void listPreviousQuarterGrades(String perm) throws SQLException {
        final int PREVIOUS_QUARTER_ID = 2;

        String sql = """
            SELECT
              co.cno,
              c.title,
              tc.grade
            FROM
              took_courses tc
            JOIN courseoffering_offeredin co ON tc.enrollment_id = co.enrollment_id
            JOIN course c ON co.cno = c.cno
            WHERE
              tc.perm = ? AND co.quarter_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, PREVIOUS_QUARTER_ID);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("📝 Previous Quarter Grades:");
                int count = 0;
                while (rs.next()) {
                    String cno = rs.getString("cno");
                    String title = rs.getString("title");
                    String grade = rs.getString("grade");

                    System.out.printf(" - %s — %s : %s%n", cno, title, grade);
                    count++;
                }

                if (count == 0) {
                    System.out.println("No grades found for previous quarter.");
                }
            }
        }
    }

}
