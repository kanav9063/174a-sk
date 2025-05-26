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
// public boolean dropCourse(String perm, String cno) throws SQLException {
//     final int QUARTER_ID = 1;

//         // Step 1: Find enrollment record
//         String findSql = 
//             "SELECT e.Enrollment_id FROM Enrolled e " +
//             "WHERE e.Perm = ? AND e.Enrollment_id IN " + 
//             "(SELECT Enrollment_id FROM CourseOffering_offeredin " +
//             "WHERE TRIM(cno) = ? AND Quarter_id = ?)";

//     try (PreparedStatement ps = conn.prepareStatement(findSql)) {
//         ps.setString(1, perm);
//         ps.setString(2, cno.trim());
//         ps.setInt(3, QUARTER_ID);

//         try (ResultSet rs = ps.executeQuery()) {
//             if (!rs.next()) {
//                 System.out.println("Not enrolled in " + cno);
//                 return false;
//             }

//             int enrollmentId = rs.getInt("Enrollment_id");

//             // Step 2: Check if this is the student's only course
//             String countSql = "SELECT COUNT(*) FROM Enrolled WHERE Perm = ?";
//             try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
//                 psCount.setString(1, perm);
//                 try (ResultSet rsCount = psCount.executeQuery()) {
//                     rsCount.next();
//                     if (rsCount.getInt(1) <= 1) {
//                         System.out.println("Cannot drop - must be enrolled in at least one course.");
//                         return false;
//                     }
//                 }
//             }

//             // Step 3: Remove enrollment record
//             String deleteSql = "DELETE FROM Enrolled WHERE Perm = ? AND Enrollment_id = ?";
//             try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
//                 psDelete.setString(1, perm);
//                 psDelete.setInt(2, enrollmentId);
//                 psDelete.executeUpdate();
//             }

//             // Step 4: Update act_enrolled count
//             String updateSql = "UPDATE CourseOffering_offeredin SET Act_enrolled = Act_enrolled - 1 WHERE Enrollment_id = ?";
//             try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
//                 psUpdate.setInt(1, enrollmentId);
//                 psUpdate.executeUpdate();
//             }

//             System.out.println("Successfully dropped " + cno);
//             return true;
//         }
//     }
// }
/**
 * listing all courses enrolled in the current quarter, gold feature.
 */
    public void listCurrentCourses(String perm) throws SQLException {
        final int QUARTER_ID = 1;
        
        String sql = 
            "SELECT co.cno, c.title, co.act_enrolled, co.max_enrollment, " +
            "       co.pfirst_name, co.plast_name, co.building_code, co.room_num, co.time_slot " +
            "FROM courseoffering_offeredin co, course c " +
            "WHERE co.cno = c.cno " +
            "AND co.quarter_id = ? " +
            "AND co.enrollment_id IN (SELECT enrollment_id FROM enrolled WHERE perm = ?) " +
            "ORDER BY co.cno";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, QUARTER_ID);
            ps.setString(2, perm);
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Current Quarter Courses for Student " + perm + ":");
                System.out.println("Course | Title                | Enrolled/Max | Instructor        | Location");
                System.out.println("-------|----------------------|--------------|-------------------|----------");
                
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String cno = rs.getString("cno");
                    String title = rs.getString("title");
                    int enrolled = rs.getInt("act_enrolled");
                    int maxEnroll = rs.getInt("max_enrollment");
                    String instructor = (rs.getString("pfirst_name") != null ? rs.getString("pfirst_name") + " " : "") + 
                                      (rs.getString("plast_name") != null ? rs.getString("plast_name") : "TBA");
                    String location = (rs.getString("building_code") != null ? rs.getString("building_code") : "") + 
                                    " " + (rs.getString("room_num") != null ? rs.getString("room_num") : "");
                    String timeSlot = rs.getString("time_slot") != null ? rs.getString("time_slot") : "TBA";
                    
                    System.out.printf("%-6s | %-20s | %3d/%-3d      | %-17s | %s %s%n",
                        cno, title, enrolled, maxEnroll, instructor.trim(), location.trim(), timeSlot);
                }
                
                if (!hasResults) {
                    System.out.println("No courses enrolled in current quarter.");
                }
            }
        }
    }

/**
 * list all courses taken by a student, registrar feature
 * ima assume this is not only current quarter but all quarters.
 */
public void listAllCourses(String perm) throws SQLException {
    // First show current courses
    listCurrentCourses(perm);
    
    // Then show past courses
    String pastSql = 
        "SELECT co.quarter_id, co.cno, c.title, co.pfirst_name, co.plast_name, " +
        "       tc.grade, q.year, q.term " +
        "FROM took_courses tc, courseoffering_offeredin co, course c, quarter q " +
        "WHERE tc.enrollment_id = co.enrollment_id " +
        "AND co.cno = c.cno " +
        "AND co.quarter_id = q.quarterid " +
        "AND tc.perm = ? " +  // Add filter for student
        "ORDER BY q.year DESC, q.term DESC, co.cno";

    try (PreparedStatement ps = conn.prepareStatement(pastSql)) {
        ps.setString(1, perm);
        
        try (ResultSet rs = ps.executeQuery()) {
            System.out.println("\nPast Courses:");
            System.out.println("Quarter | Course | Title                | Grade | Instructor");
            System.out.println("--------|--------|----------------------|-------|------------");
            
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String quarter = rs.getString("year") + " " + rs.getString("term");
                String cno = rs.getString("cno");
                String title = rs.getString("title");
                String grade = rs.getString("grade");
                String instructor = (rs.getString("pfirst_name") != null ? rs.getString("pfirst_name") + " " : "") + 
                                  (rs.getString("plast_name") != null ? rs.getString("plast_name") : "TBA");
                
                System.out.printf("%-7s | %-6s | %-20s | %-5s | %s%n",
                    quarter, cno, title, grade, instructor.trim());
            }
            
            if (!hasResults) {
                System.out.println("No past courses found.");
            }
        }
    }
}

}
