package project.controllers;

import java.io.IOException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import project.Databases.Admin;
import project.Databases.LibraryMember;
import project.Utilities.AlertMsg;
import project.Utilities.SwitchSceneUtil;

public class CreateController {

    @FXML
    private TextField AgeField;

    @FXML
    private TextField EmailField;

    @FXML
    private TextField NameField;

    @FXML
    private TextField NumberField;

    @FXML
    private TextField PasswordField;

    @FXML
    void BTNSignIn(ActionEvent event) throws IOException {
        SwitchSceneUtil.switchScene(event, "LogIn.fxml", "LogIn.css");
    }

    @FXML
    void BTNsignUp(ActionEvent event) throws IOException {
        String Name = NameField.getText().trim();
        String Age = AgeField.getText().trim();
        String Email = EmailField.getText().trim();
        String Number = NumberField.getText().trim();
        String Password = PasswordField.getText().trim();

        if (Name.isEmpty() || Age.isEmpty() || Email.isEmpty() || Number.isEmpty() || Password.isEmpty()) {
            AlertMsg.showError("Fill up", "Please fill in all fields");
            return;
        }

        if (Name.length() < 2) {
            AlertMsg.showWarning("Invalid Name", "Please enter a valid name");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(Age);
            if (age <= 0 || age > 100) {
                AlertMsg.showWarning("Invalid Age", "Please enter a valid age between 1 and 100");
                return;
            }
        } catch (NumberFormatException e) {
            AlertMsg.showError("Invalid Age", "Please enter a valid number for age");
            return;
        }

        if (Password.length() < 4) {
            AlertMsg.showWarning("Weak Password", "Password must be at least 4 characters long");
            return;
        }

        if (!Number.matches("\\d{11}")) {
            AlertMsg.showWarning("Invalid Phone", "Please enter a valid 11-digit phone number");
            return;
        }

        if (!Email.contains("@") || !Email.contains(".")) {
            AlertMsg.showWarning("Invalid Email", "Please enter a valid email address");
            return;
        }

        // Check if user already exists in either table
        if (LibraryMember.memberExists(Email) || Admin.adminExists(Email)) {
            AlertMsg.showError("Email Already Exists", "An account with this email already exists. Please use a different email address.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Registration");
        confirmationAlert.setHeaderText("Are you sure you want to create this account?");
        confirmationAlert.setContentText("Name: " + Name + "\nEmail: " + Email + "\nAge: " + age + "\nPhone Number: " + Number);
        
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Alert accountTypeAlert = new Alert(Alert.AlertType.CONFIRMATION);
            accountTypeAlert.setTitle("Select Account Type");
            accountTypeAlert.setHeaderText("Please select the type of account you want to create:");
            ButtonType memberButton = new ButtonType("Member");
            ButtonType adminButton = new ButtonType("Admin");
            accountTypeAlert.getButtonTypes().setAll(memberButton, adminButton);
        
            Optional<ButtonType> accountTypeResult = accountTypeAlert.showAndWait();
            if (accountTypeResult.isPresent()) {
                if (accountTypeResult.get() == memberButton) {
                    // Add member to database
                    boolean memberAdded = LibraryMember.addMember(Email, Name, Password, age, Number);
                    if (memberAdded) {
                        SwitchSceneUtil.currentUserEmail = Email;
                        AlertMsg.showInformation("Success", "Member account created successfully!\n\nYou can now log in with your credentials.");
                        SwitchSceneUtil.switchScene(event, "LogIn.fxml", "LogIn.css");
                    } else {
                        AlertMsg.showError("Registration Failed", "Failed to create member account. Please try again.");
                    }
                } else if (accountTypeResult.get() == adminButton) {
                    Alert adminPasscodeAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    adminPasscodeAlert.setTitle("Admin Verification");
                    adminPasscodeAlert.setHeaderText("Enter Admin Passcode:");
                    TextField passcodeField = new TextField();
                    passcodeField.setPromptText("Enter admin passcode");
                    adminPasscodeAlert.getDialogPane().setContent(passcodeField);

                    Optional<ButtonType> passcodeResult = adminPasscodeAlert.showAndWait();
                    if (passcodeResult.isPresent() && passcodeResult.get() == ButtonType.OK) {
                        String passcode = passcodeField.getText().trim();
                        if (Admin.verifyPasscode(passcode)) {
                            // Add admin to database
                            boolean adminAdded = Admin.addAdmin(Email, Name, Password, age, Number);
                            if (adminAdded) {
                                SwitchSceneUtil.currentUserEmail = Email;
                                AlertMsg.showInformation("Success", "Admin account created successfully!\n\nWelcome to the admin panel.");
                                SwitchSceneUtil.switchScene(event, "AdminMain.fxml", "AdminMain.css");
                            } else {
                                AlertMsg.showError("Registration Failed", "Failed to create admin account. Please try again.");
                            }
                        } else {
                            AlertMsg.showError("Invalid Passcode", "The admin passcode is incorrect");
                        }
                    }
                }
            }
        }
    }

    @FXML
    void Exit(ActionEvent event) {
        System.exit(0);
    }
}