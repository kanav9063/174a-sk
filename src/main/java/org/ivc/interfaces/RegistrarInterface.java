package org.ivc.interfaces;

import java.sql.SQLException;
import java.util.Scanner;

import org.ivc.dbms.DatabaseManager;

public class RegistrarInterface {
    private final DatabaseManager db;
    private final Scanner scanner;
    
    public RegistrarInterface(DatabaseManager db) {
        this.db = db;
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        System.out.println("Welcome to the Registrar Interface");
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
                        
                    case "transcript":
                        if (parts.length < 2) {
                            System.out.println("Usage: transcript <student_id>");
                        } else {
                            db.requestTranscript(parts[1]);
                        }
                        break;
                        
                    case "mailer":
                        if (parts.length < 2) {
                            System.out.println("Usage: mailer <quarter> (e.g., mailer 25W)");
                        } else {
                            db.generateGradeMailers(parts[1]);
                        }
                        break;
                        
                    case "grades":
                        if (parts.length < 2) {
                            System.out.println("Usage: grades <filename>");
                        } else {
                            db.enterGradesFromFile(parts[1]);
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

                    case "list":
                        if (parts.length < 2) {
                            System.out.println("Usage: list <student_id>");
                        } else {
                            db.listCurrentCourses(parts[1]);
                        }
                        break;

                    case "studentgrades":
                        if (parts.length < 2) {
                            System.out.println("Usage: studentgrades <student_id>");
                        } else {
                            db.listPreviousQuarterGrades(parts[1]);
                        }
                        break;
                        
                    case "exit":
                    case "quit":
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
        System.out.println("  transcript <student_id>  - View student transcript");
        System.out.println("  mailer <quarter>        - Generate grade mailers for quarter (e.g., mailer 25W)");
        System.out.println("  grades <filename>       - Enter grades from JSON file");
        System.out.println("  add <student_id> <course> - Add student to course");
        System.out.println("  drop <student_id> <course> - Drop student from course");
        System.out.println("  list <student_id>       - List student's current courses");
        System.out.println("  studentgrades <student_id> - View student's previous quarter grades");
        System.out.println("  help                    - Show this help message");
        System.out.println("  exit/quit              - Exit the program");
    }
}