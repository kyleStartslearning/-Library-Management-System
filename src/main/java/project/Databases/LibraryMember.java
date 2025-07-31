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

    








}
