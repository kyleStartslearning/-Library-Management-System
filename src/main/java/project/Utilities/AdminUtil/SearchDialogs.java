package project.Utilities.AdminUtil;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import project.Databases.Book;
import project.Databases.Admin;
import project.Databases.LibraryMember;
import project.Utilities.AlertMsg;
import project.Utilities.UIUtil;

import java.util.List;

public class SearchDialogs {
    
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

            showSearchResultsDialog(foundBooks, searchTerm);
        });

        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show search results in a formatted dialog
     */
    public static void showSearchResultsDialog(List<Book> books, String searchTerm) {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "📚 Search Results for: '" + searchTerm + "' (" + books.size() + " books found)");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label headerLabel = UIUtil.createTableHeader("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE | BORROWED", UIUtil.PRIMARY_COLOR);
        vbox.getChildren().add(headerLabel);
        
        Label separatorLabel = UIUtil.createTableSeparator("─────┼─────────────────────────────────┼────────────────────────────┼───────┼───────────┼─────────");
        vbox.getChildren().add(separatorLabel);

        for (Book book : books) {
            int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
            
            String bookInfo = String.format("%-5d | %-31s | %-26s | %-5d | %-9d | %-8d",
                book.getId(),
                UIUtil.truncateString(book.getTitle(), 31),
                UIUtil.truncateString(book.getAuthor(), 26),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                borrowedCopies);
            
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

        int totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
        int availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
        int borrowedCopies = totalCopies - availableCopies;
        
        Label summaryLabel = new Label(String.format(
            "\n📊 Summary: %d books found | %d total copies | %d available | %d borrowed",
            books.size(), totalCopies, availableCopies, borrowedCopies));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + "; -fx-font-size: 14px;");
        vbox.getChildren().add(summaryLabel);

        if (books.size() < 5) {
            Label tipsLabel = new Label("\n💡 Tips: Try searching with partial titles, author surnames, or different keywords for more results.");
            tipsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
            vbox.getChildren().add(tipsLabel);
        }

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
     * Show dialog for searching members by name or email
     */
    public static void showSearchMembersDialog() {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "Search Members by Name or Email");

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

            List<LibraryMember> foundMembers = Admin.searchMembersForDeletion(searchTerm, searchTerm);
            
            if (foundMembers.isEmpty()) {
                AlertMsg.showInformation("No Members Found", 
                    "👥 No members found matching: '" + searchTerm + "'\n\n" +
                    "Try searching with:\n" +
                    "• Different spelling\n" +
                    "• Member's full name\n" +
                    "• Partial email address\n" +
                    "• Different keywords");
                event.consume();
                return;
            }

            showMemberSearchResultsDialog(foundMembers, searchTerm);
        });

        searchField.setOnAction(e -> okButton.fire());

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    /**
     * Show member search results in a formatted dialog
     */
    public static void showMemberSearchResultsDialog(List<LibraryMember> members, String searchTerm) {
        Dialog<Void> dialog = new Dialog<>();
        UIUtil.setupDialog(dialog, "👥 Member Search Results for: '" + searchTerm + "' (" + members.size() + " members found)");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label headerLabel = UIUtil.createTableHeader("EMAIL                           | NAME                     | AGE | PHONE         | CREATED AT", UIUtil.PRIMARY_COLOR);
        vbox.getChildren().add(headerLabel);
        
        Label separatorLabel = UIUtil.createTableSeparator("─────────────────────────────────┼──────────────────────────┼─────┼───────────────┼────────────────────");
        vbox.getChildren().add(separatorLabel);

        for (LibraryMember member : members) {
            String createdAtStr = member.getCreatedAt() != null ? 
                member.getCreatedAt().toString().substring(0, 19) : "Unknown";
            
            String memberInfo = String.format("%-31s | %-24s | %-3d | %-13s | %s",
                UIUtil.truncateString(member.getEmail(), 31),
                UIUtil.truncateString(member.getName(), 24),
                member.getAge(),
                UIUtil.truncateString(member.getPhoneNumber(), 13),
                createdAtStr);
            
            Label memberLabel = new Label(memberInfo);
            memberLabel.setStyle(UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;");
            
            UIUtil.applyHoverEffect(memberLabel, 
                UIUtil.MONOSPACE_STYLE + " -fx-font-size: 11px; -fx-padding: 2px 0px;", 
                "#f0f8ff");
            
            vbox.getChildren().add(memberLabel);
        }

        int totalMembers = members.size();
        int youngMembers = (int) members.stream().filter(member -> member.getAge() < 25).count();
        int adultMembers = (int) members.stream().filter(member -> member.getAge() >= 25 && member.getAge() < 60).count();
        int seniorMembers = (int) members.stream().filter(member -> member.getAge() >= 60).count();
        
        Label summaryLabel = new Label(String.format(
            "\n📊 Summary: %d members found\n" +
            "   👶 Young (Under 25): %d members\n" +
            "   👨 Adult (25-59): %d members\n" +
            "   👴 Senior (60+): %d members",
            totalMembers, youngMembers, adultMembers, seniorMembers));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIUtil.PRIMARY_COLOR + "; -fx-font-size: 14px;");
        vbox.getChildren().add(summaryLabel);

        if (members.size() < 5) {
            Label tipsLabel = new Label("\n💡 Tips: Try searching with partial names, email addresses, or different keywords for more results.");
            tipsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
            vbox.getChildren().add(tipsLabel);
        }

        Label viewInfoLabel = UIUtil.createUserLabel("\n👤 Searching", "admin");
        viewInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
        vbox.getChildren().add(viewInfoLabel);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefSize(800, 500);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);

        javafx.scene.control.Button closeButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        closeButton.setStyle("-fx-background-color: " + UIUtil.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait();
    }
}
