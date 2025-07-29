package project.Databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        String query = "INSERT INTO books (title, author, total_copies, available_copies, added_by_email, added_by_type) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, copies);
            stmt.setInt(4, copies);
            stmt.setString(5, userEmail);
            stmt.setString(6, userType);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all books with user information
     */
    public static ResultSet getBooksWithUserInfo() {
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        String query = "SELECT * FROM books_with_user_info ORDER BY created_at DESC";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error retrieving books with user info: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if user exists and determine their type
     */
    public static String getUserType(String email) {
        Connect dbConnection = Connect.getInstance();
        Connection connection = dbConnection.getConnection();
        
        // Check if admin
        String adminQuery = "SELECT COUNT(*) FROM admins WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(adminQuery)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "admin";
            }
        } catch (SQLException e) {
            System.err.println("Error checking admin: " + e.getMessage());
        }
        
        // Check if member
        String memberQuery = "SELECT COUNT(*) FROM members WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(memberQuery)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "member";
            }
        } catch (SQLException e) {
            System.err.println("Error checking member: " + e.getMessage());
        }
        
        return null; // User not found
    }
}