package project.controllers;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import project.UtilityClass;
import project.Databases.Book;
import project.Databases.Admin;
import project.Databases.LibraryMember;

public class AdminControllers {

    @FXML
    private Label borrowedBooksLabel;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label totalMembersLabel;

    @FXML
    void BTNaddBooks(ActionEvent event) {
        UtilityClass.BookData bookData = UtilityClass.ShowAddBookDialog();

        if(bookData != null) {
            if (Book.bookExists(bookData.title, bookData.author)) {
                UtilityClass.ShowError("Duplicate Book", 
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
                UtilityClass.ShowInformation("Success",
                "Book added successfully to the Library!\n\n" +
                    "📖 Title: " + bookData.title + "\n" +
                    "✍️ Author: " + bookData.author + "\n" +
                    "📚 Copies: " + bookData.copies);
            } else {
                UtilityClass.ShowError("Error", 
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
        } catch (Exception e) {
            UtilityClass.ShowError("Error", "Failed to open remove book dialog: " + e.getMessage());
        }
    }



    @FXML
    void BTNviewAllMembers(ActionEvent event) {
        try {
            List<LibraryMember> allMembers = Admin.ViewAllmembers();
            
            if (allMembers.isEmpty()) {
                UtilityClass.ShowInformation("No Members Found", 
                    "📋 The library currently has no registered members.\n\n" +
                    "To add members, they need to register through the Create Account screen.");
                return;
            }
            
            // Show members in a dialog
            UtilityClass.ShowAllMembersDialog(allMembers);
            
        } catch (Exception e) {
            UtilityClass.ShowError("Error", 
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
        } catch (Exception e) {
            UtilityClass.ShowError("Error", "Failed to open remove member dialog: " + e.getMessage());
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
            UtilityClass.ShowError("Navigation Error", "Failed to logout: " + e.getMessage());
        }
    }

}
