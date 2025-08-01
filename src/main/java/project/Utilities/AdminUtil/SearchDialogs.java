package project.Utilities.AdminUtil;

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
import project.Utilities.AlertMsg;
import project.Utilities.SwitchSceneUtil;

import java.util.List;

public class SearchDialogs {
    
    /**
     * Show dialog for searching books by title or author
     */
    public static void showSearchBooksDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(null);
        dialog.setHeaderText("Search Books by Title or Author");

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title or author name");
        searchField.setPrefWidth(300);

        String userInfo = SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com";
        Label adminLabel = new Label("Searching as: " + userInfo + " (ADMIN)");
        adminLabel.setStyle("-fx-text-fill: #0598ff; -fx-font-size: 12px; -fx-font-weight: bold;");

        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Search Term:"), 0, 1);
        grid.add(searchField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        
        dialog.getDialogPane().setStyle(
            "-fx-border-color: #0598ff; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );

        searchField.requestFocus();
        
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String searchTerm = searchField.getText().trim();
            
            if (searchTerm.isEmpty()) {
                AlertMsg.showError("Validation Error", "Please enter a search term!");
                event.consume();
                return;
            }

            // Search for books using the existing searchBooks method
            List<Book> foundBooks = Book.searchBooks(searchTerm);
            
            if (foundBooks.isEmpty()) {
                AlertMsg.showInformation("No Books Found", 
                    "📚 No books found matching: '" + searchTerm + "'\n\n" +
                    "Try searching with:\n" +
                    "• Different spelling\n" +
                    "• Author's last name\n" +
                    "• Partial book title\n" +
                    "• Different keywords");
                event.consume();
                return;
            }

            // Show search results
            showSearchResultsDialog(foundBooks, searchTerm);
        });

        // Allow Enter key to trigger search
        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show search results in a formatted dialog
     */
    public static void showSearchResultsDialog(List<Book> books, String searchTerm) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(null);
        dialog.setHeaderText("📚 Search Results for: '" + searchTerm + "' (" + books.size() + " books found)");

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Add header with column titles
        Label headerLabel = new Label("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE | BORROWED");
        headerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0598ff;");
        vbox.getChildren().add(headerLabel);
        
        // Add separator line
        Label separatorLabel = new Label("─────┼─────────────────────────────────┼────────────────────────────┼───────┼───────────┼─────────");
        separatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #cccccc;");
        vbox.getChildren().add(separatorLabel);

        // Add each book
        for (Book book : books) {
            int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
            
            String bookInfo = String.format("%-5d | %-31s | %-26s | %-5d | %-9d | %-8d",
                book.getId(),
                truncateString(book.getTitle(), 31),
                truncateString(book.getAuthor(), 26),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                borrowedCopies);
            
            Label bookLabel = new Label(bookInfo);
            bookLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px;");
            
            // Add hover effect
            bookLabel.setOnMouseEntered(e -> 
                bookLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #f0f8ff;"));
            bookLabel.setOnMouseExited(e -> 
                bookLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px;"));
            
            // Highlight unavailable books
            if (book.getAvailableCopies() == 0) {
                bookLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #ffe6e6; -fx-text-fill: #cc0000;");
            }
            
            vbox.getChildren().add(bookLabel);
        }

        // Add summary statistics
        int totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
        int availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
        int borrowedCopies = totalCopies - availableCopies;
        
        Label summaryLabel = new Label(String.format(
            "\n📊 Summary: %d books found | %d total copies | %d available | %d borrowed",
            books.size(), totalCopies, availableCopies, borrowedCopies));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0598ff; -fx-font-size: 14px;");
        vbox.getChildren().add(summaryLabel);

        // Add search tips
        if (books.size() < 5) {
            Label tipsLabel = new Label("\n💡 Tips: Try searching with partial titles, author surnames, or different keywords for more results.");
            tipsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
            vbox.getChildren().add(tipsLabel);
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(800, 500);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        
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
     * Helper method to truncate strings that are too long for display
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
