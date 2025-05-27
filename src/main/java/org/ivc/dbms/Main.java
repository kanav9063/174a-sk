package org.ivc.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Application entry point: opens JDBC connection, calls DatabaseManager, then
 * closes.
 */
public class Main {

    // 1) Your database alias (from Oracle Cloud) + wallet path
    private static final String DB_ALIAS = "cs174adb_low";
    private static final String WALLET_DIR = "/Users/quicktech/Desktop/cs174a-project/wallet_CS174ADB";

    // 2) Build the JDBC URL using alias + TNS_ADMIN
    private static final String DB_URL
            = "jdbc:oracle:thin:@" + DB_ALIAS + "?TNS_ADMIN=" + WALLET_DIR;

    // 3) Your DB credentials
    private static final String DB_USER = "ADMIN";
    private static final String DB_PASSWORD = "Helloworld@1234";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("oracle.net.ssl_server_dn_match", "true");

        System.out.println("Connecting to: " + DB_URL);

        try (Connection conn = DriverManager.getConnection(DB_URL, props)) {
            System.out.println("✅ Connection established!");

            DatabaseManager db = new DatabaseManager(conn);

            // 1. List all students
            System.out.println("\n=== All Students ===");
            db.listAllStudents();

            // 2. Enroll Alfred in CS130 and CS026
            System.out.println("\n=== Enroll Alfred (1234567) in CS130 and CS026 ===");
            db.addCourse("1234567", "CS130");
            db.addCourse("1234567", "CS026");

            // 3. Enroll Billy in CS026
            System.out.println("\n=== Enroll Billy (1468222) in CS026 ===");
            db.addCourse("1468222", "CS026");

            // 4. Try duplicate enrollment (should fail)
            System.out.println("\n=== Try duplicate enrollment (Alfred → CS130 again) ===");
            db.addCourse("1234567", "CS130");

            // 5. List current courses for Alfred
            System.out.println("\n=== Alfred's Current Courses ===");
            db.listCurrentCourses("1234567");

            // 6. Drop Alfred from CS130
            System.out.println("\n=== Alfred drops CS130 ===");
            db.dropCourse("1234567", "CS130");

            // 7. List current courses for Alfred after drop
            System.out.println("\n=== Alfred's Current Courses (After Drop) ===");
            db.listCurrentCourses("1234567");

            // 8. List previous quarter grades for Alfred
            System.out.println("\n=== Alfred's Previous Quarter Grades ===");
            db.listPreviousQuarterGrades("1234567");

            // 9. Try to drop Alfred's last course (should fail)
            System.out.println("\n=== Try to drop Alfred's last course (should fail) ===");
            db.dropCourse("1234567", "CS026");

            // 10. List current courses for Billy
            System.out.println("\n=== Billy's Current Courses ===");
            db.listCurrentCourses("1468222");

            // 11. List previous quarter grades for Billy
            System.out.println("\n=== Billy's Previous Quarter Grades ===");
            db.listPreviousQuarterGrades("1468222");

            db.close();

        } catch (SQLException e) {
            System.out.println("❌ SQL error:");
            e.printStackTrace();
        }
    }
}
