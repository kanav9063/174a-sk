package org.ivc.interfaces;

import java.sql.SQLException;
import java.util.Scanner;

import org.ivc.dbms.DatabaseManager;

public class GoldInterface {

    private final DatabaseManager db;
    private final Scanner scanner;
    private String currentStudent;

    public GoldInterface(DatabaseManager db) {
        this.db = db;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Student Interface");
        System.out.println("Please login first");

        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }

        System.out.println("Type 'help' for available commands");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        showHelp();
                        break;

                    case "list":
                        db.listCurrentCourses(currentStudent);
                        break;

                    case "grades":
                        if (parts.length < 2) {
                            System.out.println("Usage: grades <quarter>");
                        } else {
                            db.listQuarterGrades(currentStudent, parts[1]);
                        }
                        break;

                    case "add":
                        if (parts.length < 2) {
                            System.out.println("Usage: add <course_number>");
                        } else {
                            db.addCourse(currentStudent, parts[1]);
                        }
                        break;

                    case "drop":
                        if (parts.length < 2) {
                            System.out.println("Usage: drop <course_number>");
                        } else {
                            db.dropCourse(currentStudent, parts[1]);
                        }
                        break;

                    case "check":
                        db.requirementsCheck(currentStudent);
                        break;

                    case "plan":
                        db.makePlan(currentStudent);
                        break;

                    case "pin":
                        if (parts.length < 3) {
                            System.out.println("Usage: pin <old_pin> <new_pin>");
                        } else {
                            db.setPin(currentStudent, parts[1], parts[2]);
                        }
                        break;

                    case "logout":
                        System.out.println("Logged out. Goodbye!");
                        return;

                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private boolean login() {
        System.out.print("Enter Student ID: ");
        String perm = scanner.nextLine().trim();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            if (db.verifyPin(perm, pin)) {
                currentStudent = perm;
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    private void showHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  list                    - List current courses");
        System.out.println("  grades <quarter>        - View grades for a specific quarter");
        System.out.println("  add <course>           - Add a course");
        System.out.println("  drop <course>          - Drop a course");
        System.out.println("  check                  - Check degree requirements");
        System.out.println("  plan                   - Generate study plan");
        System.out.println("  pin <old> <new>        - Change PIN");
        System.out.println("  help                   - Show this help message");
        System.out.println("  logout                 - Log out and exit");
    }
}
