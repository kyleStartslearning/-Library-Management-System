package project.Utilities.AdminUtil;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import project.Databases.Connect;
import project.Utilities.SwitchSceneUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class borrowedBooks {
    
    /**
     * Data class to hold borrower information
     */
    public static class BorrowerInfo {
        public final String memberName;
        public final String memberEmail;
        public final String bookTitle;
        public final String bookAuthor;
        public final String borrowDate;
        public final String returnDate;
        public final boolean isReturned;
        
        public BorrowerInfo(String memberName, String memberEmail, String bookTitle, 
                           String bookAuthor, String borrowDate, String returnDate, boolean isReturned) {
            this.memberName = memberName;
            this.memberEmail = memberEmail;
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate;
            this.isReturned = isReturned;
        }
    }

    /**
     * Show all borrowers and their borrowed books
     */
    public static void showAllBorrowersDialog() {
        // Get all borrower information from the database
        List<BorrowerInfo> borrowers = getAllBorrowers();
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(null);
        dialog.setHeaderText("📚 All Library Borrowers (" + borrowers.size() + " borrowed books total)");

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        if (borrowers.isEmpty()) {
            // Show empty state
            VBox emptyVBox = new VBox(20);
            emptyVBox.setPadding(new Insets(40));
            
            Label emptyLabel = new Label("📚 No borrowed books found!");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #666666;");
            
            Label suggestionLabel = new Label("All books are currently available in the library.");
            suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-font-style: italic;");
            
            emptyVBox.getChildren().addAll(emptyLabel, suggestionLabel);
            dialog.getDialogPane().setContent(emptyVBox);
        } else {
            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(20));

            // Add header with column titles
            Label headerLabel = new Label("MEMBER NAME             | EMAIL                     | BOOK TITLE              | AUTHOR                | BORROW DATE  | RETURN DATE  | STATUS");
            headerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0598ff;");
            vbox.getChildren().add(headerLabel);
            
            // Add separator line
            Label separatorLabel = new Label("─────────────────────────┼─────────────────────────┼─────────────────────────┼─────────────────────┼──────────────┼──────────────┼──────────");
            separatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-text-fill: #cccccc;");
            vbox.getChildren().add(separatorLabel);

            // Add each borrower record
            for (BorrowerInfo borrower : borrowers) {
                String status = borrower.isReturned ? "Returned" : "Borrowed";
                String returnDateStr = borrower.returnDate != null ? borrower.returnDate.substring(0, 10) : "N/A";
                String borrowDateStr = borrower.borrowDate != null ? borrower.borrowDate.substring(0, 10) : "N/A";
                
                String borrowerInfo = String.format("%-23s | %-23s | %-23s | %-19s | %-12s | %-12s | %s",
                    truncateString(borrower.memberName, 23),
                    truncateString(borrower.memberEmail, 23),
                    truncateString(borrower.bookTitle, 23),
                    truncateString(borrower.bookAuthor, 19),
                    borrowDateStr,
                    returnDateStr,
                    status);
                
                Label borrowerLabel = new Label(borrowerInfo);
                borrowerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-padding: 2px 0px;");
                
                // Add hover effect
                borrowerLabel.setOnMouseEntered(e -> 
                    borrowerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-padding: 2px 0px; -fx-background-color: #f0f8ff;"));
                borrowerLabel.setOnMouseExited(e -> 
                    borrowerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-padding: 2px 0px;"));
                
                // Highlight currently borrowed books
                if (!borrower.isReturned) {
                    borrowerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-padding: 2px 0px; -fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    
                    borrowerLabel.setOnMouseEntered(e -> 
                        borrowerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-padding: 2px 0px; -fx-background-color: #ffeaa7; -fx-text-fill: #856404;"));
                    borrowerLabel.setOnMouseExited(e -> 
                        borrowerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-padding: 2px 0px; -fx-background-color: #fff3cd; -fx-text-fill: #856404;"));
                }
                
                vbox.getChildren().add(borrowerLabel);
            }

            // Add summary statistics
            int totalBorrows = borrowers.size();
            int currentlyBorrowed = (int) borrowers.stream().filter(b -> !b.isReturned).count();
            int returned = totalBorrows - currentlyBorrowed;
            int uniqueMembers = (int) borrowers.stream().map(b -> b.memberEmail).distinct().count();
            int uniqueBooks = (int) borrowers.stream().map(b -> b.bookTitle).distinct().count();
            
            Label summaryLabel = new Label(String.format(
                "\n📊 Borrowing Statistics:\n" +
                "   📚 Total Borrows: %d records | 👥 Unique Members: %d | 📖 Unique Books: %d\n" +
                "   📖 Currently Borrowed: %d books | ✅ Returned: %d books\n" +
                "   📈 Return Rate: %.1f%%",
                totalBorrows, uniqueMembers, uniqueBooks, currentlyBorrowed, returned, 
                totalBorrows > 0 ? (returned * 100.0 / totalBorrows) : 0.0));
            summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0598ff; -fx-font-size: 14px;");
            vbox.getChildren().add(summaryLabel);

            // Add view information
            String userInfo = SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com";
            Label viewInfoLabel = new Label("\n👤 Viewing as: " + userInfo + " (ADMIN)");
            viewInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
            vbox.getChildren().add(viewInfoLabel);

            ScrollPane scrollPane = new ScrollPane(vbox);
            scrollPane.setPrefSize(1000, 600); // Increased width for all columns
            scrollPane.setFitToWidth(true);

            dialog.getDialogPane().setContent(scrollPane);
        }
        
        // Apply consistent styling
        dialog.getDialogPane().setStyle(
            "-fx-border-color: #0598ff; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );

        // Customize close button
        javafx.scene.control.Button closeButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setStyle("-fx-background-color: #0598ff; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait();
    }

    /**
     * Get all borrower information from the database using the member_borrowed_books view
     */
    private static List<BorrowerInfo> getAllBorrowers() {
        List<BorrowerInfo> borrowers = new ArrayList<>();
        
        String query = "SELECT member_name, member_email, book_title, book_author, " +
                      "borrow_date, return_date, is_returned " +
                      "FROM member_borrowed_books " +
                      "ORDER BY borrow_date DESC, member_name ASC";
        
        try (Connection conn = Connect.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                borrowers.add(new BorrowerInfo(
                    rs.getString("member_name"),
                    rs.getString("member_email"),
                    rs.getString("book_title"),
                    rs.getString("book_author"),
                    rs.getString("borrow_date"),
                    rs.getString("return_date"),
                    rs.getBoolean("is_returned")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving borrower information: " + e.getMessage());
            e.printStackTrace();
        }
        
        return borrowers;
    }

    /**
     * Helper method to truncate strings that are too long for display
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
