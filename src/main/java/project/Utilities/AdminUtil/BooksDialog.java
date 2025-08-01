package project.Utilities.AdminUtil;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import project.Databases.Book;
import project.Utilities.AlertMsg;
import project.Utilities.SwitchSceneUtil;

import java.util.List;
import java.util.Optional;

public class BooksDialog {
    
    /**
     * Data class to hold book information from the dialog
     */
    public static class BookData {
        public String title;
        public String author;
        public int copies;
        
        public BookData(String title, String author, int copies) {
            this.title = title;
            this.author = author;
            this.copies = copies;
        }
    }

    /**
     * Show dialog for adding a new book
     * @return BookData object if user confirms, null if cancelled
     */
    public static BookData showAddBookDialog() {
        Dialog<BookData> dialog = new Dialog<>();
        
        // Remove title and X button, but KEEP headerText
        dialog.setTitle(null);
        dialog.setHeaderText("Enter book information:");
        
        // Make the dialog undecorated (removes title bar and X button)
        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        // Set button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Book Title");
        titleField.setPrefWidth(250);
        
        TextField authorField = new TextField();
        authorField.setPromptText("Author Name");
        authorField.setPrefWidth(250);
        
        TextField copiesField = new TextField();
        copiesField.setPromptText("Number of Copies");
        copiesField.setPrefWidth(250);
        
        String userInfo = SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com";
        Label adminLabel = new Label("Adding as: " + userInfo + " (ADMIN)");
        adminLabel.setStyle("-fx-text-fill: #0598ff; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        copiesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                copiesField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(authorField, 1, 2);
        grid.add(new Label("Copies:"), 0, 3);
        grid.add(copiesField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.getDialogPane().setStyle(
            "-fx-border-color: #0598ff; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );
        
        titleField.requestFocus();
        
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String copiesText = copiesField.getText().trim();
            
            if (title.isEmpty()) {
                AlertMsg.showError("Validation Error", "Book title is required!");
                event.consume();
            } else if (author.isEmpty()) {
                AlertMsg.showError("Validation Error", "Author name is required!");
                event.consume();
            } else if (copiesText.isEmpty()) {
                AlertMsg.showError("Validation Error", "Number of copies is required!");
                event.consume();
            } else {
                try {
                    int copies = Integer.parseInt(copiesText);
                    if (copies <= 0) {
                        AlertMsg.showError("Validation Error", "Number of copies must be greater than 0!");
                        event.consume();
                    } else {
                        // Show confirmation dialog
                        boolean confirmed = showBookConfirmationDialog(title, author, copies);
                        if (!confirmed) {
                            event.consume(); // Stay in the dialog if not confirmed
                        }
                    }
                } catch (NumberFormatException e) {
                    AlertMsg.showError("Validation Error", "Invalid number format for copies!");
                    event.consume();
                }
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();
                    String copiesText = copiesField.getText().trim();
                    
                    if (!title.isEmpty() && !author.isEmpty() && !copiesText.isEmpty()) {
                        int copies = Integer.parseInt(copiesText);
                        if (copies > 0) {
                            return new BookData(title, author, copies);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Return null if validation fails
                }
            }
            return null;
        });

        Optional<BookData> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Show confirmation dialog for adding a book
     * @param title Book title
     * @param author Book author
     * @param copies Number of copies
     * @return true if confirmed, false if cancelled
     */
    public static boolean showBookConfirmationDialog(String title, String author, int copies) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Add Book");
        confirmAlert.setHeaderText("Do you really want to add this book?");
        
        String message = "📖 Title: " + title + "\n" +
                        "✍️ Author: " + author + "\n" +
                        "📚 Copies: " + copies + "\n" +
                        "👤 Added by: " + (SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com");
        
        confirmAlert.setContentText(message);
        
        // Customize buttons
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Confirm");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void showRemoveBookDialog() {
        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle(null);
        dialog.setHeaderText("Search book to delete");

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Book Title (partial search allowed)");
        titleField.setPrefWidth(250);
        
        TextField authorField = new TextField();
        authorField.setPromptText("Author Name (partial search allowed)");
        authorField.setPrefWidth(250);

        String userInfo = SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com";
        Label adminLabel = new Label("Deleting as: " + userInfo + " (ADMIN)");
        adminLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 12px; -fx-font-weight: bold;");

        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(authorField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        
        dialog.getDialogPane().setStyle(
            "-fx-border-color: #ff0000; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );

        titleField.requestFocus();

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            
            if (title.isEmpty() && author.isEmpty()) {
                AlertMsg.showError("Validation Error", "Please enter either a title or author to search!");
                event.consume();
                return;
            }

            // Search for books
            List<Book> foundBooks = Book.searchBooksForDeletion(title, author);
            
            if (foundBooks.isEmpty()) {
                AlertMsg.showError("No Books Found", "No books found matching your search criteria.");
                event.consume();
                return;
            }

            // Show book selection dialog
            Book selectedBook = showBookSelectionDialog(foundBooks);
            if (selectedBook == null) {
                event.consume(); // User cancelled selection
                return;
            }

            // Show confirmation dialog
            boolean confirmed = showDeleteConfirmationDialog(selectedBook);
            if (!confirmed) {
                event.consume();
                return;
            }

            // Delete the book
            boolean deleted = Book.deleteBookById(selectedBook.getId());
            if (deleted) {
                AlertMsg.showInformation("Success", "Book deleted successfully!\n\n" +
                    "📖 Title: " + selectedBook.getTitle() + "\n" +
                    "✍️ Author: " + selectedBook.getAuthor() + "\n" +
                    "🆔 Book ID: " + selectedBook.getId());
            } else {
                AlertMsg.showError("Error", "Failed to delete the book. Please try again.");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            return null; // We handle everything in the event filter
        });

        dialog.showAndWait();
    }

    /**
     * Show dialog to select a book from search results
     */
    public static Book showBookSelectionDialog(List<Book> books) {
        Dialog<Book> dialog = new Dialog<>();

        dialog.setTitle(null);
        dialog.setHeaderText("Select the book you want to delete:");

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

         dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        

        dialog.getDialogPane().setStyle(
            "-fx-border-color: #ff0000; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Add header label with column titles
        Label headerLabel = new Label("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE");
        headerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
        vbox.getChildren().add(headerLabel);
        
        // Add separator label
        Label separatorLabel = new Label("─────┼─────────────────────────────────┼────────────────────────────┼───────┼──────────");
        separatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #cccccc;");
        vbox.getChildren().add(separatorLabel);

        ToggleGroup toggleGroup = new ToggleGroup();
        
        // Create radio buttons for each book
        for (Book book : books) {
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(book);
            
            // Create formatted book info with proper spacing
            String bookInfo = String.format("%-5d | %-31s | %-26s | %-5d | %-9d",
                book.getId(), 
                truncateString(book.getTitle(), 31), 
                truncateString(book.getAuthor(), 26), 
                book.getTotalCopies(), 
                book.getAvailableCopies());
            
            radioButton.setText(bookInfo);
            radioButton.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");
            vbox.getChildren().add(radioButton);
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(700, 350); // Increased width for better spacing
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (toggleGroup.getSelectedToggle() == null) {
                AlertMsg.showError("Selection Required", "Please select a book to delete!");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK && toggleGroup.getSelectedToggle() != null) {
                return (Book) toggleGroup.getSelectedToggle().getUserData();
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Show confirmation dialog before deleting a book
     */
    public static boolean showDeleteConfirmationDialog(Book book) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle(null);
        confirmAlert.setHeaderText("⚠️ Are you sure you want to delete this book?");

        // Make the dialog undecorated (removes title bar and X button)
        confirmAlert.setOnShowing(e -> {
            Stage stage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        // Apply the same styling as ShowBookSelectionDialog
        confirmAlert.getDialogPane().setStyle(
            "-fx-border-color: #ff0000; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );
        
        String message = "🆔 Book ID: " + book.getId() + "\n" +
                        "📖 Title: " + book.getTitle() + "\n" +
                        "✍️ Author: " + book.getAuthor() + "\n" +
                        "📚 Total Copies: " + book.getTotalCopies() + "\n" +
                        "📗 Available Copies: " + book.getAvailableCopies() + "\n" +
                        "👤 Deleted by: " + (SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com") + "\n\n" +
                        "⚠️ This action cannot be undone!";
        
        confirmAlert.setContentText(message);
        
        // Customize buttons
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Delete Book");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

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

            // Show search results - UPDATED TO USE SearchDialogs
            SearchDialogs.showSearchResultsDialog(foundBooks, searchTerm);
        });

        // Allow Enter key to trigger search
        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show all books in the library in a formatted dialog
     */
    public static void showAllBooksDialog() {
        // Get all books from the database
        List<Book> allBooks = Book.getAllBooks();
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(null);
        dialog.setHeaderText("📚 All Library Books (" + allBooks.size() + " books total)");

        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        if (allBooks.isEmpty()) {
            // Show empty state
            VBox emptyVBox = new VBox(20);
            emptyVBox.setPadding(new Insets(40));
            
            Label emptyLabel = new Label("📚 No books found in the library!");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #666666;");
            
            Label suggestionLabel = new Label("To add books, use the 'Add Books' button in the admin panel.");
            suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-font-style: italic;");
            
            emptyVBox.getChildren().addAll(emptyLabel, suggestionLabel);
            dialog.getDialogPane().setContent(emptyVBox);
        } else {
            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(20));

            // Add header with column titles
            Label headerLabel = new Label("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE | BORROWED | STATUS");
            headerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0598ff;");
            vbox.getChildren().add(headerLabel);
            
            // Add separator line
            Label separatorLabel = new Label("─────┼─────────────────────────────────┼────────────────────────────┼───────┼───────────┼──────────┼──────────");
            separatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #cccccc;");
            vbox.getChildren().add(separatorLabel);

            // Add each book
            for (Book book : allBooks) {
                int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
                String status = book.getAvailableCopies() > 0 ? "Available" : "Out of Stock";
                
                String bookInfo = String.format("%-5d | %-31s | %-26s | %-5d | %-9d | %-8d | %s",
                    book.getId(),
                    truncateString(book.getTitle(), 31),
                    truncateString(book.getAuthor(), 26),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    borrowedCopies,
                    status);
                
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
                    
                    bookLabel.setOnMouseEntered(e -> 
                        bookLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #ffcccc; -fx-text-fill: #cc0000;"));
                    bookLabel.setOnMouseExited(e -> 
                        bookLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #ffe6e6; -fx-text-fill: #cc0000;"));
                }
                
                vbox.getChildren().add(bookLabel);
            }

            // Add summary statistics
            int totalBooks = allBooks.size();
            int totalCopies = allBooks.stream().mapToInt(Book::getTotalCopies).sum();
            int availableCopies = allBooks.stream().mapToInt(Book::getAvailableCopies).sum();
            int borrowedCopies = totalCopies - availableCopies;
            int outOfStockBooks = (int) allBooks.stream().filter(book -> book.getAvailableCopies() == 0).count();
            int availableBooks = totalBooks - outOfStockBooks;
            
            Label summaryLabel = new Label(String.format(
                "\n📊 Library Statistics:\n" +
                "   📚 Total Books: %d titles | 📦 Total Copies: %d\n" +
                "   ✅ Available: %d copies (%d books) | 📖 Borrowed: %d copies\n" +
                "   ❌ Out of Stock: %d books",
                totalBooks, totalCopies, availableCopies, availableBooks, borrowedCopies, outOfStockBooks));
            summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0598ff; -fx-font-size: 14px;");
            vbox.getChildren().add(summaryLabel);

            // Add view information
            String userInfo = SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com";
            Label viewInfoLabel = new Label("\n👤 Viewing as: " + userInfo + " (ADMIN)");
            viewInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
            vbox.getChildren().add(viewInfoLabel);

            ScrollPane scrollPane = new ScrollPane(vbox);
            scrollPane.setPrefSize(900, 600); // Increased width for the status column
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
     * Helper method to truncate strings that are too long for display
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
