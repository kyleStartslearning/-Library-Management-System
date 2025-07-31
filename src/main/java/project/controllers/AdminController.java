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
        // Show the add book dialog (which now includes confirmation)
        UtilityClass.BookData bookData = UtilityClass.ShowAddBookDialog();
        
        if (bookData != null) {
            // Check if book already exists before attempting to add
            if (Book.bookExists(bookData.title, bookData.author)) {
                UtilityClass.ShowError("Duplicate Book", 
                    "This book already exists in the database!\n\n" +
                    "📖 Title: " + bookData.title + "\n" +
                    "✍️ Author: " + bookData.author + "\n\n" +
                    "Please check the existing books or add a different book.");
                return;
            }
            
            // Get current user email, default to admin if not set
            String currentEmail = UtilityClass.currentUserEmail != null ? 
                                 UtilityClass.currentUserEmail : "admin@library.com";
            
            // Add book to database (confirmation already handled in dialog)
            boolean success = Book.addBook(
                bookData.title, 
                bookData.author, 
                bookData.copies, 
                currentEmail,
                "admin"
            );
            
            if (success) {
                UtilityClass.ShowInformation("Success", 
                    "Book added successfully to the database!\n\n" +
                    "📖 Title: " + bookData.title + "\n" +
                    "✍️ Author: " + bookData.author + "\n" +
                    "📚 Copies: " + bookData.copies);
            } else {
                UtilityClass.ShowError("Database Error", 
                    "Failed to add book to database.\n\n" +
                    "Possible reasons:\n" +
                    "• Database connection issue\n" +
                    "• Invalid data format\n" +
                    "• System error\n\n" +
                    "Please try again or contact system administrator.");
            }
        }
        // If bookData is null, user cancelled at some point
    }

    @FXML
    void BTNremoveBooks(ActionEvent event) {
        
    }

    @FXML
    void BTNaddMembers(ActionEvent event) {
        
        
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
