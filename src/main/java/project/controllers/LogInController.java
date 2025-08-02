package project.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import project.Databases.Admin;
import project.Databases.LibraryMember;
import project.Utilities.AlertMsg;
import project.Utilities.SwitchSceneUtil;

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
    void BTNlogin(ActionEvent event) {
        String email = UserField.getText().trim();
        String password = PasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            AlertMsg.showError("Error", "Please Fill in all fields");
            return;
        }

        try {
            // First try to authenticate as Admin
            Admin admin = Admin.authenticateAdmin(email, password);
            if (admin != null) {
                SwitchSceneUtil.currentUserEmail = email;
                AlertMsg.showInformation("Success", "Welcome back Admin, " + admin.getName() + "!");
                SwitchSceneUtil.switchScene(event, "AdminMain.fxml", "AdminMain.css");
                return;
            }

            // If admin authentication fails, try member authentication
            LibraryMember member = LibraryMember.AuthenticateMember(email, password);
            if (member != null) {
                SwitchSceneUtil.currentUserEmail = email;
                AlertMsg.showInformation("Success", "Welcome back, " + member.getName() + "!");
                SwitchSceneUtil.switchScene(event, "StudentMain.fxml", "StudentMain.css");
                return;
            }

            // If both authentications fail
            AlertMsg.showError("Login Failed", "Invalid email or password.\n\nPlease check your credentials and try again.");

        } catch (Exception e) {
            AlertMsg.showError("Error", "Login error: " + e.getMessage());
        }
    }

    @FXML
    void BTNCreate(ActionEvent event) {
        try {
            SwitchSceneUtil.switchScene(event, "CreateScene.fxml", "CreateAcc.css");
        } catch (Exception e) {
            AlertMsg.showError("Navigation Error", "Failed to load create account screen: " + e.getMessage());
        }
    }

    @FXML
    void Exit(ActionEvent event) {
        System.exit(0);
    }
}
