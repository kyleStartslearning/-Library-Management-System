package project.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    }

    @FXML
    void Exit(ActionEvent event) {
        System.exit(0);

    }

}
