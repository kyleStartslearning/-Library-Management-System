package project;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import project.Databases.Admin;
import project.Databases.Book;
import project.Databases.LibraryMember;
import javafx.geometry.Insets;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class UtilityClass {

    public static String currentUserEmail;
    public static String currentUserType; // 'admin' or 'member'
    public static String currentUserName;

    public static void ShowError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void ShowWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void ShowInformation(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void switchScene(ActionEvent event, String fxmlFile, String cssFile) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(UtilityClass.class.getResource("/project/FXML/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            if (cssFile != null) {
                scene.getStylesheets().add(UtilityClass.class.getResource("/project/CSS/" + cssFile).toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            ShowError("Navigation Error", "Failed to load the requested screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
    public static BookData ShowAddBookDialog() {
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
        
        String userInfo = currentUserEmail != null ? currentUserEmail : "admin@library.com";
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
                ShowError("Validation Error", "Book title is required!");
                event.consume();
            } else if (author.isEmpty()) {
                ShowError("Validation Error", "Author name is required!");
                event.consume();
            } else if (copiesText.isEmpty()) {
                ShowError("Validation Error", "Number of copies is required!");
                event.consume();
            } else {
                try {
                    int copies = Integer.parseInt(copiesText);
                    if (copies <= 0) {
                        ShowError("Validation Error", "Number of copies must be greater than 0!");
                        event.consume();
                    } else {
                        // Show confirmation dialog
                        boolean confirmed = ShowBookConfirmationDialog(title, author, copies);
                        if (!confirmed) {
                            event.consume(); // Stay in the dialog if not confirmed
                        }
                    }
                } catch (NumberFormatException e) {
                    ShowError("Validation Error", "Invalid number format for copies!");
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
    public static boolean ShowBookConfirmationDialog(String title, String author, int copies) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Add Book");
        confirmAlert.setHeaderText("Do you really want to add this book?");
        
        String message = "📖 Title: " + title + "\n" +
                        "✍️ Author: " + author + "\n" +
                        "📚 Copies: " + copies + "\n" +
                        "👤 Added by: " + (currentUserEmail != null ? currentUserEmail : "admin@library.com");
        
        confirmAlert.setContentText(message);
        
        // Customize buttons
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Confirm");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static BookData ShowRemoveBook() {
        Dialog<BookData> dialog = new Dialog<>();

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

        String userInfo = currentUserEmail != null ? currentUserEmail : "admin@library.com";
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
                ShowError("Validation Error", "Please enter either a title or author to search!");
                event.consume();
                return;
            }

            // Search for books
            List<Book> foundBooks = Book.searchBooksForDeletion(title, author);
            
            if (foundBooks.isEmpty()) {
                ShowError("No Books Found", "No books found matching your search criteria.");
                event.consume();
                return;
            }

            // Show book selection dialog
            Book selectedBook = ShowBookSelectionDialog(foundBooks);
            if (selectedBook == null) {
                event.consume(); // User cancelled selection
                return;
            }

            // Show confirmation dialog
            boolean confirmed = ShowDeleteConfirmationDialog(selectedBook);
            if (!confirmed) {
                event.consume();
                return;
            }

            // Delete the book
            boolean deleted = Book.deleteBookById(selectedBook.getId());
            if (deleted) {
                ShowInformation("Success", "Book deleted successfully!\n\n" +
                    "📖 Title: " + selectedBook.getTitle() + "\n" +
                    "✍️ Author: " + selectedBook.getAuthor() + "\n" +
                    "🆔 Book ID: " + selectedBook.getId());
            } else {
                ShowError("Error", "Failed to delete the book. Please try again.");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            return null; // We handle everything in the event filter
        });

        Optional<BookData> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Show dialog to select a book from search results
     */
    public static Book ShowBookSelectionDialog(List<Book> books) {
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
                ShowError("Selection Required", "Please select a book to delete!");
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
     * Helper method to truncate strings that are too long for display
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Show confirmation dialog before deleting a book
     */
    public static boolean ShowDeleteConfirmationDialog(Book book) {
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
                    "👤 Deleted by: " + (currentUserEmail != null ? currentUserEmail : "admin@library.com") + "\n\n" +
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
 * Show all members and admins in a dialog with detailed information
 */
public static void ShowAllMembersDialog(List<LibraryMember> members) {
    // Get all admins as well
    List<Admin> admins = Admin.ViewAllAdmins();
    
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle(null);
    dialog.setHeaderText("📋 All Library Users (" + admins.size() + " admins, " + members.size() + " members)");

    // Make dialog undecorated
    dialog.setOnShowing(e -> {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
    });

    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    VBox vbox = new VBox(15);
    vbox.setPadding(new Insets(20));

    // ADMIN ACCOUNTS SECTION
    if (!admins.isEmpty()) {
        Label adminSectionLabel = new Label("🔧 ADMIN ACCOUNTS (" + admins.size() + " total)");
        adminSectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff6b35; -fx-padding: 10px 0px;");
        vbox.getChildren().add(adminSectionLabel);

        // Add admin header with column titles
        Label adminHeaderLabel = new Label("EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT");
        adminHeaderLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ff6b35;");
        vbox.getChildren().add(adminHeaderLabel);
        
        // Add admin separator label
        Label adminSeparatorLabel = new Label("─────────────────────────────────┼──────────────────────────┼─────┼───────────────┼────────────────────");
        adminSeparatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #cccccc;");
        vbox.getChildren().add(adminSeparatorLabel);

        // Add each admin
        for (Admin admin : admins) {
            String createdAtStr = admin.getCreatedAt() != null ? 
                admin.getCreatedAt().toString().substring(0, 19) : "Unknown";
            
            String adminInfo = String.format("%-31s | %-24s | %-3d | %-13s | %s",
                truncateString(admin.getEmail(), 31),
                truncateString(admin.getName(), 24),
                admin.getAge(),
                truncateString(admin.getPhoneNumber(), 13),
                createdAtStr);
            
            Label adminLabel = new Label(adminInfo);
            adminLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #fff5f5;");
            
            // Add hover effect for admins
            adminLabel.setOnMouseEntered(e -> 
                adminLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #ffe6e6;"));
            adminLabel.setOnMouseExited(e -> 
                adminLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #fff5f5;"));
            
            vbox.getChildren().add(adminLabel);
        }
    }

    // Add spacing between sections
    Label spacingLabel = new Label("");
    spacingLabel.setStyle("-fx-padding: 10px 0px;");
    vbox.getChildren().add(spacingLabel);

    // MEMBER ACCOUNTS SECTION
    Label memberSectionLabel = new Label("👥 MEMBER ACCOUNTS (" + members.size() + " total)");
    memberSectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0598ff; -fx-padding: 10px 0px;");
    vbox.getChildren().add(memberSectionLabel);

    if (!members.isEmpty()) {
        // Add member header with column titles
        Label memberHeaderLabel = new Label("EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT");
        memberHeaderLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0598ff;");
        vbox.getChildren().add(memberHeaderLabel);
        
        // Add member separator line
        Label memberSeparatorLabel = new Label("─────────────────────────────────┼──────────────────────────┼─────┼───────────────┼────────────────────");
        memberSeparatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #cccccc;");
        vbox.getChildren().add(memberSeparatorLabel);

        // Add each member
        for (LibraryMember member : members) {
            String createdAtStr = member.getCreatedAt() != null ? 
                member.getCreatedAt().toString().substring(0, 19) : "Unknown";
            
            String memberInfo = String.format("%-31s | %-24s | %-3d | %-13s | %s",
                truncateString(member.getEmail(), 31),
                truncateString(member.getName(), 24),
                member.getAge(),
                truncateString(member.getPhoneNumber(), 13),
                createdAtStr);
            
            Label memberLabel = new Label(memberInfo);
            memberLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px;");
            
            // Add hover effect for members
            memberLabel.setOnMouseEntered(e -> 
                memberLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-background-color: #f0f8ff;"));
            memberLabel.setOnMouseExited(e -> 
                memberLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 2px 0px;"));
            
            vbox.getChildren().add(memberLabel);
        }
    } else {
        Label noMembersLabel = new Label("No members found in the system.");
        noMembersLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #888888; -fx-font-size: 12px;");
        vbox.getChildren().add(noMembersLabel);
    }

    // Add summary at bottom
    Label summaryLabel = new Label("\n📊 Total Users: " + (admins.size() + members.size()) + 
                                  " (" + admins.size() + " admins + " + members.size() + " members)");
    summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 14px;");
    vbox.getChildren().add(summaryLabel);

    ScrollPane scrollPane = new ScrollPane(vbox);
    scrollPane.setPrefSize(800, 600); // Increased height for both sections
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
 * Show dialog for removing a member
 */
public static void ShowRemoveMember() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle(null);
    dialog.setHeaderText("Search member to delete");

    dialog.setOnShowing(e -> {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
    });

    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField nameField = new TextField();
    nameField.setPromptText("Member Name (partial search allowed)");
    nameField.setPrefWidth(250);
    
    TextField emailField = new TextField();
    emailField.setPromptText("Member Email (partial search allowed)");
    emailField.setPrefWidth(250);

    String userInfo = currentUserEmail != null ? currentUserEmail : "admin@library.com";
    Label adminLabel = new Label("Deleting as: " + userInfo + " (ADMIN)");
    adminLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 12px; -fx-font-weight: bold;");

    grid.add(adminLabel, 0, 0, 2, 1);
    grid.add(new Label("Name:"), 0, 1);
    grid.add(nameField, 1, 1);
    grid.add(new Label("Email:"), 0, 2);
    grid.add(emailField, 1, 2);

    dialog.getDialogPane().setContent(grid);
    
    dialog.getDialogPane().setStyle(
        "-fx-border-color: #ff0000; " +
        "-fx-border-width: 2px; " +
        "-fx-border-radius: 5px; " +
        "-fx-background-radius: 5px;"
    );

    nameField.requestFocus();

    javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    
    okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (name.isEmpty() && email.isEmpty()) {
            ShowError("Validation Error", "Please enter either a name or email to search!");
            event.consume();
            return;
        }

        // Search for members
        List<LibraryMember> foundMembers = Admin.searchMembersForDeletion(name, email);
        
        if (foundMembers.isEmpty()) {
            ShowError("No Members Found", "No members found matching your search criteria.");
            event.consume();
            return;
        }

        // Show member selection dialog
        LibraryMember selectedMember = ShowMemberSelectionDialog(foundMembers);
        if (selectedMember == null) {
            event.consume(); // User cancelled selection
            return;
        }

        // Show confirmation dialog
        boolean confirmed = ShowDeleteMemberConfirmationDialog(selectedMember);
        if (!confirmed) {
            event.consume();
            return;
        }

        // Delete the member
        boolean deleted = Admin.deleteMemberByEmail(selectedMember.getEmail());
        if (deleted) {
            ShowInformation("Success", "Member deleted successfully!\n\n" +
                "👤 Name: " + selectedMember.getName() + "\n" +
                "📧 Email: " + selectedMember.getEmail() + "\n" +
                "📞 Phone: " + selectedMember.getPhoneNumber());
        } else {
            ShowError("Error", "Failed to delete the member. Please try again.");
            event.consume();
        }
    });

    dialog.setResultConverter(dialogButton -> {
        return null; // We handle everything in the event filter
    });

    dialog.showAndWait();
}

/**
 * Show dialog to select a member from search results
 */
public static LibraryMember ShowMemberSelectionDialog(List<LibraryMember> members) {
    Dialog<LibraryMember> dialog = new Dialog<>();
    dialog.setTitle(null);
    dialog.setHeaderText("Select member to delete:");

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
    Label headerLabel = new Label("EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT");
    headerLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
    vbox.getChildren().add(headerLabel);
    
    // Add separator line
    Label separatorLabel = new Label("─────────────────────────────────┼──────────────────────────┼─────┼───────────────┼────────────────────");
    separatorLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #cccccc;");
    vbox.getChildren().add(separatorLabel);

    ToggleGroup toggleGroup = new ToggleGroup();
    
    // Create radio buttons for each member
    for (LibraryMember member : members) {
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(toggleGroup);
        radioButton.setUserData(member);
        
        String createdAtStr = member.getCreatedAt() != null ? 
            member.getCreatedAt().toString().substring(0, 19) : "Unknown";
        
        // Create formatted member info with proper spacing
        String memberInfo = String.format("%-31s | %-24s | %-3d | %-13s | %s",
            truncateString(member.getEmail(), 31),
            truncateString(member.getName(), 24),
            member.getAge(),
            truncateString(member.getPhoneNumber(), 13),
            createdAtStr);
        
        radioButton.setText(memberInfo);
        radioButton.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        vbox.getChildren().add(radioButton);
    }

    ScrollPane scrollPane = new ScrollPane(vbox);
    scrollPane.setPrefSize(700, 350);
    scrollPane.setFitToWidth(true);

    dialog.getDialogPane().setContent(scrollPane);

    javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
        if (toggleGroup.getSelectedToggle() == null) {
            ShowError("Selection Required", "Please select a member to delete!");
            event.consume();
        }
    });

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == ButtonType.OK && toggleGroup.getSelectedToggle() != null) {
            return (LibraryMember) toggleGroup.getSelectedToggle().getUserData();
        }
        return null;
    });

    Optional<LibraryMember> result = dialog.showAndWait();
    return result.orElse(null);
}

/**
 * Show confirmation dialog before deleting a member
 */
    public static boolean ShowDeleteMemberConfirmationDialog(LibraryMember member) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle(null);
        confirmAlert.setHeaderText("⚠️ Are you sure you want to delete this member?");

        // Make the dialog undecorated (removes title bar and X button)
        confirmAlert.setOnShowing(e -> {
            Stage stage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        // Apply the same styling as other deletion dialogs
        confirmAlert.getDialogPane().setStyle(
            "-fx-border-color: #ff0000; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );
        
        String createdAtStr = member.getCreatedAt() != null ? 
            member.getCreatedAt().toString().substring(0, 19) : "Unknown";
        
        String message = "👤 Name: " + member.getName() + "\n" +
                        "📧 Email: " + member.getEmail() + "\n" +
                        "🎂 Age: " + member.getAge() + "\n" +
                        "📞 Phone: " + member.getPhoneNumber() + "\n" +
                        "📅 Member Since: " + createdAtStr + "\n" +
                        "🗑️ Deleted by: " + (currentUserEmail != null ? currentUserEmail : "admin@library.com") + "\n\n" +
                        "⚠️ This action cannot be undone!";
        
        confirmAlert.setContentText(message);
        
        // Customize buttons
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Delete Member");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Show dialog for searching books by title or author
     */
    public static void ShowSearchBooksDialog() {
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

        String userInfo = currentUserEmail != null ? currentUserEmail : "admin@library.com";
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
                ShowError("Validation Error", "Please enter a search term!");
                event.consume();
                return;
            }

            // Search for books using the existing searchBooks method
            List<Book> foundBooks = Book.searchBooks(searchTerm);
            
            if (foundBooks.isEmpty()) {
                ShowInformation("No Books Found", 
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
            ShowSearchResultsDialog(foundBooks, searchTerm);
        });

        // Allow Enter key to trigger search
        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show search results in a formatted dialog
     */
    public static void ShowSearchResultsDialog(List<Book> books, String searchTerm) {
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

}

