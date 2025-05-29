package org.ivc.dbms;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
                System.out.println("📝 Previous Quarter Grades:");
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
    //8- hash pin
    private String hashPin(String pin) {
        /**
         * hashPin: Hashes a 4- or 5-digit PIN using SHA-256 and returns the hex
         * string. This ensures that the PIN is never stored or compared in
         * plain text. Only the hash is stored in the database for security.
         */
        try {
            // Create a SHA-256 message digest instance
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Hash the PIN as bytes (UTF-8)
            byte[] digest = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            // Convert the hash bytes to a hex string
            StringBuilder sb = new StringBuilder(2 * digest.length);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java, so this should never happen
            throw new RuntimeException(e);
        }
    }

    //9- verify pin(never deals with plain-text PINs directly from the database. It only compares hashes)   
    public boolean verifyPin(String perm, String pin) throws SQLException {

        /**
         * verifyPin: Checks if the supplied plain-text PIN matches the stored
         * (hashed) PIN for student `perm`. Returns true if the PIN is correct,
         * false otherwise. Only this method and setPin should ever access the
         * PIN.
         */
        // 1) Fetch the stored hashed PIN for this student
        String sql = "SELECT pin FROM student WHERE perm_num = ?";
        String storedHash;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such student.");
                    return false;
                }
                storedHash = rs.getString("pin");
            }
        }
        // 2) Hash the supplied PIN and compare to the stored hash
        String suppliedHash = hashPin(pin);
        boolean ok = suppliedHash.equals(storedHash);
        System.out.println(ok ? "PIN verified" : "PIN incorrect");
        return ok;
    }

    //10- set pin (only way to change a student's PIN)
    public boolean setPin(String perm, String oldPin, String newPin) throws SQLException {

        /**
         * setPin: Changes the PIN from oldPin to newPin if oldPin is correct.
         * Returns true on success, false otherwise. This is the only way to
         * change a student's PIN.
         */
        // 1) Verify the old PIN first (for security)
        if (!verifyPin(perm, oldPin)) {
            System.out.println("Cannot change PIN: old PIN does not match.");
            return false;
        }
        // 2) Hash the new PIN and update it in the database
        String upd = "UPDATE student SET pin = ? WHERE perm_num = ?";
        String newHash = hashPin(newPin);
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setString(1, newHash);
            ps.setString(2, perm);
            ps.executeUpdate();
        }
        System.out.println("PIN changed successfully.");
        return true;
    }

}
