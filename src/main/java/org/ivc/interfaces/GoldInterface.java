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
        System.out.println("Type 'help' for available commands");
        
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            
            try {
                switch (command) {
                    case "help":
                        showHelp();
                        break;
                        
                    case "list":
                        if (parts.length < 2) {
                            System.out.println("Usage: list <student_id>");
                        } else {
                            db.listCurrentCourses(parts[1]);
                        }
                        break;
                        
                    case "grades":
                        if (parts.length < 2) {
                            System.out.println("Usage: grades <student_id>");
                        } else {
                            db.listPreviousQuarterGrades(parts[1]);
                        }
                        break;
                        
                    case "add":
                        if (parts.length < 3) {
                            System.out.println("Usage: add <student_id> <course_number>");
                        } else {
                            db.addCourse(parts[1], parts[2]);
                        }
                        break;
                        
                    case "drop":
                        if (parts.length < 3) {
                            System.out.println("Usage: drop <student_id> <course_number>");
                        } else {
                            db.dropCourse(parts[1], parts[2]);
                        }
                        break;
                        
                    case "check":
                        if (parts.length < 2) {
                            System.out.println("Usage: check <student_id>");
                        } else {
                            db.requirementsCheck(parts[1]);
                        }
                        break;
                        
                    case "plan":
                        if (parts.length < 2) {
                            System.out.println("Usage: plan <student_id>");
                        } else {
                            db.makePlan(parts[1]);
                        }
                        break;
                        
                    case "pin":
                        if (parts.length < 4) {
                            System.out.println("Usage: pin <student_id> <old_pin> <new_pin>");
                        } else {
                            db.setPin(parts[1], parts[2], parts[3]);
                        }
                        break;
                        
                    case "exit":
                        System.out.println("Goodbye!");
                        return;
                        
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    private void showHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  list <student_id>                    - List current courses");
        System.out.println("  grades <student_id>                  - View previous quarter grades");
        System.out.println("  add <student_id> <course>           - Add a course");
        System.out.println("  drop <student_id> <course>          - Drop a course");
        System.out.println("  check <student_id>                  - Check degree requirements");
        System.out.println("  plan <student_id>                   - Generate study plan");
        System.out.println("  pin <student_id> <old> <new>        - Change PIN");
        System.out.println("  help                                - Show this help message");
        System.out.println("  exit                                - Exit the program");
    }
}
