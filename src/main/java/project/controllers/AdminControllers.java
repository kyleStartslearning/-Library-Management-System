package project.controllers;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import project.UtilityClass;
import project.Databases.Book;
import project.Databases.Admin;
import project.Databases.LibraryMember;
import project.Utilities.AlertMsg; // ADD THIS IMPORT

import java.net.URL;
import java.util.ResourceBundle;

public class AdminControllers implements Initializable {

    @FXML
    private Label borrowedBooksLabel;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label totalMembersLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load statistics when the controller is initialized
        loadStatistics();
    }

    /**
     * Load and display statistics in the labels
     */
    private void loadStatistics() {
        try {
            // Get total books
            int totalBooks = Book.getTotalBookCount();
            totalBooksLabel.setText(String.valueOf(totalBooks));

            // Get total members
            int totalMembers = Admin.getTotalMemberCount();
            totalMembersLabel.setText(String.valueOf(totalMembers));

            // Get currently borrowed books
            int borrowedBooks = Book.getCurrentlyBorrowedCount();
            borrowedBooksLabel.setText(String.valueOf(borrowedBooks));

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
            
            // Set default values if there's an error
            totalBooksLabel.setText("0");
            totalMembersLabel.setText("0");
            borrowedBooksLabel.setText("0");
        }
    }

    /**
     * Refresh statistics (call this after adding/removing books or members)
     */
    public void refreshStatistics() {
        loadStatistics();
    }

    @FXML
    void BTNaddBooks(ActionEvent event) {
        UtilityClass.BookData bookData = UtilityClass.ShowAddBookDialog();

        if(bookData != null) {
            if (Book.bookExists(bookData.title, bookData.author)) {
                // UPDATE THIS:
                AlertMsg.showError("Duplicate Book", 
                "This book already exists in the Library!\n\n" +
                    "📖 Title: " + bookData.title + "\n" +
                    "✍️ Author: " + bookData.author + "\n\n" +
                    "Please check the existing books or add a different book.");
                return;
            }

            String currentEmail = UtilityClass.currentUserEmail != null ?
                                    UtilityClass.currentUserEmail : "admin@library.com";

            boolean success = Book.addBook(
            bookData.title, 
            bookData.author, 
            bookData.copies,
            currentEmail, 
            "admin");

            if (success) {
                // UPDATE THIS:
                AlertMsg.showInformation("Success",
                "Book added successfully to the Library!\n\n" +
                    "📖 Title: " + bookData.title + "\n" +
                    "✍️ Author: " + bookData.author + "\n" +
                    "📚 Copies: " + bookData.copies);
                
                refreshStatistics();
            } else {
                // UPDATE THIS:
                AlertMsg.showError("Error", 
                 "Failed to add book to database.\n\n" +
                    "Possible reasons:\n" +
                    "• Database connection issue\n" +
                    "• Invalid data format\n" +
                    "• System error\n\n" +
                    "Please try again or contact system administrator.");
            }
        }
    }

    @FXML
    void BTNremoveBooks(ActionEvent event) {
        try {
            UtilityClass.ShowRemoveBook();
            refreshStatistics();
        } catch (Exception e) {
            // UPDATE THIS:
            AlertMsg.showError("Error", "Failed to open remove book dialog: " + e.getMessage());
        }
    }

    @FXML
    void BTNviewAllMembers(ActionEvent event) {
        try {
            List<LibraryMember> allMembers = Admin.ViewAllMembers();
            
            if (allMembers.isEmpty()) {
                // UPDATE THIS:
                AlertMsg.showInformation("No Members Found", 
                    "📋 The library currently has no registered members.\n\n" +
                    "To add members, they need to register through the Create Account screen.");
                return;
            }
            
            UtilityClass.ShowAllMembersDialog(allMembers);
            
        } catch (Exception e) {
            // UPDATE THIS:
            AlertMsg.showError("Error", 
                "Failed to retrieve members from database.\n\n" +
                "Error details: " + e.getMessage() + "\n\n" +
                "Please try again or contact system administrator.");
            e.printStackTrace();
        }
    }

    @FXML
    void BTNremoveMembers(ActionEvent event) {
        try {
            UtilityClass.ShowRemoveMember();
            refreshStatistics();
        } catch (Exception e) {
            // UPDATE THIS:
            AlertMsg.showError("Error", "Failed to open remove member dialog: " + e.getMessage());
        }
    }

    @FXML
    void BTNsearchBooks(ActionEvent event) {

    }

    @FXML
    void BTNsearchMembers(ActionEvent event) {

    }

    @FXML
    void BTNviewAllBooks(ActionEvent event) {

    }

    

    @FXML
    void BTNviewBorrowers(ActionEvent event){
      

    }

    @FXML
    void BTNlogOut(ActionEvent event) throws IOException {
          try {
            UtilityClass.switchScene(event, "LogIn.fxml", "LogIn.css");
        } catch (Exception e) {
            // UPDATE THIS:
            AlertMsg.showError("Navigation Error", "Failed to logout: " + e.getMessage());
        }
    }

}
