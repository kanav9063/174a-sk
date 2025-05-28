package org.ivc.dbms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

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
     * Enrolls student `perm` in course `cno` (for quarter_id = 1).
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


    public void listCurrentCourses(String perm) throws SQLException {
        String sql = 
            "SELECT co.cno, c.en_code, co.max_enrollment, " +
            "       co.professor_name, co.time_location " +
            "FROM takes_courses tc, courseoffering_offeredin co, course c " +
            "WHERE tc.enrollment_id = co.enrollment_id " +
            "AND tc.yr_qtr = co.yr_qtr " +
            "AND co.cno = c.cno " +
            "AND tc.perm_num = ? " +
            "AND tc.grade IS NULL " +  
            "ORDER BY co.cno";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Current Quarter Courses for Student " + perm + ":");
                System.out.println("Course | Enrollment Id        | Max Enrollment | Professor        | Time/Location");
                System.out.println("-------|----------------------|---------------|------------------|---------------");
                
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String cno = rs.getString("cno");
                    String title = rs.getString("en_code");
                    int maxEnroll = rs.getInt("max_enrollment");
                    String professor = rs.getString("professor_name") != null ? 
                                     rs.getString("professor_name") : "TBA";
                    String timeLocation = rs.getString("time_location") != null ? 
                                        rs.getString("time_location") : "TBA";
                    
                    System.out.printf("%-6s | %-20s | %-13d | %-16s | %s%n",
                        cno, title, maxEnroll, professor.trim(), timeLocation);
                }
                
                if (!hasResults) {
                    System.out.println("No courses enrolled in current quarter.");
                }
            }
        }
    }

    /**
     * list grades from just the previous quarter, registrar feature
     */
    public void listPrevQuarterGrades(String perm) throws SQLException {    
        // First find the most recent completed quarter
        String prevQuarterSql = 
            "SELECT DISTINCT co.yr_qtr " +
            "FROM takes_courses tc, courseoffering_offeredin co " +
            "WHERE tc.enrollment_id = co.enrollment_id " +
            "AND tc.yr_qtr = co.yr_qtr " +
            "AND tc.perm_num = ? " +
            "AND tc.grade IS NOT NULL " +
            "ORDER BY " +
            "  SUBSTR(co.yr_qtr, 1, 2) DESC, " +  // Year part
            "  CASE SUBSTR(co.yr_qtr, 3, 1) " +   // Quarter part
            "    WHEN 'F' THEN 3 " +              // Fall is latest
            "    WHEN 'S' THEN 2 " +              // Spring is middle
            "    WHEN 'W' THEN 1 " +              // Winter is earliest
            "  END DESC " +
            "FETCH FIRST 1 ROW ONLY";

        String prevQuarter = null;
        try (PreparedStatement ps = conn.prepareStatement(prevQuarterSql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prevQuarter = rs.getString("yr_qtr");
                }
            }
        }

        if (prevQuarter == null) {
            System.out.println("No previous quarter grades found for student " + perm);
            return;
        }

        String pastSql = 
            "SELECT co.cno, c.en_code, co.max_enrollment, " +
            "       co.professor_name, co.time_location, tc.grade " +
            "FROM takes_courses tc, courseoffering_offeredin co, course c " +
            "WHERE tc.enrollment_id = co.enrollment_id " +
            "AND tc.yr_qtr = co.yr_qtr " +
            "AND co.cno = c.cno " +
            "AND tc.perm_num = ? " +
            "AND tc.yr_qtr = ? " +
            "ORDER BY co.cno";

        try (PreparedStatement ps = conn.prepareStatement(pastSql)) {
            ps.setString(1, perm);
            ps.setString(2, prevQuarter);
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nPrevious Quarter (" + prevQuarter + ") Courses:");
                System.out.println("Course | Enrollment Id        | Grade | Professor");
                System.out.println("-------|----------------------|-------|------------");
                
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String cno = rs.getString("cno");
                    String title = rs.getString("en_code");
                    String grade = rs.getString("grade");
                    String professor = rs.getString("professor_name") != null ? 
                                     rs.getString("professor_name") : "TBA";
                    
                    System.out.printf("%-6s | %-20s | %-5s | %s%n",
                        cno, title, grade, professor.trim());
                }
                
                if (!hasResults) {
                    System.out.println("No courses found from previous quarter.");
                }
            }
        }
    }

    /**
     * Enters grades for all students in a course from a JSON file.
     * Expected JSON format - similar to slack:
     * {
     *   "enrollment_id": 56789,
     *  "yr_qtr": "25W",
     *   "grades": [
     *     { "perm": "1234567", "grade": "B" },
     *     { "perm": "1468222", "grade": "A" }
     *   ]
     * }
     * 
     * for correct usage make sure add the JSON library dependency to the pom.xml file. Add this inside the <dependencies> section
     * <dependency>
     * <groupId>org.json</groupId>
     * <artifactId>json</artifactId>
     * <version>20231013</version>
     * </dependency>
     * Then run mvn clean install to make sure the JSON library is included.
     * do i need to add validation here for duplicate grades (ie if the same student shows up twice)
     */
    public boolean enterGradesFromFile(String filename) throws SQLException {
        try {
            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }
            
            JSONObject json = new JSONObject(jsonContent.toString());
            JSONArray grades = json.getJSONArray("grades");
            int enrollmentId = json.getInt("enrollment_id");
            String yrQtr = json.getString("yr_qtr");
            
            String verifySql = 
                "SELECT 1 FROM courseoffering_offeredin WHERE enrollment_id = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                ps.setInt(1, enrollmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Course offering " + enrollmentId + " not found.");
                        return false;
                    }
                }
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (int i = 0; i < grades.length(); i++) {
                JSONObject gradeEntry = grades.getJSONObject(i);
                String perm = gradeEntry.getString("perm");
                String grade = gradeEntry.getString("grade");
                
                if (!grade.matches("^[A-C][+-]?|D|F$")) {
                    System.out.println("Invalid grade format for student " + perm + ": " + grade);
                    failCount++;
                    continue;
                }
                
                String updateSql =
                    "UPDATE takes_courses SET grade = ? WHERE perm_num = ? AND enrollment_id = ? AND yr_qtr = ?";
                
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, grade);
                    ps.setString(2, perm);
                    ps.setInt(3, enrollmentId);
                    ps.setString(4, yrQtr);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        successCount++;
                    } else {
                        System.out.println("No record found for student " + perm + " in enrollment " + enrollmentId + " for quarter " + yrQtr);
                        failCount++;
                    }
                } catch (SQLException e) {
                    System.out.println("Error updating grade for student " + perm + ": " + e.getMessage());
                    failCount++;
                }
            }
            
            System.out.println("Grade entry complete for enrollment " + enrollmentId + " (" + yrQtr + "):");
            System.out.println("Successfully updated: " + successCount + " grades");
            System.out.println("Failed to update: " + failCount + " grades");
            
            return failCount == 0;
            
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Error processing JSON: " + e.getMessage());
            return false;
        }
    }

        /**
     * request transcript, registrar feature
     */
    public void requestTranscript(String perm) throws SQLException {
        String sql = 
            "SELECT co.cno, c.en_code, co.max_enrollment, " +
            "       co.professor_name, co.time_location, tc.grade, co.yr_qtr " +
            "FROM takes_courses tc, courseoffering_offeredin co, course c " +
            "WHERE tc.enrollment_id = co.enrollment_id " +
            "AND tc.yr_qtr = co.yr_qtr " +
            "AND co.cno = c.cno " +
            "AND tc.perm_num = ? " +
            "AND tc.grade IS NOT NULL " +
            "ORDER BY co.yr_qtr DESC, co.cno";  //look into changing to true chronological ordering

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Transcript for Student " + perm + ":");
                System.out.println("Quarter | Course | Enrollment Id        | Grade | Professor        | Time/Location");
                System.out.println("--------|--------|----------------------|-------|------------------|---------------");
                
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String yrQtr = rs.getString("yr_qtr");
                    String cno = rs.getString("cno");
                    String title = rs.getString("en_code");
                    String grade = rs.getString("grade");
                    String professor = rs.getString("professor_name") != null ? 
                                     rs.getString("professor_name") : "TBA";
                    String timeLocation = rs.getString("time_location") != null ? 
                                        rs.getString("time_location") : "TBA";
                    
                    System.out.printf("%-7s | %-6s | %-20s | %-5s | %-16s | %s%n",
                        yrQtr, cno, title, grade, professor.trim(), timeLocation);
                }
                
                if (!hasResults) {
                    System.out.println("No past courses found.");
                }
            }
        }
    }

    /**
     * Generates grade mailers for all students in a given quarter, registrar feature
     */
    public void generateGradeMailers(String yrQtr) throws SQLException {
        String sql = 
            "SELECT DISTINCT s.perm_num, s.name, s.majorname " +
            "FROM student s, takes_courses tc " +
            "WHERE s.perm_num = tc.perm_num " +
            "AND tc.yr_qtr = ? " +
            "AND tc.grade IS NOT NULL " +
            "ORDER BY s.perm_num";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yrQtr);
            
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasStudents = false;
                
                while (rs.next()) {
                    hasStudents = true;
                    String perm = rs.getString("perm_num");
                    String name = rs.getString("name");
                    String major = rs.getString("majorname");
                    
                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("GRADE MAILER - " + yrQtr);
                    System.out.println("=".repeat(60));
                    System.out.printf("Student: %s (%s)%n", name, perm);
                    System.out.printf("Major: %s%n", major);
                    System.out.println("-".repeat(60));
                    
                    String courseSql = 
                        "SELECT tc.perm_num, s.name, co.cno, c.en_code, tc.grade, co.professor_name, co.yr_qtr " +
                        "FROM takes_courses tc, student s, courseoffering_offeredin co, course c " +
                        "WHERE tc.perm_num = s.perm_num " +
                        "AND tc.enrollment_id = co.enrollment_id " +
                        "AND tc.yr_qtr = co.yr_qtr " +
                        "AND co.cno = c.cno " +
                        "AND tc.perm_num = ? " +
                        "AND tc.yr_qtr = ? " +
                        "ORDER BY co.cno";
                    
                    try (PreparedStatement psCourses = conn.prepareStatement(courseSql)) {
                        psCourses.setString(1, perm);
                        psCourses.setString(2, yrQtr);
                        
                        try (ResultSet rsCourses = psCourses.executeQuery()) {
                            System.out.println("Course | Enrollment Id   | Professor        | Grade");
                            System.out.println("-------|-----------------|------------------|-------");
                            
                            while (rsCourses.next()) {
                                String cno = rsCourses.getString("cno");
                                String title = rsCourses.getString("en_code");
                                String professor = rsCourses.getString("professor_name") != null ? 
                                                 rsCourses.getString("professor_name") : "TBA";
                                String grade = rsCourses.getString("grade");
                                
                                System.out.printf("%-6s | %-15s | %-16s | %s%n",
                                    cno, title, professor.trim(), grade);
                            }
                        }
                    }
                    
                    System.out.println("=".repeat(60) + "\n");
                }
                
                if (!hasStudents) {
                    System.out.println("No students found with grades for quarter " + yrQtr);
                }
            }
        }
    }

}
