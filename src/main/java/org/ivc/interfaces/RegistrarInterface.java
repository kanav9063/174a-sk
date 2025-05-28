package org.ivc.interfaces;

import java.util.Scanner;
import java.sql.SQLException;
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
        System.out.println("  help                    - Show this help message");
        System.out.println("  exit/quit              - Exit the program");
    }
}
