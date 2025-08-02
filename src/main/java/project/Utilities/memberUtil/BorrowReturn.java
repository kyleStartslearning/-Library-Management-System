package project.Utilities.memberUtil;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import project.Databases.Book;
import project.Databases.LibraryMember;
import project.Databases.Connect;
import project.Utilities.AlertMsg;
import project.Utilities.SwitchSceneUtil;
import project.Utilities.UIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class BorrowReturn {
    
    /**
     * Data class to hold borrowed book information
     */
    public static class BorrowedBookInfo {
        public final int borrowId;
        public final String bookTitle;
        public final String bookAuthor;
        public final int bookId;
        public final String borrowDate;
        public final String returnDate;
        public final boolean isReturned;
        
        public BorrowedBookInfo(int borrowId, String bookTitle, String bookAuthor, int bookId,
                               String borrowDate, String returnDate, boolean isReturned) {
            this.borrowId = borrowId;
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.bookId = bookId;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate;
            this.isReturned = isReturned;
        }
    }

    /**
     * Main dialog for borrow/return operations
     */
    public static void showBorrowReturnMainDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "📚 Library Borrow/Return System"); // USING UIUtil

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

        ButtonType searchBorrowBtn = new ButtonType("Search & Borrow Books");
        ButtonType returnBooksBtn = new ButtonType("Return Books");
        ButtonType myBooksBtn = new ButtonType("My Borrowed Books");

        dialog.getDialogPane().getButtonTypes().addAll(
            searchBorrowBtn,
            returnBooksBtn,
            myBooksBtn,
            ButtonType.CANCEL
        );

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        String currentEmail = SwitchSceneUtil.currentUserEmail;
        LibraryMember currentMember = LibraryMember.getMemberByEmail(currentEmail);
        String memberName = currentMember != null ? currentMember.getName() : "Student";

        Label welcomeLabel = new Label("Welcome, " + memberName + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + ";");

        Label instructionLabel = new Label(
            "Choose an option:\n\n" +
            "• Search & Borrow Books - Find and borrow available books\n" +
            "• Return Books - Return your currently borrowed books\n" +
            "• My Borrowed Books - View your borrowing history"
        );
        instructionLabel.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(welcomeLabel, instructionLabel);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> buttonType);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            ButtonType buttonPressed = result.get();
            if (buttonPressed == searchBorrowBtn) {
                showSearchBooksDialog();
            } else if (buttonPressed == returnBooksBtn) {
                showReturnBooksDialog();
            } else if (buttonPressed == myBooksBtn) {
                showMyBorrowedBooksDialog();
            }
        }
    }

    /**
     * Dialog for searching available books to borrow
     */
    public static void showSearchBooksDialog() {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "🔍 Search Available Books"); // USING UIUtil

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = UIUtil.createDialogGrid(); // USING UIUtil
        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title or author name");
        searchField.setPrefWidth(300);

        Label memberLabel = UIUtil.createUserLabel("Searching", "member"); // USING UIUtil

        grid.add(memberLabel, 0, 0, 2, 1);
        grid.add(new Label("Search Term:"), 0, 1);
        grid.add(searchField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        searchField.requestFocus();

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String searchTerm = searchField.getText().trim();
            
            if (searchTerm.isEmpty()) {
                AlertMsg.showError("Search Error", "Please enter a search term!");
                event.consume();
                return;
            }

            List<Book> foundBooks = searchAvailableBooks(searchTerm);
            
            if (foundBooks.isEmpty()) {
                AlertMsg.showInformation("No Results", 
                    "No available books found matching: '" + searchTerm + "'\n\n" +
                    "Try searching with different keywords or author names.");
                event.consume();
                return;
            }

            showBorrowableBookResults(foundBooks, searchTerm);
        });

        searchField.setOnAction(e -> okButton.fire());
        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Search for available books (books with available copies > 0)
     */
    private static List<Book> searchAvailableBooks(String searchTerm) {
        String query = "SELECT * FROM books WHERE (title LIKE ? OR author LIKE ?) AND available_copies > 0 ORDER BY title";
        
        return Connect.executeQuery(query, rs -> {
            List<Book> books = new ArrayList<>();
            try {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    books.add(book);
                }
            } catch (SQLException e) {
                System.err.println("Error processing search results: " + e.getMessage());
            }
            return books;
        }, "%" + searchTerm + "%", "%" + searchTerm + "%");
    }

    /**
     * Show borrowable book search results
     */
    public static void showBorrowableBookResults(List<Book> books, String searchTerm) {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "📚 Available Books - Search Results for: '" + searchTerm + "'"); // USING UIUtil

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label headerLabel = UIUtil.createTableHeader("ID    | TITLE                           | AUTHOR                     | AVAILABLE | ACTION", UIUtil.PRIMARY_COLOR);
        vbox.getChildren().add(headerLabel);
        
        Label separatorLabel = UIUtil.createTableSeparator("──────┼─────────────────────────────────┼────────────────────────────┼───────────┼────────");
        vbox.getChildren().add(separatorLabel);

        for (Book book : books) {
            String bookInfo = String.format("%-5d | %-31s | %-26s | %-9d | Click to Borrow",
                book.getId(),
                UIUtil.truncateString(book.getTitle(), 31),
                UIUtil.truncateString(book.getAuthor(), 26),
                book.getAvailableCopies());
            
            Label bookLabel = new Label(bookInfo);
            bookLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-cursor: hand;");
            
            // Add click event to show confirmation dialog and borrow the book
            bookLabel.setOnMouseClicked(e -> {
                if (showBorrowConfirmationDialog(book)) {
                    boolean borrowed = borrowBook(book);
                    if (borrowed) {
                        dialog.close();
                        AlertMsg.showInformation("Success", 
                            "Book borrowed successfully!\n\n" +
                            "📖 Title: " + book.getTitle() + "\n" +
                            "✍️ Author: " + book.getAuthor() + "\n" +
                            "🆔 Book ID: " + book.getId() + "\n\n" +
                            "Please remember to return it on time!");
                    }
                }
            });
            
            UIUtil.applyHoverEffect(bookLabel, 
                UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-cursor: hand;", 
                "#e6f3ff");
            
            vbox.getChildren().add(bookLabel);
        }

        Label summaryLabel = new Label(String.format(
            "\n📊 Search Summary: Found %d available books\n" +
            "💡 Tip: Click on any book to borrow it!",
            books.size()));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + "; -fx-font-size: 14px;");
        vbox.getChildren().add(summaryLabel);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(700, 500);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);

        javafx.scene.control.Button closeButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setStyle("-fx-background-color: " + UIUtil.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait();
    }

    /**
     * Show confirmation dialog for borrowing a book
     */
    private static boolean showBorrowConfirmationDialog(Book book) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        UIUtil.setupAlert(confirmAlert, "Do you want to borrow this book?");

        String currentEmail = SwitchSceneUtil.currentUserEmail;
        String message = "📖 Title: " + book.getTitle() + "\n" +
                        "✍️ Author: " + book.getAuthor() + "\n" +
                        "🆔 Book ID: " + book.getId() + "\n" +
                        "📚 Available Copies: " + book.getAvailableCopies() + "\n" +
                        "👤 Borrowing as: " + currentEmail + "\n\n" +
                        "⚠️ Please remember to return the book on time!";

        confirmAlert.setContentText(message);
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Borrow Book");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Borrow a book for the current member
     */
    private static boolean borrowBook(Book book) {
        String currentEmail = SwitchSceneUtil.currentUserEmail;
        
        if (memberHasBorrowedBook(currentEmail, book.getId())) {
            AlertMsg.showError("Already Borrowed", 
                "You have already borrowed this book and haven't returned it yet.\n\n" +
                "Please return it first before borrowing again.");
            return false;
        }
        
        String insertQuery = "INSERT INTO borrowed_books (member_email, book_id, borrow_date, is_returned) VALUES (?, ?, datetime('now'), 0)";
        String updateQuery = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";
        
        try (Connection conn = Connect.getDBConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                
                insertStmt.setString(1, currentEmail);
                insertStmt.setInt(2, book.getId());
                int insertResult = insertStmt.executeUpdate();
                
                updateStmt.setInt(1, book.getId());
                int updateResult = updateStmt.executeUpdate();
                
                if (insertResult > 0 && updateResult > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    AlertMsg.showError("Borrow Failed", "Failed to borrow the book. It might no longer be available.");
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            AlertMsg.showError("Database Error", "An error occurred while borrowing the book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if member has already borrowed a specific book
     */
    private static boolean memberHasBorrowedBook(String memberEmail, int bookId) {
        String query = "SELECT COUNT(*) FROM borrowed_books WHERE member_email = ? AND book_id = ? AND is_returned = 0";
        try {
            int count = Connect.executeCount(query, memberEmail, bookId);
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dialog for returning borrowed books
     */
    public static void showReturnBooksDialog() {
        String currentEmail = SwitchSceneUtil.currentUserEmail;
        List<BorrowedBookInfo> borrowedBooks = getMemberBorrowedBooks(currentEmail, false);
        
        if (borrowedBooks.isEmpty()) {
            AlertMsg.showInformation("No Books to Return", 
                "You don't have any books to return.\n\n" +
                "All your borrowed books have been returned or you haven't borrowed any books yet.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupWarningDialog(dialog, "📚 Return Your Borrowed Books (" + borrowedBooks.size() + " books)"); // USING UIUtil

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label headerLabel = UIUtil.createBorrowTableHeader(); // USING UIUtil
        vbox.getChildren().add(headerLabel);
        
        Label separatorLabel = UIUtil.createBorrowTableSeparator(); // USING UIUtil
        vbox.getChildren().add(separatorLabel);

        for (BorrowedBookInfo borrowedBook : borrowedBooks) {
            String bookInfo = String.format("%-31s | %-26s | %-13s | Click to Return",
                UIUtil.truncateString(borrowedBook.bookTitle, 31),
                UIUtil.truncateString(borrowedBook.bookAuthor, 26),
                borrowedBook.borrowDate.substring(0, 10));
            
            Label bookLabel = new Label(bookInfo);
            bookLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-cursor: hand;");
            
            bookLabel.setOnMouseClicked(e -> {
                boolean returned = returnBook(borrowedBook);
                if (returned) {
                    dialog.close();
                    AlertMsg.showInformation("Success", 
                        "Book returned successfully!\n\n" +
                        "📖 Title: " + borrowedBook.bookTitle + "\n" +
                        "✍️ Author: " + borrowedBook.bookAuthor + "\n" +
                        "📅 Borrowed: " + borrowedBook.borrowDate.substring(0, 10) + "\n" +
                        "📅 Returned: Today\n\n" +
                        "Thank you for returning the book on time!");
                }
            });
            
            UIUtil.applyHoverEffect(bookLabel, 
                UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-cursor: hand;", 
                "#ffe6e6");
            
            vbox.getChildren().add(bookLabel);
        }

        Label summaryLabel = new Label(String.format(
            "\n📊 Books to Return: %d\n" +
            "💡 Tip: Click on any book to return it!",
            borrowedBooks.size()));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIUtil.WARNING_COLOR + "; -fx-font-size: 14px;");
        vbox.getChildren().add(summaryLabel);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(700, 500);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);

        javafx.scene.control.Button closeButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setStyle("-fx-background-color: " + UIUtil.WARNING_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait();
    }

    /**
     * Return a borrowed book
     */
    private static boolean returnBook(BorrowedBookInfo borrowedBook) {
        String updateBorrowQuery = "UPDATE borrowed_books SET is_returned = 1, return_date = datetime('now') WHERE id = ?";
        String updateBookQuery = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
        
        try (Connection conn = Connect.getDBConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement updateBorrowStmt = conn.prepareStatement(updateBorrowQuery);
                 PreparedStatement updateBookStmt = conn.prepareStatement(updateBookQuery)) {
                
                updateBorrowStmt.setInt(1, borrowedBook.borrowId);
                int borrowResult = updateBorrowStmt.executeUpdate();
                
                updateBookStmt.setInt(1, borrowedBook.bookId);
                int bookResult = updateBookStmt.executeUpdate();
                
                if (borrowResult > 0 && bookResult > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    AlertMsg.showError("Return Failed", "Failed to return the book. Please try again.");
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            AlertMsg.showError("Database Error", "An error occurred while returning the book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Show member's borrowed books history
     */
    public static void showMyBorrowedBooksDialog() {
        String currentEmail = SwitchSceneUtil.currentUserEmail;
        List<BorrowedBookInfo> allBorrowedBooks = getMemberBorrowedBooks(currentEmail, true);
        
        if (allBorrowedBooks.isEmpty()) {
            AlertMsg.showInformation("No Borrowing History", 
                "You haven't borrowed any books yet.\n\n" +
                "Use 'Search & Borrow Books' to start borrowing!");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "📚 My Borrowing History (" + allBorrowedBooks.size() + " records)"); // USING UIUtil

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label headerLabel = UIUtil.createTableHeader("TITLE                           | AUTHOR                     | BORROWED DATE | RETURN DATE  | STATUS", UIUtil.PRIMARY_COLOR);
        vbox.getChildren().add(headerLabel);
        
        Label separatorLabel = UIUtil.createTableSeparator("─────────────────────────────────┼────────────────────────────┼───────────────┼──────────────┼─────────");
        vbox.getChildren().add(separatorLabel);

        for (BorrowedBookInfo borrowedBook : allBorrowedBooks) {
            String returnDateStr = borrowedBook.returnDate != null ? borrowedBook.returnDate.substring(0, 10) : "N/A";
            String status = borrowedBook.isReturned ? "Returned" : "Borrowed";
            
            String bookInfo = String.format("%-31s | %-26s | %-13s | %-12s | %s",
                UIUtil.truncateString(borrowedBook.bookTitle, 31),
                UIUtil.truncateString(borrowedBook.bookAuthor, 26),
                borrowedBook.borrowDate.substring(0, 10),
                returnDateStr,
                status);
            
            Label bookLabel = new Label(bookInfo);
            bookLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;");
            
            // Highlight currently borrowed books
            if (!borrowedBook.isReturned) {
                bookLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                
                UIUtil.applyHoverEffect(bookLabel, 
                    UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #fff3cd; -fx-text-fill: #856404;", 
                    "#ffeaa7");
            } else {
                UIUtil.applyHoverEffect(bookLabel, 
                    UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;", 
                    "#f0f8ff");
            }
            
            vbox.getChildren().add(bookLabel);
        }

        int currentlyBorrowed = (int) allBorrowedBooks.stream().filter(b -> !b.isReturned).count();
        int returned = allBorrowedBooks.size() - currentlyBorrowed;
        
        Label summaryLabel = new Label(String.format(
            "\n📊 Your Borrowing Statistics:\n" +
            "   📚 Total Books Borrowed: %d\n" +
            "   📖 Currently Borrowed: %d books\n" +
            "   ✅ Returned: %d books\n" +
            "   📈 Return Rate: %.1f%%",
            allBorrowedBooks.size(), currentlyBorrowed, returned,
            allBorrowedBooks.size() > 0 ? (returned * 100.0 / allBorrowedBooks.size()) : 0.0));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + "; -fx-font-size: 14px;");
        vbox.getChildren().add(summaryLabel);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(800, 500);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);

        javafx.scene.control.Button closeButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setStyle("-fx-background-color: " + UIUtil.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait();
    }

    /**
     * Get member's borrowed books
     */
    private static List<BorrowedBookInfo> getMemberBorrowedBooks(String memberEmail, boolean includeReturned) {
        String query = "SELECT bb.id, b.title, b.author, b.id as book_id, bb.borrow_date, bb.return_date, bb.is_returned " +
                      "FROM borrowed_books bb " +
                      "JOIN books b ON bb.book_id = b.id " +
                      "WHERE bb.member_email = ?";
        
        if (!includeReturned) {
            query += " AND bb.is_returned = 0";
        }
        
        query += " ORDER BY bb.borrow_date DESC";
        
        return Connect.executeQuery(query, rs -> {
            List<BorrowedBookInfo> borrowedBooks = new ArrayList<>();
            try {
                while (rs.next()) {
                    borrowedBooks.add(new BorrowedBookInfo(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("book_id"),
                        rs.getString("borrow_date"),
                        rs.getString("return_date"),
                        rs.getBoolean("is_returned")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("Error processing borrowed books: " + e.getMessage());
            }
            return borrowedBooks;
        }, memberEmail);
    }
}
