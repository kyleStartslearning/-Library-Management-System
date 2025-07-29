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
            // Use correct path format for resources
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
     * Show dialog for adding a new book
     * @return BookData object if user confirms, null if cancelled
     */
    public static BookData ShowAddBookDialog() {
        Dialog<BookData> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter book information:");
        
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
        
        // Show who is adding the book
        String userInfo = currentUserEmail != null ? currentUserEmail : "admin@library.com";
        Label adminLabel = new Label("Adding as: " + userInfo + " (ADMIN)");
        adminLabel.setStyle("-fx-text-fill: #0598ff; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Add validation for numbers only
        copiesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                copiesField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        grid.add(adminLabel, 0, 0, 2, 1);  // Span across both columns
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(authorField, 1, 2);
        grid.add(new Label("Copies:"), 0, 3);
        grid.add(copiesField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus on title field
        titleField.requestFocus();
        
        // Convert result to BookData when OK is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String copiesText = copiesField.getText().trim();
                
                if (title.isEmpty() || author.isEmpty() || copiesText.isEmpty()) {
                    ShowError("Validation Error", "All fields are required!");
                    return null;
                }
                
                try {
                    int copies = Integer.parseInt(copiesText);
                    if (copies <= 0) {
                        ShowError("Validation Error", "Number of copies must be greater than 0!");
                        return null;
                    }
                    return new BookData(title, author, copies);
                } catch (NumberFormatException e) {
                    ShowError("Validation Error", "Please enter a valid number for copies!");
                    return null;
                }
            }
            return null;
        });
        
        Optional<BookData> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    // Data class for returning dialog results
    public static class BookData {
        public final String title;
        public final String author;
        public final int copies;
        
        public BookData(String title, String author, int copies) {
            this.title = title;
            this.author = author;
            this.copies = copies;
        }
    }
}