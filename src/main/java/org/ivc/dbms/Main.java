// File: src/main/java/org/ivc/dbms/Main.java
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
    private static final String WALLET_DIR = "wallet_CS174ADB";

    // 2) Build the JDBC URL using alias + TNS_ADMIN
    private static final String DB_URL
            = "jdbc:oracle:thin:@" + DB_ALIAS + "?TNS_ADMIN=" + WALLET_DIR;

    // 3) Your DB credentials
    private static final String DB_USER = "ADMIN";
    private static final String DB_PASSWORD = "Helloworld@1234";

    public static void main(String[] args) {
        // Set up connection properties
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("oracle.net.ssl_server_dn_match", "true");

        System.out.println("Connecting to: " + DB_URL);
        // 4) Open connection (AutoCloseable)
        try (Connection conn = DriverManager.getConnection(DB_URL, props)) {
            System.out.println("✅ Connection established!");

            // 5) Create your manager and call its methods
            DatabaseManager db = new DatabaseManager(conn);
            db.listAllStudents();                     // print students
            // boolean ok = db.addCourse("14682", "CS130");  // try enroll
            // System.out.println("addCourse returned: " + ok);
            // boolean test_delete = db.dropCourse("14682", "CS130");  // trying delete
            // System.out.println("dropCourse returned: " + test_delete);
             db.listAllCourses("1234567");
            // System.out.println("listCurrentCourses returned: " + currClasses);



            // 6) Clean up
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
