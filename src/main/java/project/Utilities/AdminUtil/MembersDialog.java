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
import project.Databases.Admin;
import project.Databases.LibraryMember;
import project.Utilities.AlertMsg;
import project.Utilities.SwitchSceneUtil;
import project.Utilities.UIUtil;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class MembersDialog {

    /**
     * Show all members and admins in a dialog with detailed information
     */
    public static void showAllMembersDialog(List<LibraryMember> members) {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "📋 All Library Members & Admins (" + members.size() + " total)");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Get admins separately and convert to LibraryMember objects
        List<Admin> adminList = Admin.ViewAllAdmins();
        List<LibraryMember> admins = convertAdminsToLibraryMembers(adminList);
        List<LibraryMember> regularMembers = members;

        // MEMBERS SECTION
        if (!regularMembers.isEmpty()) {
            Label memberSectionLabel = new Label("👥 LIBRARY MEMBERS (" + regularMembers.size() + " total)");
            memberSectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + "; -fx-padding: 10px 0px;");
            vbox.getChildren().add(memberSectionLabel);

            // Add member header with column titles using UIUtil
            Label memberHeaderLabel = UIUtil.createTableHeader(
                "EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT", 
                UIUtil.PRIMARY_COLOR);
            vbox.getChildren().add(memberHeaderLabel);
            
            // Add member separator label using UIUtil
            Label memberSeparatorLabel = UIUtil.createTableSeparator(
                "─────────────────────────────────┼────────────────────────────┼─────┼───────────────┼──────────────");
            vbox.getChildren().add(memberSeparatorLabel);

            // Add each member
            for (LibraryMember member : regularMembers) {
                String memberInfo = String.format("%-31s | %-26s | %-3d | %-13s | %s",
                    UIUtil.truncateString(member.getEmail(), 31),
                    UIUtil.truncateString(member.getName(), 26),
                    member.getAge(),
                    UIUtil.truncateString(member.getPhoneNumber(), 13), // Fixed method name
                    member.getCreatedAt().toString().substring(0, 10));
                
                Label memberLabel = new Label(memberInfo);
                memberLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;");
                
                // Add hover effect using UIUtil
                UIUtil.applyHoverEffect(memberLabel, 
                    UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;", 
                    "#f0f8ff");
                
                vbox.getChildren().add(memberLabel);
            }
        }

        // ADMINS SECTION
        if (!admins.isEmpty()) {
            Label adminSectionLabel = new Label("🔧 ADMIN ACCOUNTS (" + admins.size() + " total)");
            adminSectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UIUtil.WARNING_COLOR + "; -fx-padding: 10px 0px;");
            vbox.getChildren().add(adminSectionLabel);

            // Add admin header with column titles using UIUtil
            Label adminHeaderLabel = UIUtil.createTableHeader(
                "EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT", 
                UIUtil.WARNING_COLOR);
            vbox.getChildren().add(adminHeaderLabel);
            
            // Add admin separator label using UIUtil
            Label adminSeparatorLabel = UIUtil.createTableSeparator(
                "─────────────────────────────────┼────────────────────────────┼─────┼───────────────┼──────────────");
            vbox.getChildren().add(adminSeparatorLabel);

            // Add each admin
            for (LibraryMember admin : admins) {
                String adminInfo = String.format("%-31s | %-26s | %-3d | %-13s | %s",
                    UIUtil.truncateString(admin.getEmail(), 31),
                    UIUtil.truncateString(admin.getName(), 26),
                    admin.getAge(),
                    UIUtil.truncateString(admin.getPhoneNumber(), 13), // Fixed method name
                    admin.getCreatedAt().toString().substring(0, 10));
                
                Label adminLabel = new Label(adminInfo);
                adminLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-text-fill: " + UIUtil.WARNING_COLOR + ";");
                
                // Add hover effect using UIUtil
                UIUtil.applyHoverEffect(adminLabel, 
                    UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px; -fx-text-fill: " + UIUtil.WARNING_COLOR + ";", 
                    "#fff3cd");
                
                vbox.getChildren().add(adminLabel);
            }
        }

        // Add summary statistics
        Label summaryLabel = new Label(String.format(
            "\n📊 User Statistics:\n" +
            "   👥 Total Members: %d\n" +
            "   🔧 Total Admins: %d\n" +
            "   📈 Total Users: %d",
            regularMembers.size(), admins.size(), regularMembers.size() + admins.size()));
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

    // Convert Admin objects to LibraryMember objects for consistent display
    private static List<LibraryMember> convertAdminsToLibraryMembers(List<Admin> adminList) {
        List<LibraryMember> libraryMembers = new ArrayList<>();
        for (Admin admin : adminList) {
            LibraryMember member = new LibraryMember();
            member.setEmail(admin.getEmail());
            member.setName(admin.getName());
            member.setPassword(admin.getPassword());
            member.setAge(admin.getAge());
            member.setPhoneNumber(admin.getPhoneNumber());
            member.setCreatedAt(admin.getCreatedAt());
            libraryMembers.add(member);
        }
        return libraryMembers;
    }

    /**
     * Show dialog for removing a member
     */
    public static void showRemoveMemberDialog() {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDangerDialog(dialog, "Search member to delete");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = UIUtil.createDialogGrid();

        TextField emailField = new TextField();
        emailField.setPromptText("Member Email (exact match)");
        emailField.setPrefWidth(250);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Member Name (partial search allowed)");
        nameField.setPrefWidth(250);

        Label adminLabel = UIUtil.createDangerUserLabel("Deleting", "admin");

        grid.add(adminLabel, 0, 0, 2, 1);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Name:"), 0, 2);
        grid.add(nameField, 1, 2);

        dialog.getDialogPane().setContent(grid);
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
            LibraryMember selectedMember = showMemberSelectionDialog(foundMembers);
            if (selectedMember == null) {
                event.consume(); // User cancelled selection
                return;
            }

            // Show confirmation dialog
            boolean confirmed = showDeleteMemberConfirmationDialog(selectedMember);
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

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show dialog to select a member from search results
     */
    public static LibraryMember showMemberSelectionDialog(List<LibraryMember> members) {
        Dialog<LibraryMember> dialog = new Dialog<>();
        UIUtil.setupDangerDialog(dialog, "Select the member you want to delete:");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Add header label with column titles using UIUtil
        Label headerLabel = UIUtil.createTableHeader(
            "EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT", 
            UIUtil.DANGER_COLOR);
        vbox.getChildren().add(headerLabel);
        
        // Add separator label using UIUtil
        Label separatorLabel = UIUtil.createTableSeparator(
            "─────────────────────────────────┼──────────────────────────┼─────┼───────────────┼────────────────────");
        vbox.getChildren().add(separatorLabel);

        ToggleGroup toggleGroup = new ToggleGroup();
        
        // Create radio buttons for each member
        for (LibraryMember member : members) {
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(member);
            
            String createdAtStr = member.getCreatedAt() != null ? 
                member.getCreatedAt().toString().substring(0, 19) : "Unknown";
            
            // Create formatted member info with proper spacing using UIUtil
            String memberInfo = String.format("%-31s | %-24s | %-3d | %-13s | %s",
                UIUtil.truncateString(member.getEmail(), 31),
                UIUtil.truncateString(member.getName(), 24),
                member.getAge(),
                UIUtil.truncateString(member.getPhoneNumber(), 13), // Fixed method name
                createdAtStr);
            
            radioButton.setText(memberInfo);
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
    public static boolean showDeleteMemberConfirmationDialog(LibraryMember member) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle(null);
        confirmAlert.setHeaderText("⚠️ Are you sure you want to delete this member?");

        confirmAlert.setOnShowing(e -> {
            Stage stage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        // Use UIUtil for styling
        confirmAlert.getDialogPane().setStyle(UIUtil.DANGER_DIALOG_STYLE);
        
        String createdAtStr = member.getCreatedAt() != null ? 
            member.getCreatedAt().toString().substring(0, 19) : "Unknown";
        
        String message = "👤 Name: " + member.getName() + "\n" +
                        "📧 Email: " + member.getEmail() + "\n" +
                        "🎂 Age: " + member.getAge() + "\n" +
                        "📞 Phone: " + member.getPhoneNumber() + "\n" + // Fixed method name
                        "📅 Created: " + createdAtStr + "\n" +
                        "🗑️ Deleted by: " + (SwitchSceneUtil.currentUserEmail != null ? SwitchSceneUtil.currentUserEmail : "admin@library.com") + "\n\n" +
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

    /**
     * Show dialog for searching members (both regular members and admins)
     */
    public static void showSearchMembersDialog() {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "Search Library Members");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = UIUtil.createDialogGrid();
        TextField searchField = new TextField();
        searchField.setPromptText("Enter member name or email");
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

            // Use existing searchMembersForDeletion method for search functionality
            List<LibraryMember> foundMembers = Admin.searchMembersForDeletion(searchTerm, searchTerm);
            
            if (foundMembers.isEmpty()) {
                AlertMsg.showInformation("No Members Found", 
                    "👥 No members found matching: '" + searchTerm + "'\n\n" +
                    "Try searching with:\n" +
                    "• Different spelling\n" +
                    "• Full name or partial name\n" +
                    "• Email address\n" +
                    "• Different keywords");
                event.consume();
                return;
            }

            // Show search results using the existing method
            showAllMembersDialog(foundMembers);
        });

        // Allow Enter key to trigger search
        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }
}
