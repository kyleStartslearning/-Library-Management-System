package project;

import project.Databases.Connect;
import project.Databases.Admin;
import project.Databases.Book;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Console-based testing application for database operations
 */
public class ConsoleTest {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║    Library Database Test Tool    ║");
        System.out.println("╚══════════════════════════════════╝");
        
        // Initialize database
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        if (connection == null) {
            System.out.println("❌ Failed to connect to database!");
            return;
        }
        
        System.out.println("✅ Database connected successfully!");
        
        // Interactive menu
        while (true) {
            showMenu();
            int choice = getChoice();
            
            switch (choice) {
                case 1 -> testAuthentication();
                case 2 -> testAddBook();
                case 3 -> viewAllBooks();
                case 4 -> viewStatistics();
                case 5 -> testAuditTrail();
                case 6 -> {
                    System.out.println("Goodbye!");
                    dbConnection.closeConnection();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void showMenu() {
        System.out.println("\n═══════════════════════════════════");
        System.out.println("Choose an option:");
        System.out.println("1. Test Authentication");
        System.out.println("2. Add a Book");
        System.out.println("3. View All Books");
        System.out.println("4. View Statistics");
        System.out.println("5. View Audit Trail");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }
    
    private static int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void testAuthentication() {
        System.out.println("\n--- Authentication Test ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        try {
            Admin admin = Admin.authenticateAdmin(email, password);
            if (admin != null) {
                System.out.println("✅ Authentication successful!");
                System.out.println("Welcome, " + admin.getName());
            } else {
                System.out.println("❌ Authentication failed!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void testAddBook() {
        System.out.println("\n--- Add Book Test ---");
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        System.out.print("Enter number of copies: ");
        int copies = Integer.parseInt(scanner.nextLine());
        
        boolean success = Book.addBook(title, author, copies, "system@library.com", "admin");
        
        if (success) {
            System.out.println("✅ Book added successfully!");
        } else {
            System.out.println("❌ Failed to add book!");
        }
    }
    
    private static void viewAllBooks() {
        System.out.println("\n--- All Books ---");
        
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books_with_user_info")) {
            ResultSet rs = stmt.executeQuery();
            
            System.out.printf("%-30s %-20s %-15s %-15s%n", "Title", "Author", "Copies", "Added By");
            System.out.println("─".repeat(80));
            
            while (rs.next()) {
                System.out.printf("%-30s %-20s %-15s %-15s%n",
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("total_copies"),
                    rs.getString("added_by_name")
                );
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void viewStatistics() {
        System.out.println("\n--- Library Statistics ---");
        
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM library_statistics")) {
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Total Members: " + rs.getInt("total_members"));
                System.out.println("Total Books: " + rs.getInt("total_books"));
                System.out.println("Total Copies: " + rs.getInt("total_book_copies"));
                System.out.println("Available Copies: " + rs.getInt("available_copies"));
                System.out.println("Currently Borrowed: " + rs.getInt("currently_borrowed"));
                System.out.println("Books by Admins: " + rs.getInt("books_added_by_admins"));
                System.out.println("Books by Members: " + rs.getInt("books_added_by_members"));
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void testAuditTrail() {
        System.out.println("\n--- Audit Trail ---");
        
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user_book_contributions")) {
            ResultSet rs = stmt.executeQuery();
            
            System.out.printf("%-20s %-10s %-15s%n", "User", "Type", "Books Added");
            System.out.println("─".repeat(45));
            
            while (rs.next()) {
                System.out.printf("%-20s %-10s %-15s%n",
                    rs.getString("user_name"),
                    rs.getString("added_by_type"),
                    rs.getInt("books_contributed")
                );
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}