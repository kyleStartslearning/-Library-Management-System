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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import project.Databases.Admin;
import project.Databases.LibraryMember;
import project.Utilities.AlertMsg;
import javafx.geometry.Insets;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UtilityClass {

    public static String currentUserEmail;
    public static String currentUserType; // 'admin' or 'member'
    public static String currentUserName;

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
            AlertMsg.showError("Navigation Error", "Failed to load the requested screen: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Helper method to truncate strings that are too long for display
     */
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
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

        TextField emailField = new TextField();
        emailField.setPromptText("Member Email (exact match)");
        emailField.setPrefWidth(250);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Member Name (partial search allowed)");
        nameField.setPrefWidth(250);

        String userInfo = currentUserEmail != null ? currentUserEmail : "admin@library.com";
        Label adminLabel = new Label("Deleting as: " + userInfo + " (ADMIN)");
        adminLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 12px; -fx-font-weight: bold;");

        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Name:"), 0, 2);
        grid.add(nameField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        
        dialog.getDialogPane().setStyle(
            "-fx-border-color: #ff0000; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );

        emailField.requestFocus();

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String email = emailField.getText().trim();
            String name = nameField.getText().trim();
            
            if (email.isEmpty() && name.isEmpty()) {
                AlertMsg.showError("Validation Error", "Please enter either an email or name to search!");
                event.consume();
                return;
            }

            // Search for members
            List<LibraryMember> foundMembers = Admin.searchMembersForDeletion(email, name);
            
            if (foundMembers.isEmpty()) {
                AlertMsg.showError("No Members Found", "No members found matching your search criteria.");
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
                AlertMsg.showInformation("Success", "Member deleted successfully!\n\n" +
                    "👤 Name: " + selectedMember.getName() + "\n" +
                    "📧 Email: " + selectedMember.getEmail() + "\n" +
                    "📞 Phone: " + selectedMember.getPhoneNumber());
            } else {
                AlertMsg.showError("Error", "Failed to delete the member. Please try again.");
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
        dialog.setHeaderText("Select the member you want to delete:");

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
        
        // Add separator label
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
            radioButton.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");
            vbox.getChildren().add(radioButton);
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(700, 350);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (toggleGroup.getSelectedToggle() == null) {
                AlertMsg.showError("Selection Required", "Please select a member to delete!");
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
        
        // Apply the same styling
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
                        "📅 Created: " + createdAtStr + "\n" +
                        "🗑️ Deleted by: " + (currentUserEmail != null ? currentUserEmail : "admin@library.com") + "\n\n" +
                        "⚠️ This action cannot be undone!\n" +
                        "⚠️ All borrowed books by this member will also be affected!";
        
        confirmAlert.setContentText(message);
        
        // Customize buttons
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.YES)).setText("Delete Member");
        ((javafx.scene.control.Button) confirmAlert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cancel");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

}

