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
import javafx.geometry.Insets;
import java.io.IOException;
import java.util.Optional;

public class UtilityClass {

    public static String currentUserEmail;
    public static String currentUserType; // 'admin' or 'member'
    public static String currentUserName;

    public static void ShowError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
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

}
