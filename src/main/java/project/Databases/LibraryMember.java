package project.Databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class LibraryMember {
    private String email;
    private String name;
    private String password;
    private int age;
    private String phone_number;
    private Timestamp createdAt;

    public LibraryMember(String email, String name, String password, int age, String phone_number, Timestamp createAt){
        this.email = email;
        this.name = name;
        this.password = password;
        this.age = age;
        this.phone_number = phone_number;
        this.createdAt = createAt;
    }

    public LibraryMember() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getPhoneNumber() { return phone_number; }
    public void setPhoneNumber(String phone_number) { this.phone_number = phone_number; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    

    public static LibraryMember AuthenticateMember(String email, String password) {
        
        String query = "SELECT email, name, password, age, phone_number FROM members WHERE email = ? AND password =?";

        try (Connection connection = Connect.getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
                
                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                           return new LibraryMember(
                            rs.getString("email"),
                            rs.getString("name"),
                            rs.getString("password"),
                            rs.getInt("age"),
                            rs.getString("phone_number"),
                            null  // createdAt - since you're not selecting it from the query
                );
            }
            
        } catch (Exception e) {
             System.err.println("Error authenticating admin " + e.getMessage());
        }

        return null;
    }

    /**
     * Add a new member to the database
     */
    public static boolean addMember(String email, String name, String password, int age, String phoneNumber) {
        // Check if member already exists
        if (memberExists(email)) {
            System.err.println("Member with email " + email + " already exists");
            return false;
        }

        String query = "INSERT INTO members (email, name, password, age, phone_number) VALUES (?, ?, ?, ?, ?)";
        
        try {
            boolean result = Connect.executeUpdate(query, email, name, password, age, phoneNumber);
            if (result) {
                System.out.println("Member added successfully: " + name + " (" + email + ")");
            } else {
                System.err.println("Failed to add member to database");
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error adding member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a member with the given email already exists
     */
    public static boolean memberExists(String email) {
        String query = "SELECT COUNT(*) FROM members WHERE email = ?";
        try {
            int count = Connect.executeCount(query, email);
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error checking if member exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get member by email address
     */
    public static LibraryMember getMemberByEmail(String email) {
        String query = "SELECT email, name, password, age, phone_number, created_at FROM members WHERE email = ?";
        
        try (Connection connection = Connect.getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
                
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new LibraryMember(
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("age"),
                        rs.getString("phone_number"),
                        rs.getTimestamp("created_at")
                    );
                }
            
        } catch (Exception e) {
            System.err.println("Error getting member by email: " + e.getMessage());
        }

        return null;
    }

    /**
     * Load and get user information for display
     * @param currentEmail The email of the current user
     * @return Welcome message string
     */
    public static String loadUserInfo(String currentEmail) {
        if (currentEmail != null) {
            try {
                LibraryMember member = LibraryMember.getMemberByEmail(currentEmail);
                if (member != null) {
                    return "Welcome, " + member.getName();
                } else {
                    return "Welcome Student";
                }
            } catch (Exception e) {
                System.err.println("Error loading user info: " + e.getMessage());
                return "Welcome Student";
            }
        } else {
            return "Welcome Student";
        }
    }

}
