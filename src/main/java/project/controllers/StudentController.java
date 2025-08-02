package project.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import project.Utilities.SwitchSceneUtil;
import project.Utilities.memberUtil.BorrowReturn;
import project.Databases.LibraryMember;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class StudentController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label userLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserInfo();
    }

    /**
     * Load and display current user information
     */
    private void loadUserInfo() {
        String currentEmail = SwitchSceneUtil.currentUserEmail;
        String welcomeMessage = LibraryMember.loadUserInfo(currentEmail);
        userLabel.setText(welcomeMessage);
    }

    @FXML
    void BTNborrowReturn(ActionEvent event) {
        BorrowReturn.showBorrowReturnMainDialog();
    }

    @FXML
    void BTNsearch(ActionEvent event) {
        BorrowReturn.showSearchBooksDialog();
    }

    @FXML
    void BTNlogOut(ActionEvent event) throws IOException {
        // Clear current user session
        SwitchSceneUtil.currentUserEmail = null;
        SwitchSceneUtil.switchScene(event, "LogIn.fxml", "LogIn.css");
    }
}
