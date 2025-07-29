package project.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import project.UtilityClass;
import project.Databases.Admin;

public class LogInController {

    @FXML
    private PasswordField PasswordField;

    @FXML
    private TextField UserField;

    @FXML
    private Button closeButton;

    @FXML
    private Button createAccButton;

    @FXML
    private Button loginButton;

    @FXML
    void BTNCreate(ActionEvent event) {


    }

    @FXML
    void BTNlogin(ActionEvent event) {
        String email = UserField.getText().trim();
        String password = PasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            UtilityClass.ShowError("Error", "Please Fill in all fields");
            return;
        }

        try {
            Admin admin = Admin.authenticateAdmin(email, password);
            if (admin != null) {
                
                UtilityClass.currentUserEmail = email;
                UtilityClass.ShowInformation("Success", "Welcome back, " + admin.getName() + "!");
                
                
                UtilityClass.switchScene(event, "AdminMain.fxml", "AdminMain.css");
            } else {
                // Authentication failed
                UtilityClass.ShowError("Login Failed", "Invalid email or password");
            }
        } catch (Exception e) {
            UtilityClass.ShowError("Error", "Login error: " + e.getMessage());
        }
    }

    @FXML
    void Exit(ActionEvent event) {
        System.exit(0);

    }

}
