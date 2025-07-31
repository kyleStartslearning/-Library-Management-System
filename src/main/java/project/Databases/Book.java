package project.Databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



public class Book {
    private int id;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;
    private int borrowCount;
    private String addedByEmail;
    private String addedByType; // 'admin' or 'member'
    private Timestamp createdAt;
    private String updatedByEmail;
    private String updatedByType;
    private Timestamp updatedAt;

    // Constructors
    public Book() {}

    public Book(String title, String author, int totalCopies, String addedByEmail, String addedByType) {
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.borrowCount = 0;
        this.addedByEmail = addedByEmail;
        this.addedByType = addedByType;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    
    public int getBorrowCount() { return borrowCount; }
    public void setBorrowCount(int borrowCount) { this.borrowCount = borrowCount; }
    
    public String getAddedByEmail() { return addedByEmail; }
    public void setAddedByEmail(String addedByEmail) { this.addedByEmail = addedByEmail; }
    
    public String getAddedByType() { return addedByType; }
    public void setAddedByType(String addedByType) { this.addedByType = addedByType; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /**
     * Add a new book to the database - can be added by admin or member
     */
    public static boolean addBook(String title, String author, int copies, String userEmail, String userType) {
        // First check if book already exists
        if (bookExists(title, author)) {
            System.err.println("Book already exists: " + title + " by " + author);
            return false;
        }
        
        // Validate inputs
        if (title == null || title.trim().isEmpty()) {
            System.err.println("Book title cannot be empty");
            return false;
        }
        if (author == null || author.trim().isEmpty()) {
            System.err.println("Author name cannot be empty");
            return false;
        }
        if (copies <= 0) {
            System.err.println("Number of copies must be greater than 0");
            return false;
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            System.err.println("User email cannot be empty");
            return false;
        }
        if (userType == null || (!userType.equals("admin") && !userType.equals("member"))) {
            System.err.println("User type must be 'admin' or 'member'");
            return false;
        }
        
        String query = "INSERT INTO books (title, author, total_copies, available_copies, added_by_email, added_by_type) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            boolean result = Connect.executeUpdate(query, title, author, copies, copies, userEmail, userType);
            if (result) {
                System.out.println("Book added successfully: " + title + " by " + author);
            } else {
                System.err.println("Failed to add book to database");
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all books as a list of Book objects
     */
    public static List<Book> getAllBooks() {
        String query = "SELECT * FROM books ORDER BY created_at DESC";
        
        return Connect.executeQuery(query, rs -> {
            List<Book> books = new ArrayList<>();
            try {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    book.setBorrowCount(rs.getInt("borrow_count"));
                    book.setAddedByEmail(rs.getString("added_by_email"));
                    book.setAddedByType(rs.getString("added_by_type"));
                    book.setCreatedAt(rs.getTimestamp("created_at"));
                    books.add(book);
                }
            } catch (SQLException e) {
                System.err.println("Error processing book results: " + e.getMessage());
            }
            return books;
        });
    }
    
    /**
     * Check if user exists and determine their type
     */
    public static String getUserType(String email) {
        // Check if admin
        int adminCount = Connect.executeCount("SELECT COUNT(*) FROM admins WHERE email = ?", email);
        if (adminCount > 0) return "admin";
        
        // Check if member
        int memberCount = Connect.executeCount("SELECT COUNT(*) FROM members WHERE email = ?", email);
        if (memberCount > 0) return "member";
        
        return null; // User not found
    }
    
    /**
     * Search books by title or author
     */
    public static List<Book> searchBooks(String searchTerm) {
        String query = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY title";
        String searchPattern = "%" + searchTerm + "%";
        
        return Connect.executeQuery(query, rs -> {
            List<Book> books = new ArrayList<>();
            try {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    books.add(book);
                }
            } catch (SQLException e) {
                System.err.println("Error processing search results: " + e.getMessage());
            }
            return books;
        }, searchPattern, searchPattern);
    }
    
    /**
     * Search books for deletion (more detailed results)
     */
    public static List<Book> searchBooksForDeletion(String title, String author) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM books WHERE ");
        List<String> conditions = new ArrayList<>();
        List<String> parameters = new ArrayList<>();
        
        if (title != null && !title.trim().isEmpty()) {
            conditions.add("title LIKE ?");
            parameters.add("%" + title.trim() + "%");
        }
        
        if (author != null && !author.trim().isEmpty()) {
            conditions.add("author LIKE ?");
            parameters.add("%" + author.trim() + "%");
        }
        
        // Join conditions with OR if both are provided, otherwise use the single condition
        if (conditions.size() == 2) {
            queryBuilder.append("(").append(conditions.get(0)).append(" OR ").append(conditions.get(1)).append(")");
        } else {
            queryBuilder.append(conditions.get(0));
        }
        
        queryBuilder.append(" ORDER BY title, author");
        
        String query = queryBuilder.toString();
        
        return Connect.executeQuery(query, rs -> {
            List<Book> books = new ArrayList<>();
            try {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    book.setBorrowCount(rs.getInt("borrow_count"));
                    book.setAddedByEmail(rs.getString("added_by_email"));
                    book.setAddedByType(rs.getString("added_by_type"));
                    book.setCreatedAt(rs.getTimestamp("created_at"));
                    books.add(book);
                }
            } catch (SQLException e) {
                System.err.println("Error processing search results for deletion: " + e.getMessage());
            }
            return books;
        }, parameters.toArray());
    }
    
    /**
     * Get book by ID
     */
    public static Book getBookById(int id) {
        String query = "SELECT * FROM books WHERE id = ?";
        
        return Connect.executeQuery(query, rs -> {
            try {
                if (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    book.setBorrowCount(rs.getInt("borrow_count"));
                    book.setAddedByEmail(rs.getString("added_by_email"));
                    book.setAddedByType(rs.getString("added_by_type"));
                    return book;
                }
            } catch (SQLException e) {
                System.err.println("Error getting book by ID: " + e.getMessage());
            }
            return null;
        }, id);
    }
    
    /**
     * Update book availability when borrowed/returned
     */
    public static boolean updateAvailableCopies(int bookId, int newAvailableCount) {
        String query = "UPDATE books SET available_copies = ? WHERE id = ?";
        return Connect.executeUpdate(query, newAvailableCount, bookId);
    }
    
    /**
     * Delete a book by ID
     */
    public static boolean deleteBookById(int bookId) {
        String query = "DELETE FROM books WHERE id = ?";
        try {
            boolean result = Connect.executeUpdate(query, bookId);
            if (result) {
                System.out.println("Book deleted successfully with ID: " + bookId);
            } else {
                System.err.println("Failed to delete book with ID: " + bookId);
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a book exists by title and author
     */
    public static boolean bookExists(String title, String author) {
        int count = Connect.executeCount("SELECT COUNT(*) FROM books WHERE title = ? AND author = ?", title, author);
        return count > 0;
    }
    
    /**
     * Get total number of books
     */
    public static int getTotalBookCount() {
        return Connect.executeCount("SELECT COUNT(*) FROM books");
    }
    
    /**
     * Get total number of available copies
     */
    public static int getTotalAvailableCopies() {
        return Connect.executeCount("SELECT SUM(available_copies) FROM books");
    }
}


