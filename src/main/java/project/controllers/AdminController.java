package project.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import project.UtilityClass;
import project.UtilityClass.BookData;
import project.Databases.Book;

public class AdminController {

    @FXML
    private Label borrowedBooksLabel;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label totalMembersLabel;

    @FXML
    void BTNaddBooks(ActionEvent event) {
        // Show the add book dialog
        BookData bookData = UtilityClass.ShowAddBookDialog();
        
        if (bookData != null) {
            // Get current user email, default to admin if not set
            String currentEmail = UtilityClass.currentUserEmail != null ? 
                                 UtilityClass.currentUserEmail : "admin@library.com";
            
            // Add book to database
            boolean success = Book.addBook(
                bookData.title, 
                bookData.author, 
                bookData.copies, 
                currentEmail,
                "admin"  // Admin type
            );
            
            if (success) {
                UtilityClass.ShowInformation("Success", 
                    "Book added successfully!\n\n" +
                    "📖 Title: " + bookData.title + "\n" +
                    "✍️ Author: " + bookData.author + "\n" +
                    "📚 Copies: " + bookData.copies + "\n" +
                    "👤 Added by: " + currentEmail);
                
                // TODO: Refresh statistics on the dashboard
                // updateStatistics();
            } else {
                UtilityClass.ShowError("Error", "Failed to add book to database. Please try again.");
            }
        }
        // If bookData is null, user cancelled the dialog - do nothing
    }

    @FXML
    void BTNaddMembers(ActionEvent event) {
        // TODO: Implement add members functionality
    }

    @FXML
    void BTNlogOut(ActionEvent event) {
        try {
            UtilityClass.switchScene(event, "LogIn.fxml", "LogIn.css");
        } catch (Exception e) {
            UtilityClass.ShowError("Navigation Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    void BTNremoveBooks(ActionEvent event) {
        // TODO: Implement remove books functionality
    }

    @FXML
    void BTNremoveMembers(ActionEvent event) {
        // TODO: Implement remove members functionality
    }

    @FXML
    void BTNsearchBooks(ActionEvent event) {
        // TODO: Implement search books functionality
    }

    @FXML
    void BTNsearchMembers(ActionEvent event) {
        // TODO: Implement search members functionality
    }

    @FXML
    void BTNviewAllBooks(ActionEvent event) {
        // TODO: Implement view all books functionality
    }

    @FXML
    void BTNviewBorrowers(ActionEvent event) {
        // TODO: Implement view borrowers functionality
    }
}
