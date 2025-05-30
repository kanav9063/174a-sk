package org.ivc.dbms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Encapsulates basic database operations for IVC, updated to match the final
 * schema: - student(perm_num, …) - courseoffering_offeredin(enrollment_id, cno,
 * yr_qtr, max_enrollment, professor_name, time_location) -
 * takes_courses(perm_num, enrollment_id, yr_qtr, grade)
 */
public class DatabaseManager {

    private final Connection conn;

    // === Define your current and previous quarter codes ===
    private static final String CURRENT_QTR = "25S";
    private static final String PREVIOUS_QTR = "25W";

    public DatabaseManager(Connection conn) {
        this.conn = conn;
    }

    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // 1. List all students
    public void listAllStudents() throws SQLException {
        // we fetch all students' perm numbers and names
        String sql = "SELECT perm_num, name FROM student";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Students:");
            // Loop through each student 
            while (rs.next()) {
                System.out.printf("  %s – %s%n",
                        rs.getString("perm_num"),
                        rs.getString("name")
                );
            }
        }
    }

    // 2. Enroll student `perm` in course `cno` for CURRENT_QTR.
    public boolean addCourse(String perm, String cno) throws SQLException {
        // 2.1) Try to find the course offering for this course and quarter
        String findSql
                = "SELECT enrollment_id, max_enrollment "
                + "FROM courseoffering_offeredin "
                + "WHERE cno = ? AND yr_qtr = ?";
        int enrollmentId, capacity;
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, cno);
            ps.setString(2, CURRENT_QTR);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // If not found, course isn't offered this quarter
                    System.out.println("Course not offered this quarter.");
                    return false;
                }
                // Get the enrollment id and max capacity for this offering
                enrollmentId = rs.getInt("enrollment_id");
                capacity = rs.getInt("max_enrollment");
            }
        }

        // 2.2) Check if the course is already full
        String capSql
                = "SELECT COUNT(*) FROM takes_courses "
                + "WHERE enrollment_id = ? AND yr_qtr = ?";
        try (PreparedStatement ps = conn.prepareStatement(capSql)) {
            ps.setInt(1, enrollmentId);
            ps.setString(2, CURRENT_QTR);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) >= capacity) {
                    // Too many students already enrolled
                    System.out.println("Course is full.");
                    return false;
                }
            }
        }

        // 2.3) Prevent duplicate enrollment for this student/course/quarter
        String dupSql
                = "SELECT 1 FROM takes_courses "
                + "WHERE perm_num = ? AND enrollment_id = ? AND yr_qtr = ?";
        try (PreparedStatement ps = conn.prepareStatement(dupSql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollmentId);
            ps.setString(3, CURRENT_QTR);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Already enrolled
                    System.out.println("Already enrolled.");
                    return false;
                }
            }
        }

        // 2.4) Check if student is already in 5 or more courses this quarter
        String countSql
                = "SELECT COUNT(*) FROM takes_courses "
                + "WHERE perm_num = ? AND yr_qtr = ?";
        try (PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setString(1, perm);
            ps.setString(2, CURRENT_QTR);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) >= 5) {
                    // Student can't enroll in more than 5 courses
                    System.out.println("Cannot enroll in more than 5 courses.");
                    return false;
                }
            }
        }

        // 2.5) All checks passed, insert the enrollment (grade is NULL for current)
        String insSql
                = "INSERT INTO takes_courses "
                + "(perm_num, enrollment_id, yr_qtr, grade) "
                + "VALUES (?, ?, ?, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(insSql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollmentId);
            ps.setString(3, CURRENT_QTR);
            ps.executeUpdate();
        }

        // Success message
        System.out.printf("✅ %s enrolled in %s (%s)%n", perm, cno, CURRENT_QTR);
        return true;
    }

    // 3. Drop student `perm` from course `cno` in CURRENT_QTR.
    public boolean dropCourse(String perm, String cno) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // 3.1) Find the offering
            String findSql
                    = "SELECT enrollment_id FROM courseoffering_offeredin "
                    + "WHERE cno = ? AND yr_qtr = ?";
            int enrollmentId;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, cno);
                ps.setString(2, CURRENT_QTR);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Course not offered this quarter.");
                        conn.rollback();
                        return false;
                    }
                    enrollmentId = rs.getInt("enrollment_id");
                }
            }

            // 3.2) Ensure student is enrolled
            String checkSql
                    = "SELECT 1 FROM takes_courses "
                    + "WHERE perm_num = ? AND enrollment_id = ? AND yr_qtr = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, perm);
                ps.setInt(2, enrollmentId);
                ps.setString(3, CURRENT_QTR);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Not enrolled in " + cno);
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 3.3) Prevent dropping last course
            String countSql
                    = "SELECT COUNT(*) FROM takes_courses "
                    + "WHERE perm_num = ? AND yr_qtr = ?";
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setString(1, perm);
                ps.setString(2, CURRENT_QTR);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) <= 1) {
                        System.out.println("Cannot drop your last course.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 3.4) Delete
            String delSql
                    = "DELETE FROM takes_courses "
                    + "WHERE perm_num = ? AND enrollment_id = ? AND yr_qtr = ?";
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.setString(1, perm);
                ps.setInt(2, enrollmentId);
                ps.setString(3, CURRENT_QTR);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.printf("✅ Dropped %s for %s%n", cno, perm);
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // 4. Lists all courses the student is enrolled in THIS quarter.
    public void listCurrentCourses(String perm) throws SQLException {
        String sql
                = "SELECT co.cno, co.time_location, co.professor_name "
                + "FROM takes_courses tc "
                + "JOIN courseoffering_offeredin co "
                + "  ON tc.enrollment_id = co.enrollment_id "
                + " AND tc.yr_qtr = co.yr_qtr "
                + "WHERE tc.perm_num = ? AND tc.yr_qtr = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setString(2, CURRENT_QTR);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("📚 Current Courses:");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf(" - %s | %s | Prof. %s%n",
                            rs.getString("cno"),
                            rs.getString("time_location"),
                            rs.getString("professor_name")
                    );
                }
                if (!found) {
                    System.out.println("No current enrollments.");
                }
            }
        }
    }

    // 5. Lists all grades from the PREVIOUS quarter.
    public void listPreviousQuarterGrades(String perm) throws SQLException {
        String sql
                = "SELECT co.cno, tc.grade "
                + "FROM takes_courses tc "
                + "JOIN courseoffering_offeredin co "
                + "  ON tc.enrollment_id = co.enrollment_id "
                + " AND tc.yr_qtr = co.yr_qtr "
                + "WHERE tc.perm_num = ? AND tc.yr_qtr = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setString(2, PREVIOUS_QTR);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Previous Quarter Grades:");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf(" - %s : %s%n",
                            rs.getString("cno"),
                            rs.getString("grade")
                    );
                }
                if (!found) {
                    System.out.println("No grades found for " + PREVIOUS_QTR);
                }
            }
        }
    }

    // 6. Requirements check:
    public void requirementsCheck(String perm) throws SQLException {
        /**
         * In 6: - as the TA said, we make important assumption that all courses
         * in CURRENT_QTR are "passed." - If all mandatory courses are passed
         * AND enough electives are passed, prints "Requirements met: Yes". -
         * Otherwise lists which mandatory remain, and how many electives still
         * needed.
         */

        // 6.1) first, fetch this student's major and how many electives are required
        String majSql
                = "SELECT m.majorname, m.electivecount "
                + "FROM student s JOIN major_managedby m ON s.majorname=m.majorname "
                + "WHERE s.perm_num = ?";
        String major; // the student's major (e.g., CS, ECE)
        int electiveNeeded; // how many electives this major requires
        try (PreparedStatement ps = conn.prepareStatement(majSql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such student " + perm);
                    return;
                }
                major = rs.getString("majorname");
                electiveNeeded = rs.getInt("electivecount");
            }
        }

        // 6.2) then, load 1) mandatory & 2) elective sets for that major
        Set<String> mandatory = new HashSet<>(); // all required courses for this major
        Set<String> electives = new HashSet<>(); // all electives that count for this major
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cno FROM mandatory WHERE majorname = ?")) {
            ps.setString(1, major);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mandatory.add(rs.getString(1));
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cno FROM elective WHERE majorname = ?")) {
            ps.setString(1, major);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    electives.add(rs.getString(1));
                }
            }
        }

        // 6.3) Fetch all courses that are either:
        //    • passed in any prior quarter (grade != NULL and A/B/C), or
        //    • currently enrolled this quarter (yr_qtr = CURRENT_QTR) (assume all courses in CURRENT_QTR are "passed")
        Set<String> passed = new HashSet<>(); // all courses considered "passed" for this check
        String allSql
                = "SELECT co.cno, tc.grade, tc.yr_qtr "
                + "FROM takes_courses tc "
                + " JOIN courseoffering_offeredin co "
                + "   ON tc.enrollment_id = co.enrollment_id "
                + "  AND tc.yr_qtr = co.yr_qtr "
                + "WHERE tc.perm_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(allSql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cno = rs.getString("cno"); // course number
                    String grade = rs.getString("grade"); // grade (may be null if in progress)
                    String yq = rs.getString("yr_qtr"); // year+quarter string
                    // If grade is not null and is A/B/C, count as passed
                    if (grade != null) {
                        char G = grade.charAt(0);
                        if (G == 'A' || G == 'B' || G == 'C') {
                            passed.add(cno);
                        }
                        // If currently enrolled in this quarter, also count as passed (per TA's rule)
                    } else if (yq.equals(CURRENT_QTR)) {
                        passed.add(cno);
                    }
                }
            }
        }

        // 6.4) Which mandatory remain?
        // remMand: set of all required courses for this major that have NOT been passed yet
        Set<String> remMand = new TreeSet<>(mandatory); // TreeSet for sorted output
        remMand.removeAll(passed); // remove all passed courses from the set of required courses

        // 6.5) How many electives passed already?
        // passedElec: count of passed courses that are in the electives set
        int passedElec = 0;
        for (String c : passed) {
            if (electives.contains(c)) {
                passedElec++;
            }
        }
        // remElec: how many electives are still needed (never negative)
        int remElec = Math.max(0, electiveNeeded - passedElec);

        // 6.6 ) Output
        // If no required courses remain and no electives remain, requirements are met
        if (remMand.isEmpty() && remElec == 0) {
            System.out.println("Requirements met: Yes");
        } else {
            // Otherwise, print what is still missing
            System.out.println("Requirements not yet met:");
            if (!remMand.isEmpty()) {
                System.out.println("  Mandatory still to take: " + remMand);
            }
            if (remElec > 0) {
                System.out.println("  Electives still to take: " + remElec);
            }
        }
    }

    // 7. Make a plan:
    public void makePlan(String perm) throws SQLException {
        /**
         * in 7 we start making a plan that begins "next" quarter (we cycle
         * W→F→S repeating(as curr=S25)). -like before we make important
         * assumption that all courses in CURRENT_QTR are "passed." - Greedily
         * picks up to 5 courses per quarter(max a student can take): • first
         * starting with remaining mandatory, • then electives if still needed,
         * but only those whose prerequisites are satisfied by the "passed" set.
         * -we also make the assumption of stopping 12 future quarters(4 year
         * college degree=12 quarters).
         */

        // --- 7.1: compute initial state (same as requirementsCheck) ---
        // Get the student's major and how many electives are required
        String majSql
                = "SELECT m.majorname, m.electivecount "
                + "FROM student s JOIN major_managedby m ON s.majorname=m.majorname "
                + "WHERE s.perm_num = ?";
        String major; // the student's major (e.g., CS, ECE)
        int electiveNeeded; // how many electives this major requires
        try (PreparedStatement ps = conn.prepareStatement(majSql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                major = rs.getString("majorname");
                electiveNeeded = rs.getInt("electivecount");
            }
        }
        // Load all mandatory and elective courses for this major
        Set<String> mandatory = new HashSet<>(); // all required courses for this major
        Set<String> electives = new HashSet<>(); // all electives that count for this major
        Set<String> passed = new HashSet<>();    // all courses considered "passed" for planning
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cno FROM mandatory WHERE majorname=?")) {
            ps.setString(1, major);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mandatory.add(rs.getString(1));
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cno FROM elective WHERE majorname=?")) {
            ps.setString(1, major);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    electives.add(rs.getString(1));
                }
            }
        }
        // Load all courses the student has passed (A/B/C) or is currently enrolled in (CURRENT_QTR)
        String allSql
                = "SELECT co.cno, tc.grade, tc.yr_qtr "
                + "FROM takes_courses tc "
                + " JOIN courseoffering_offeredin co "
                + "   ON tc.enrollment_id = co.enrollment_id "
                + "  AND tc.yr_qtr      = co.yr_qtr "
                + "WHERE tc.perm_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(allSql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cno = rs.getString("cno"); // course number
                    String grade = rs.getString("grade"); // grade (may be null if in progress)
                    String yq = rs.getString("yr_qtr"); // year+quarter string
                    // If grade is not null and is A/B/C, count as passed
                    if (grade != null) {
                        char G = grade.charAt(0);
                        if (G == 'A' || G == 'B' || G == 'C') {
                            passed.add(cno);
                        }
                        // If currently enrolled in this quarter, also count as passed
                    } else if (yq.equals(CURRENT_QTR)) {
                        passed.add(cno);
                    }
                }
            }
        }
        // Compute which mandatory and electives remain
        Set<String> remMand = new TreeSet<>(mandatory); // required courses not yet passed
        remMand.removeAll(passed);
        int passedElec = 0; // number of electives already passed
        for (String c : passed) {
            if (electives.contains(c)) {
                passedElec++;
            }
        }
        int remElec = electiveNeeded - passedElec; // electives still needed
        // If nothing remains, print and exit
        if (remMand.isEmpty() && remElec <= 0) {
            System.out.println("Already eligible to graduate — no plan needed.");
            return;
        }

        //as you can see 7.1 was similar to requirements Check
        // --- 7.2: load prerequisites map ---
        // we basically build a map: course -> set of its prerequisites 
        Map<String, Set<String>> prereqs = new HashMap<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT cid,pid FROM prerequisites")) {
            while (rs.next()) {
                String cid = rs.getString(1); // course id
                String pid = rs.getString(2); // prerequisite id
                prereqs.computeIfAbsent(cid, x -> new HashSet<>()).add(pid);
            }
        }

        // --- 7.3: build next-12-quarter cycle ---
        // This will generate the next 12 quarter suffixes (W, F, S) after CURRENT_QTR
        char[] cycle = {'W', 'F', 'S'}; // order of quarters
        char currTerm = CURRENT_QTR.charAt(CURRENT_QTR.length() - 1); // e.g., 'S' for Spring
        int idx = (new String(cycle)).indexOf(currTerm); // find current quarter in cycle
        List<String> futureTerms = new ArrayList<>(); // list of next 12 quarter suffixes
        for (int i = 0; i < 12; i++) {
            idx = (idx + 1) % cycle.length;
            futureTerms.add(cycle[idx] + "");  // "W","F","S",...
        }

        // --- 7.4: greedy plan construction ---
        // plan: quarter suffix -> list of courses to take in that quarter
        Map<String, List<String>> plan = new LinkedHashMap<>();
        for (String term : futureTerms) {
            // Stop if all requirements are met
            if (remMand.isEmpty() && remElec <= 0) {
                break;
            }

            // 1) Find all courses offered in this term (by suffix, e.g., 'W')
            List<String> offered = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DISTINCT cno FROM courseoffering_offeredin WHERE substr(yr_qtr,-1)=?")) {
                ps.setString(1, term);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        offered.add(rs.getString(1));
                    }
                }
            }

            // 2) Filter to those whose prerequisites are all in 'passed' and not already passed
            List<String> avail = new ArrayList<>(); // available to take this quarter
            for (String c : offered) {
                Set<String> req = prereqs.getOrDefault(c, Collections.emptySet()); // prereqs for this course
                // Only add if not already passed and all prereqs are passed
                if (!passed.contains(c) && passed.containsAll(req)) {
                    avail.add(c);
                }
            }

            // 3) Pick up to 5: mandatory first, then electives
            List<String> pick = new ArrayList<>(); // courses to take this quarter
            // a) Add required courses first
            for (String c : avail) {
                if (pick.size() < 5 && remMand.contains(c)) {
                    pick.add(c);
                }
            }
            // b) Then fill with electives if needed
            for (String c : avail) {
                if (pick.size() >= 5) {
                    break;
                }
                if (!remMand.contains(c) && electives.contains(c) && remElec > 0) {
                    pick.add(c);
                }
            }
            // If nothing can be taken this quarter, skip
            if (pick.isEmpty()) {
                continue;
            }

            // 4) Record this quarter's plan
            plan.put(term, pick);

            // 5) Simulate passing these courses (so they count for future quarters)
            for (String c : pick) {
                passed.add(c);
                if (remMand.remove(c)) {
                    /* removed one required */ } else {
                    remElec--;
                }
            }
        }

        // --- 7.5: print plan ---
        System.out.println("Study Plan (after " + CURRENT_QTR + "):");
        if (plan.isEmpty()) {
            System.out.println("  No viable plan found in next 12 quarters.");
        } else {
            plan.forEach((q, list)
                    -> System.out.printf("  %s -> %s%n", q, list)
            );
        }
    }

    // ----------- PIN MANAGEMENT (8-10)------------

    //8- hash function
    private String hashPin(String pin) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing PIN", e);
        }
    }

    //9- verify pin(never deals with plain-text PINs directly from the database. It only compares hashes)
    public boolean verifyPin(String perm, String pin) throws SQLException {
        // Hash the input PIN before comparing with stored hash
        String hashedPin = hashPin(pin);
        String sql = "SELECT 1 FROM student WHERE perm_num = ? AND pin = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setString(2, hashedPin); // Compare with hashed PIN
            try (ResultSet rs = ps.executeQuery()) {
                boolean valid = rs.next();
                System.out.println(valid ? "PIN verified" : "Invalid credentials");
                return valid;
            }
        }
    }

    //10- set pin (only way to change a student's PIN)
    public boolean setPin(String perm, String oldPin, String newPin) throws SQLException {
        // 1) Verify the old PIN first (for security)
        if (!verifyPin(perm, oldPin)) {
            System.out.println("Cannot change PIN: old PIN does not match.");
            return false;
        }

        // 2) Validate new PIN format (4-5 digits)
        if (newPin == null || !newPin.matches("\\d{4,5}")) {
            System.out.println("Invalid PIN format. PIN must be 4-5 digits.");
            return false;
        }

        // 3) Hash the new PIN before storing
        String hashedNewPin = hashPin(newPin);
        String upd = "UPDATE student SET pin = ? WHERE perm_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setString(1, hashedNewPin); // Store hashed PIN in database 
            ps.setString(2, perm);
            ps.executeUpdate();
        }

        System.out.println("PIN changed successfully.");
        return true;
    }

    /**
     * Enters grades for all students in a course from a JSON file. Expected
     * JSON format - similar to slack: { "enrollment_id": 56789, "yr_qtr":
     * "25W", "grades": [ { "perm": "1234567", "grade": "B" }, { "perm":
     * "1468222", "grade": "A" } ] }
     *
     * for correct usage make sure add the JSON library dependency to the
     * pom.xml file. Add this inside the <dependencies> section
     * <dependency>
     * <groupId>org.json</groupId>
     * <artifactId>json</artifactId>
     * <version>20231013</version>
     * </dependency>
     * Then run mvn clean install to make sure the JSON library is included. do
     * i need to add validation here for duplicate grades (ie if the same
     * student shows up twice)
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

            String verifySql
                    = "SELECT 1 FROM courseoffering_offeredin WHERE enrollment_id = ?";

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

                String updateSql
                        = "UPDATE takes_courses SET grade = ? WHERE perm_num = ? AND enrollment_id = ? AND yr_qtr = ?";

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
        String sql
                = "SELECT co.cno, c.en_code, co.max_enrollment, "
                + "       co.professor_name, co.time_location, tc.grade, co.yr_qtr "
                + "FROM takes_courses tc, courseoffering_offeredin co, course c "
                + "WHERE tc.enrollment_id = co.enrollment_id "
                + "AND tc.yr_qtr = co.yr_qtr "
                + "AND co.cno = c.cno "
                + "AND tc.perm_num = ? "
                + "AND tc.grade IS NOT NULL "
                + "ORDER BY co.yr_qtr DESC, co.cno";  //look into changing to true chronological ordering

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
                    String professor = rs.getString("professor_name") != null
                            ? rs.getString("professor_name") : "TBA";
                    String timeLocation = rs.getString("time_location") != null
                            ? rs.getString("time_location") : "TBA";

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
     * Generates grade mailers for all students in a given quarter, registrar
     * feature
     */
    public void generateGradeMailers(String yrQtr) throws SQLException {
        // First query: Gets all students who have grades for the specified quarter
        // Returns: student ID, name, and major for students with non-null grades
        String sql
                = "SELECT DISTINCT s.perm_num, s.name, s.majorname "
                + "FROM student s, takes_courses tc "
                + "WHERE s.perm_num = tc.perm_num "
                + "AND tc.yr_qtr = ? "
                + "AND tc.grade IS NOT NULL "
                + "ORDER BY s.perm_num";

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
                    
                    // This query retrieves detailed course information for a specific student in a given quarter. It joins four tables:
                    // takes_courses (for enrollment and grade info), student (for student details), 
                    // courseoffering_offeredin (for course offering details like professor), and course (for course details).
                    // For each course, it returns the student's ID and name, course number, enrollment code, grade received,
                    // professor name (shows "TBA" if null), and quarter. The results are filtered by student ID and quarter,
                    // and sorted by course number for consistent display.
                    String courseSql
                            = "SELECT tc.perm_num, s.name, co.cno, c.en_code, tc.grade, co.professor_name, co.yr_qtr "
                            + "FROM takes_courses tc, student s, courseoffering_offeredin co, course c "
                            + "WHERE tc.perm_num = s.perm_num "
                            + "AND tc.enrollment_id = co.enrollment_id "
                            + "AND tc.yr_qtr = co.yr_qtr "
                            + "AND co.cno = c.cno "
                            + "AND tc.perm_num = ? "
                            + "AND tc.yr_qtr = ? "
                            + "ORDER BY co.cno";

                    try (PreparedStatement psCourses = conn.prepareStatement(courseSql)) {
                        psCourses.setString(1, perm);
                        psCourses.setString(2, yrQtr);

                        try (ResultSet rsCourses = psCourses.executeQuery()) {
                            System.out.println("Course | Enrollment Id   | Professor        | Grade");
                            System.out.println("-------|-----------------|------------------|-------");

                            while (rsCourses.next()) {
                                String cno = rsCourses.getString("cno");
                                String title = rsCourses.getString("en_code");
                                String professor = rsCourses.getString("professor_name") != null
                                        ? rsCourses.getString("professor_name") : "TBA";
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

    //list every student in a course, regsitrar
    public void listStudentsInCourse(String courseNumber, String quarter) throws SQLException {
        // 1. Inner query: Gets enrollment_id(s) for the given course number (cno)
        // 2. Middle query: Gets perm_num(s) of students enrolled in those course offerings for the given quarter
        // 3. Outer query: Gets the student details (perm_num and name) for those students
        String sql = "SELECT DISTINCT s.perm_num, s.name " +
                    "FROM student s " +
                    "WHERE s.perm_num IN (" +
                    "    SELECT tc.perm_num " +
                    "    FROM takes_courses tc " +
                    "    WHERE tc.enrollment_id IN (" +
                    "        SELECT co.enrollment_id " +
                    "        FROM courseoffering_offeredin co " +
                    "        WHERE co.cno = ? " +
                    "    ) " +
                    "    AND tc.yr_qtr = ?" +
                    ")";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseNumber);
            ps.setString(2, quarter);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nStudents enrolled in " + courseNumber + " (" + quarter + "):");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("  %s - %s%n", 
                        rs.getString("perm_num"), 
                        rs.getString("name"));
                }
                if (!found) {
                    System.out.println("  No students enrolled in this course.");
                }
            }
        }
    }

    //dummy function to print hashed pins for all students, just a utility to help hard coding of hashed pins 
    // public void printHashedPins() throws SQLException {
    //     String sql = "SELECT perm_num, pin FROM student";
    //     try (Statement stmt = conn.createStatement();
    //          ResultSet rs = stmt.executeQuery(sql)) {
            
    //         System.out.println("\nCurrent PINs and their hashed versions:");
    //         System.out.println("----------------------------------------");
            
    //         while (rs.next()) {
    //             String perm = rs.getString("perm_num");
    //             String currentPin = rs.getString("pin");
    //             String hashedPin = hashPin(currentPin);
                
    //             System.out.printf("Student %s:%n", perm);
    //             System.out.printf("  Current PIN: %s%n", currentPin);
    //             System.out.printf("  Hashed PIN: %s%n", hashedPin);
    //             System.out.println("----------------------------------------");
    //         }
    //     }
    // }
    

}
