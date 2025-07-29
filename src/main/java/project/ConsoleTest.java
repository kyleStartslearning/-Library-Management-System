package project;

import project.Databases.Connect;
import project.Databases.Admin;
import project.Databases.Book;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.List;

/**
 * Console-based testing application for database operations
 */
public class ConsoleTest {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        Library Database Test Tool                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        
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
                case 6 -> testNewExecuteMethods();  // NEW: Test the executeQuery methods
                case 7 -> testSearchBooks();        // NEW: Test search functionality
                case 8 -> testBookOperations();     // NEW: Test book CRUD operations
                case 9 -> {
                    System.out.println("\n👋 Goodbye! Thanks for testing!");
                    dbConnection.closeConnection();
                    return;
                }
                default -> System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }
    
    private static void showMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("📚 Choose an option:");
        System.out.println("1. 🔐 Test Authentication");
        System.out.println("2. ➕ Add a Book");
        System.out.println("3. 📖 View All Books");
        System.out.println("4. 📊 View Statistics");
        System.out.println("5. 🔍 View Audit Trail");
        System.out.println("6. ⚡ Test New Execute Methods");  // NEW
        System.out.println("7. 🔎 Test Search Books");         // NEW
        System.out.println("8. 🛠️  Test Book Operations");      // NEW
        System.out.println("9. 🚪 Exit");
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
        System.out.println("\n🔐 --- Authentication Test ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        try {
            Admin admin = Admin.authenticateAdmin(email, password);
            if (admin != null) {
                System.out.println("✅ Authentication successful!");
                System.out.println("👋 Welcome, " + admin.getName() + "!");
                
                // Test getUserType method
                String userType = Book.getUserType(email);
                System.out.println("🏷️  User type detected: " + userType);
            } else {
                System.out.println("❌ Authentication failed!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void testAddBook() {
        System.out.println("\n➕ --- Add Book Test ---");
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        System.out.print("Enter number of copies: ");
        
        try {
            int copies = Integer.parseInt(scanner.nextLine());
            
            // Show before count
            int beforeCount = Book.getTotalBookCount();
            System.out.println("📊 Books before adding: " + beforeCount);
            
            // Add book
            boolean success = Book.addBook(title, author, copies, "system@library.com", "admin");
            
            if (success) {
                // Show after count to verify
                int afterCount = Book.getTotalBookCount();
                System.out.println("✅ Book added successfully!");
                System.out.println("📊 Books after adding: " + afterCount);
                System.out.println("📈 Books increased by: " + (afterCount - beforeCount));
                
                // Check if book exists
                boolean exists = Book.bookExists(title, author);
                System.out.println("🔍 Book exists in database: " + (exists ? "✅ YES" : "❌ NO"));
            } else {
                System.out.println("❌ Failed to add book!");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format!");
        }
    }
    
    // NEW: Test the executeQuery methods
    private static void testNewExecuteMethods() {
        System.out.println("\n⚡ --- Testing New Execute Methods ---");
        
        System.out.println("\n📊 Using Book.getTotalBookCount():");
        int totalBooks = Book.getTotalBookCount();
        System.out.println("Total books: " + totalBooks);
        
        System.out.println("\n📚 Using Book.getTotalAvailableCopies():");
        int availableCopies = Book.getTotalAvailableCopies();
        System.out.println("Total available copies: " + availableCopies);
        
        System.out.println("\n📖 Using Book.getAllBooks():");
        List<Book> allBooks = Book.getAllBooks();
        if (allBooks != null && !allBooks.isEmpty()) {
            System.out.println("✅ Retrieved " + allBooks.size() + " books successfully!");
            System.out.println("📖 First few books:");
            for (int i = 0; i < Math.min(3, allBooks.size()); i++) {
                Book book = allBooks.get(i);
                System.out.printf("   %d. %s by %s (%d copies)%n", 
                    i + 1, book.getTitle(), book.getAuthor(), book.getTotalCopies());
            }
        } else {
            System.out.println("❌ No books found or error occurred");
        }
        
        System.out.println("\n🔍 Testing Book.getUserType():");
        System.out.print("Enter email to check: ");
        String email = scanner.nextLine();
        String userType = Book.getUserType(email);
        System.out.println("User type: " + (userType != null ? userType : "❌ Not found"));
    }
    
    // NEW: Test search functionality
    private static void testSearchBooks() {
        System.out.println("\n🔎 --- Search Books Test ---");
        System.out.print("Enter search term (title or author): ");
        String searchTerm = scanner.nextLine();
        
        if (searchTerm.trim().isEmpty()) {
            System.out.println("❌ Search term cannot be empty!");
            return;
        }
        
        List<Book> searchResults = Book.searchBooks(searchTerm);
        
        if (searchResults != null && !searchResults.isEmpty()) {
            System.out.println("✅ Found " + searchResults.size() + " book(s):");
            System.out.println("─".repeat(80));
            System.out.printf("%-30s %-20s %-8s %-8s%n", "Title", "Author", "Total", "Available");
            System.out.println("─".repeat(80));
            
            for (Book book : searchResults) {
                System.out.printf("%-30s %-20s %-8d %-8d%n",
                    truncateString(book.getTitle(), 29),
                    truncateString(book.getAuthor(), 19),
                    book.getTotalCopies(),
                    book.getAvailableCopies()
                );
            }
        } else {
            System.out.println("❌ No books found matching: '" + searchTerm + "'");
        }
    }
    
    // NEW: Test book CRUD operations
    private static void testBookOperations() {
        System.out.println("\n🛠️ --- Book Operations Test ---");
        System.out.println("1. Get book by ID");
        System.out.println("2. Update available copies");
        System.out.println("3. Check if book exists");
        System.out.print("Choose operation: ");
        
        int choice = getChoice();
        
        switch (choice) {
            case 1 -> testGetBookById();
            case 2 -> testUpdateAvailableCopies();
            case 3 -> testBookExists();
            default -> System.out.println("❌ Invalid choice");
        }
    }
    
    private static void testGetBookById() {
        System.out.print("Enter book ID: ");
        try {
            int bookId = Integer.parseInt(scanner.nextLine());
            Book book = Book.getBookById(bookId);
            
            if (book != null) {
                System.out.println("✅ Book found:");
                System.out.println("📖 Title: " + book.getTitle());
                System.out.println("✍️  Author: " + book.getAuthor());
                System.out.println("📚 Total Copies: " + book.getTotalCopies());
                System.out.println("✅ Available: " + book.getAvailableCopies());
                System.out.println("👤 Added by: " + book.getAddedByEmail() + " (" + book.getAddedByType() + ")");
            } else {
                System.out.println("❌ Book not found with ID: " + bookId);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format!");
        }
    }
    
    private static void testUpdateAvailableCopies() {
        System.out.print("Enter book ID: ");
        try {
            int bookId = Integer.parseInt(scanner.nextLine());
            
            // Get current book info
            Book book = Book.getBookById(bookId);
            if (book == null) {
                System.out.println("❌ Book not found with ID: " + bookId);
                return;
            }
            
            System.out.println("Current available copies: " + book.getAvailableCopies());
            System.out.print("Enter new available copies: ");
            int newCopies = Integer.parseInt(scanner.nextLine());
            
            boolean success = Book.updateAvailableCopies(bookId, newCopies);
            
            if (success) {
                System.out.println("✅ Available copies updated successfully!");
                
                // Verify the change
                Book updatedBook = Book.getBookById(bookId);
                if (updatedBook != null) {
                    System.out.println("📊 Verification - New available copies: " + updatedBook.getAvailableCopies());
                }
            } else {
                System.out.println("❌ Failed to update available copies!");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format!");
        }
    }
    
    private static void testBookExists() {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        
        boolean exists = Book.bookExists(title, author);
        System.out.println("🔍 Book exists: " + (exists ? "✅ YES" : "❌ NO"));
        
        if (exists) {
            System.out.println("📖 This book is already in the database!");
        } else {
            System.out.println("➕ This book can be added to the database.");
        }
    }
    
    private static void viewAllBooks() {
        System.out.println("\n📖 --- All Books ---");
        
        // Use the new getAllBooks method
        List<Book> allBooks = Book.getAllBooks();
        
        if (allBooks != null && !allBooks.isEmpty()) {
            System.out.printf("%-30s %-20s %-8s %-8s %-15s%n", "Title", "Author", "Total", "Avail", "Added By");
            System.out.println("─".repeat(85));
            
            for (Book book : allBooks) {
                System.out.printf("%-30s %-20s %-8d %-8d %-15s%n",
                    truncateString(book.getTitle(), 29),
                    truncateString(book.getAuthor(), 19),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    truncateString(book.getAddedByEmail(), 14)
                );
            }
            System.out.println("─".repeat(85));
            System.out.println("📊 Total: " + allBooks.size() + " books");
        } else {
            System.out.println("❌ No books found or error occurred");
        }
    }
    
    private static void viewStatistics() {
        System.out.println("\n📊 --- Library Statistics ---");
        
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM library_statistics")) {
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("👥 Total Members: " + rs.getInt("total_members"));
                System.out.println("📚 Total Books: " + rs.getInt("total_books"));
                System.out.println("📖 Total Copies: " + rs.getInt("total_book_copies"));
                System.out.println("✅ Available Copies: " + rs.getInt("available_copies"));
                System.out.println("📤 Currently Borrowed: " + rs.getInt("currently_borrowed"));
                System.out.println("👨‍💼 Books by Admins: " + rs.getInt("books_added_by_admins"));
                System.out.println("👤 Books by Members: " + rs.getInt("books_added_by_members"));
                
                // Compare with our utility methods
                System.out.println("\n🔍 Verification using Book utility methods:");
                System.out.println("📚 Book.getTotalBookCount(): " + Book.getTotalBookCount());
                System.out.println("📖 Book.getTotalAvailableCopies(): " + Book.getTotalAvailableCopies());
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void testAuditTrail() {
        System.out.println("\n🔍 --- Audit Trail ---");
        
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
    
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "N/A";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}