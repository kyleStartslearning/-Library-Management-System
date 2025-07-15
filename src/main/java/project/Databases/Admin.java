package project.Databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Admin {
    private String email;
    private String name;
    private String password;
    private int age;
    private String phoneNumber;

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

    public static Admin authenticateAdmin(String email, String password) throws SQLException {
        Connect dbConnect = Connect.getInstance();
        Connection connection = dbConnect.getConnection();

        String query = "SELECT email, name, password, age, phone_number FROM admins WHERE email = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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




    
}
