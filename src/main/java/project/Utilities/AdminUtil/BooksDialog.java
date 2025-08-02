package project.Utilities.AdminUtil;

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
import project.Utilities.UIUtil;


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
        UIUtil.setupDialog(dialog, "Enter book information:");
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        GridPane grid = UIUtil.createDialogGrid();
        
        TextField titleField = new TextField();
        titleField.setPromptText("Book Title");
        titleField.setPrefWidth(250);
        
        TextField authorField = new TextField();
        authorField.setPromptText("Author Name");
        authorField.setPrefWidth(250);
        
        TextField copiesField = new TextField();
        copiesField.setPromptText("Number of Copies");
        copiesField.setPrefWidth(250);
        
        Label adminLabel = UIUtil.createUserLabel("Adding", "admin");
        
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
                        boolean confirmed = showBookConfirmationDialog(title, author, copies);
                        if (!confirmed) {
                            event.consume();
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
        UIUtil.setupAlert(confirmAlert, "Do you really want to add this book?");
        
        String message = "📖 Title: " + title + "\n" +
                        "✍️ Author: " + author + "\n" +
                        "📚 Copies: " + copies + "\n" +
                        "👤 Added by: " + (SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com");
        
        confirmAlert.setContentText(message);
        
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Confirm");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void showRemoveBookDialog() {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDangerDialog(dialog, "Search book to delete");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = UIUtil.createDialogGrid();

        TextField titleField = new TextField();
        titleField.setPromptText("Book Title (partial search allowed)");
        titleField.setPrefWidth(250);
        
        TextField authorField = new TextField();
        authorField.setPromptText("Author Name (partial search allowed)");
        authorField.setPrefWidth(250);

        Label adminLabel = UIUtil.createDangerUserLabel("Deleting", "admin");

        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(authorField, 1, 2);

        dialog.getDialogPane().setContent(grid);
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

            List<Book> foundBooks = Book.searchBooksForDeletion(title, author);
            
            if (foundBooks.isEmpty()) {
                AlertMsg.showError("No Books Found", "No books found matching your search criteria.");
                event.consume();
                return;
            }

            Book selectedBook = showBookSelectionDialog(foundBooks);
            if (selectedBook == null) {
                event.consume();
                return;
            }

            boolean confirmed = showDeleteConfirmationDialog(selectedBook);
            if (!confirmed) {
                event.consume();
                return;
            }

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

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show dialog to select a book from search results
     */
    public static Book showBookSelectionDialog(List<Book> books) {
        Dialog<Book> dialog = new Dialog<>();
        UIUtil.setupDangerDialog(dialog, "Select the book you want to delete:");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label headerLabel = UIUtil.createTableHeader("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE", UIUtil.DANGER_COLOR);
        vbox.getChildren().add(headerLabel);
        
        Label separatorLabel = UIUtil.createTableSeparator("─────┼─────────────────────────────────┼────────────────────────────┼───────┼──────────");
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
                UIUtil.truncateString(book.getTitle(), 31), 
                UIUtil.truncateString(book.getAuthor(), 26), 
                book.getTotalCopies(), 
                book.getAvailableCopies());
            
            radioButton.setText(bookInfo);
            radioButton.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 12px;");
            vbox.getChildren().add(radioButton);
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(700, 350);
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

        confirmAlert.setOnShowing(e -> {
            Stage stage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        confirmAlert.getDialogPane().setStyle(UIUtil.DANGER_DIALOG_STYLE);
        
        String message = "🆔 Book ID: " + book.getId() + "\n" +
                        "📖 Title: " + book.getTitle() + "\n" +
                        "✍️ Author: " + book.getAuthor() + "\n" +
                        "📚 Total Copies: " + book.getTotalCopies() + "\n" +
                        "📗 Available Copies: " + book.getAvailableCopies() + "\n" +
                        "👤 Deleted by: " + (SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com") + "\n\n" +
                        "⚠️ This action cannot be undone!";
        
        confirmAlert.setContentText(message);
        
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
        UIUtil.setupDialog(dialog, "Search Books by Title or Author");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = UIUtil.createDialogGrid();
        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title or author name");
        searchField.setPrefWidth(300);

        Label adminLabel = UIUtil.createUserLabel("Searching", "admin");

        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Search Term:"), 0, 1);
        grid.add(searchField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        searchField.requestFocus();
        
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String searchTerm = searchField.getText().trim();
            
            if (searchTerm.isEmpty()) {
                AlertMsg.showError("Validation Error", "Please enter a search term!");
                event.consume();
                return;
            }

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

            SearchDialogs.showSearchResultsDialog(foundBooks, searchTerm);
        });

        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show all books in the library in a formatted dialog
     */
    public static void showAllBooksDialog() {
        List<Book> allBooks = Book.getAllBooks();
        
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "📚 All Library Books (" + allBooks.size() + " books total)");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        if (allBooks.isEmpty()) {
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

            Label headerLabel = UIUtil.createTableHeader("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE | BORROWED | STATUS", UIUtil.PRIMARY_COLOR);
            vbox.getChildren().add(headerLabel);
            
            Label separatorLabel = UIUtil.createTableSeparator("─────┼─────────────────────────────────┼────────────────────────────┼───────┼───────────┼──────────┼──────────");
            vbox.getChildren().add(separatorLabel);

            // Add each book
            for (Book book : allBooks) {
                int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
                String status = book.getAvailableCopies() > 0 ? "Available" : "Out of Stock";
                
                String bookInfo = String.format("%-5d | %-31s | %-26s | %-5d | %-9d | %-8d | %s",
                    book.getId(),
                    UIUtil.truncateString(book.getTitle(), 31),
                    UIUtil.truncateString(book.getAuthor(), 26),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    borrowedCopies,
                    status);
                
                Label bookLabel = new Label(bookInfo);
                bookLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;");
                
                UIUtil.applyHoverEffect(bookLabel, 
                    UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;", 
                    "#f0f8ff");
                
                if (book.getAvailableCopies() == 0) {
                    bookLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #ffe6e6; -fx-text-fill: " + UIUtil.DANGER_COLOR + ";");
                    
                    UIUtil.applyHoverEffect(bookLabel, 
                        UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #ffe6e6; -fx-text-fill: " + UIUtil.DANGER_COLOR + ";", 
                        "#ffcccc");
                }
                
                vbox.getChildren().add(bookLabel);
            }

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
            summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + "; -fx-font-size: 14px;");
            vbox.getChildren().add(summaryLabel);

            Label viewInfoLabel = UIUtil.createUserLabel("\n👤 Viewing", "admin");
            viewInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
            vbox.getChildren().add(viewInfoLabel);

            ScrollPane scrollPane = new ScrollPane(vbox);
            scrollPane.setPrefSize(900, 600);
            scrollPane.setFitToWidth(true);

            dialog.getDialogPane().setContent(scrollPane);
        }

        javafx.scene.control.Button closeButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setStyle("-fx-background-color: " + UIUtil.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait();
    }
}
