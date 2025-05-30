package org.ivc.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.ivc.interfaces.RegistrarInterface;
import org.ivc.interfaces.GoldInterface;



/**
 * Application entry point: opens JDBC connection, calls DatabaseManager
 * methods, then closes.
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
        // set up SSL/TLS properties for Oracle wallet
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("oracle.net.ssl_server_dn_match", "true");

        System.out.println("Connecting to: " + DB_URL);
        try (Connection conn = DriverManager.getConnection(DB_URL, props)) {
            System.out.println("Connection established!");

            DatabaseManager db = new DatabaseManager(conn);

            // // 1. List all students
            // System.out.println("\n=== All Students ===");
            // db.listAllStudents();
            // // 2. Enroll Alfred in CS174 and CS026
            // System.out.println("\n=== Enroll Alfred (12345) in CS174 and CS026 ===");
            // db.addCourse("12345", "CS174");
            // db.addCourse("12345", "CS026");
            // System.out.println("\n=== Alfred's Current Courses ===");
            // db.listCurrentCourses("12345");
            // // 3. Try duplicate enrollment (should fail)
            // System.out.println("\n=== Try duplicate enrollment: Alfred → CS170 again ===");
            // db.addCourse("12345", "CS170");
            // // 4. Drop Alfred from CS174 and CS026
            // System.out.println("\n=== Alfred drops CS174 ===");
            // db.dropCourse("12345", "CS174");
            // System.out.println("\n=== Alfred drops CS026 ===");
            // db.dropCourse("12345", "CS026");
            // System.out.println("\n=== Alfred's Current Courses ===");
            // db.listCurrentCourses("12345");
            // 5. List the previous quarter grades for Alfred
            // System.out.println("\n=== Billy's Previous Quarter Grades ===");
            // db.listPreviousQuarterGrades("14682");
            // // 6. Check requirements for Billy
            // System.out.println("\n=== Billy's Requirements Check ===");
            // db.requirementsCheck("14682");
            // // 7. Make a plan for Billy
            // System.out.println("\n=== Billy's Study Plan ===");
            // db.makePlan("14682");
            // ... existing code ...
            // System.out.println("\n=== Joe Pepsi's Requirements Check ===");
            // db.requirementsCheck("36912");
            // System.out.println("\n=== Joe Pepsi's Study Plan ===");
            // db.makePlan("36912");
            // // ... existing code ...

            // 8. Test PIN verification
            // System.out.println("\n=== Test PIN Verification (Alfred, 12345) ===");
            // // Scenario 1: Correct PIN (initial PIN is '00000', assuming it's hashed in your DB)
            // // If your DB has plain text '00000', this will fail until you hash it.
            // // To test with unhashed PINs in DB: comment out hashPin in verifyPin and compare directly.
            // db.verifyPin("12345", "12345"); // Should be true if DB has HASH    of "12345"
            // // Scenario 2: Incorrect PIN
            // db.verifyPin("12345", "wrong"); // Should be false

            // // 9. Test Set PIN
            // System.out.println("\n=== Test Set PIN (Alfred, 12345) ===");
            // // Scenario 1: Correct old PIN, set new PIN
            // db.setPin("12345", "12345", "54321"); // Changes PIN to HASH of "54321"
            // // Scenario 2: Verify new PIN
            // db.verifyPin("12345", "54321"); // Should be true
            // // Scenario 3: Incorrect old PIN
            // db.setPin("12345", "wrong", "11111"); // Should fail
            // // Scenario 4: Restore original PIN for consistency in other tests
            // db.setPin("12345", "54321", "12345");
            // ... existing code ...

            // // 9. Try to drop Alfred's last course (should fail)
            // System.out.println("\n=== Try to drop Alfred's last course (CS026) ===");
            // db.dropCourse("12345", "CS026");
            // // 10. List current courses for Billy
            // System.out.println("\n=== Billy's Current Courses ===");
            // db.listCurrentCourses("14682");
            // // 11. List previous quarter grades for Billy
            // System.out.println("\n=== Billy's Previous Quarter Grades ===");
            // db.listPreviousQuarterGrades("14682");
            // clean up
            // RegistrarInterface registrar = new RegistrarInterface(db);
            // registrar.start();
            // GoldInterface gold = new GoldInterface(db);
            // gold.start();


            //move this, temporary for now
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\nWelcome to the Student Registration System");
                System.out.println("Please select interface:");
                System.out.println("1. Student Interface (Gold)");
                System.out.println("2. Registrar Interface");
                System.out.println("3. Exit");
                
                System.out.print("\nSelect interface (1-3): ");
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        System.out.println("\nStarting Student Interface...");
                        GoldInterface goldInterface = new GoldInterface(db);
                        goldInterface.start();
                        break;
                        
                    case "2":
                        System.out.println("\nStarting Registrar Interface...");
                        RegistrarInterface registrarInterface = new RegistrarInterface(db);
                        registrarInterface.start();
                        break;
                        
                    case "3":
                        System.out.println("Goodbye!");
                        scanner.close();
                        db.close();
                        return;
                        
                    default:
                        System.out.println("Invalid choice. Please select 1, 2, or 3.");
                }
            }

            //pin  verification come bakc to if needed
            // db.setPin("12345", "12345", "12345");
            // db.verifyPin("12345", "12345");
            // db.printHashedPins();
            //db.close(); //commenting for now

        } catch (SQLException e) {
            System.out.println(" SQL error:");
            e.printStackTrace();
        }
    }
}
