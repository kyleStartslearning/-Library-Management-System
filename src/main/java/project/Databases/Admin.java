package project.Databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Admin {
    private String email;
    private String name;
    private String password;
    private int age;
    private String phoneNumber;
    private Timestamp createdAt; // Add this field

    public Admin(String email, String name, String  password, int age, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.age = age;
        this.phoneNumber = phoneNumber;
    }

    public Admin () {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public static Admin authenticateAdmin(String email, String password) throws SQLException {
        String query = "SELECT email, name, password, age, phone_number FROM admins WHERE email = ? AND password = ?";

        try (Connection connection = Connect.getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return new Admin(
                    rs.getString("email"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getInt("age"),
                    rs.getString("phone_number")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating admin " + e.getMessage());
        }
        
        return null;
        
    }   

    /**
     * View all members in the system
     * @return List of LibraryMember objects
     */
    public static List<LibraryMember> ViewAllMembers() {
        String query = "SELECT email, name, password, age, phone_number, created_at FROM members ORDER BY created_at DESC";
        
        return Connect.executeQuery(query, rs -> {
            List<LibraryMember> members = new ArrayList<>();
            try {
                while (rs.next()) {
                    LibraryMember member = new LibraryMember();
                    member.setEmail(rs.getString("email"));
                    member.setName(rs.getString("name"));
                    member.setPassword(rs.getString("password"));
                    member.setAge(rs.getInt("age"));
                    member.setPhoneNumber(rs.getString("phone_number"));
                    member.setCreatedAt(rs.getTimestamp("created_at"));
                    members.add(member);
                }
            } catch (SQLException e) {
                System.err.println("Error processing members: " + e.getMessage());
            }
            return members;
        });
    }

    /**
     * View all admins in the system
     * @return List of Admin objects
     */
    public static List<Admin> ViewAllAdmins() {
        String query = "SELECT email, name, password, age, phone_number, created_at FROM admins ORDER BY created_at DESC";
        
        return Connect.executeQuery(query, rs -> {
            List<Admin> admins = new ArrayList<>();
            try {
                while (rs.next()) {
                    Admin admin = new Admin();
                    admin.setEmail(rs.getString("email"));
                    admin.setName(rs.getString("name"));
                    admin.setPassword(rs.getString("password"));
                    admin.setAge(rs.getInt("age"));
                    admin.setPhoneNumber(rs.getString("phone_number"));
                    admin.setCreatedAt(rs.getTimestamp("created_at"));
                    admins.add(admin);
                }
            } catch (SQLException e) {
                System.err.println("Error processing admins: " + e.getMessage());
            }
            return admins;
        });
    }

    /**
     * Search members for deletion (more detailed results)
     */
    public static List<LibraryMember> searchMembersForDeletion(String name, String email) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM members WHERE ");
        List<String> conditions = new ArrayList<>();
        List<String> parameters = new ArrayList<>();
        
        if (name != null && !name.trim().isEmpty()) {
            conditions.add("name LIKE ?");
            parameters.add("%" + name.trim() + "%");
        }
        
        if (email != null && !email.trim().isEmpty()) {
            conditions.add("email LIKE ?");
            parameters.add("%" + email.trim() + "%");
        }
        
        // Join conditions with OR if both are provided, otherwise use the single condition
        if (conditions.size() == 2) {
            queryBuilder.append("(").append(conditions.get(0)).append(" OR ").append(conditions.get(1)).append(")");
        } else {
            queryBuilder.append(conditions.get(0));
        }
        
        queryBuilder.append(" ORDER BY name, email");
        
        String query = queryBuilder.toString();
        
        return Connect.executeQuery(query, rs -> {
            List<LibraryMember> members = new ArrayList<>();
            try {
                while (rs.next()) {
                    LibraryMember member = new LibraryMember();
                    member.setEmail(rs.getString("email"));
                    member.setName(rs.getString("name"));
                    member.setPassword(rs.getString("password"));
                    member.setAge(rs.getInt("age"));
                    member.setPhoneNumber(rs.getString("phone_number"));
                    member.setCreatedAt(rs.getTimestamp("created_at"));
                    members.add(member);
                }
            } catch (SQLException e) {
                System.err.println("Error processing search results for member deletion: " + e.getMessage());
            }
            return members;
        }, parameters.toArray());
    }

    /**
     * Delete a member by email
     */
    public static boolean deleteMemberByEmail(String email) {
        String query = "DELETE FROM members WHERE email = ?";
        try {
            boolean result = Connect.executeUpdate(query, email);
            if (result) {
                System.out.println("Member deleted successfully with email: " + email);
            } else {
                System.err.println("Failed to delete member with email: " + email);
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error deleting member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total number of members
     */
    public static int getTotalMemberCount() {
        return Connect.executeCount("SELECT COUNT(*) FROM members");
    }
}
