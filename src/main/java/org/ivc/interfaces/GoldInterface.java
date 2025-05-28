// package org.ivc.dbms;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;
// import java.util.Scanner;

// public class GoldInterface {
//     private static final String DB_URL      = "jdbc:oracle:thin:@cs174adb_tp?TNS_ADMIN=wallet_CS174ADB";
//     private static final String DB_USER     = "ADMIN";
//     private static final String DB_PASSWORD = "Helloworld@1234";

//     public static void main(String[] args) {
//         try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//              Scanner sc     = new Scanner(System.in)) {

//             DatabaseManager db = new DatabaseManager(conn);

//             // 1) Login
//             System.out.print("Enter Student ID > ");
//             String perm = sc.nextLine().trim();
//             System.out.print("Enter PIN        > ");
//             String pin  = sc.nextLine().trim();

//             if (!db.verifyPin(perm, pin)) {
//                 System.out.println(" Invalid ID/PIN. Exiting.");
//                 return;
//             }

//             System.out.println("Welcome, " + db.getStudentName(perm) + "!");
//             System.out.println("Type 'help' for commands.");

//             // 2) Command loop
//             while (true) {
//                 System.out.print("> ");
//                 String line = sc.nextLine().trim();
//                 if (line.isEmpty()) continue;

//                 String[] parts = line.split("\\s+");
//                 String cmd = parts[0].toLowerCase();

//                 switch (cmd) {
//                     case "help":
//                         System.out.println("Commands: list, grades, add <course>, drop <course>, check, plan, changepin, logout");
//                         break;

//                     case "list":
//                         db.listCurrentCourses(perm);
//                         break;

//                     case "grades":
//                         db.listPreviousGrades(perm);
//                         break;

//                     case "add":
//                         if (parts.length < 2) {
//                             System.out.println("Usage: add <COURSE_NO>");
//                         } else {
//                             db.addCourse(perm, parts[1]);
//                         }
//                         break;

//                     case "drop":
//                         if (parts.length < 2) {
//                             System.out.println("Usage: drop <COURSE_NO>");
//                         } else {
//                             db.dropCourse(perm, parts[1]);
//                         }
//                         break;

//                     case "check":
//                         db.requirementsCheck(perm);
//                         break;

//                     case "plan":
//                         db.makePlan(perm);
//                         break;

//                     case "changepin":
//                         System.out.print("Enter current PIN > ");
//                         String oldPin = sc.nextLine().trim();
//                         System.out.print("Enter new PIN     > ");
//                         String newPin = sc.nextLine().trim();
//                         db.changePin(perm, oldPin, newPin);
//                         break;

//                     case "logout":
//                         System.out.println(" Goodbye!");
//                         db.close();
//                         return;

//                     default:
//                         System.out.println("Unknown command. Type 'help'.");
//                 }
//             }

//         } catch (SQLException e) {
//             System.err.println("Database error:");
//             e.printStackTrace();
//         }
//     }
// }
