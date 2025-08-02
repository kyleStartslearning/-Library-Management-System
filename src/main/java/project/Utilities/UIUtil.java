package project.Utilities;

import javafx.scene.control.Dialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;

public class UIUtil {
    
    // ==================== COLOR CONSTANTS ====================
    public static final String PRIMARY_COLOR = "#0598ff";
    public static final String DANGER_COLOR = "#ff0000";
    public static final String WARNING_COLOR = "#ff6b35";
    public static final String GRAY_COLOR = "#cccccc";
    
    // ==================== CSS STYLES ====================
    public static final String PRIMARY_DIALOG_STYLE = 
        "-fx-border-color: " + PRIMARY_COLOR + "; " +
        "-fx-border-width: 2px; " +
        "-fx-border-radius: 5px; " +
        "-fx-background-radius: 5px;";
    
    public static final String DANGER_DIALOG_STYLE = 
        "-fx-border-color: " + DANGER_COLOR + "; " +
        "-fx-border-width: 2px; " +
        "-fx-border-radius: 5px; " +
        "-fx-background-radius: 5px;";
    
    public static final String WARNING_DIALOG_STYLE = 
        "-fx-border-color: " + WARNING_COLOR + "; " +
        "-fx-border-width: 2px; " +
        "-fx-border-radius: 5px; " +
        "-fx-background-radius: 5px;";
    
    public static final String MONOSPACE_STYLE = "-fx-font-family: 'Consolas', monospace;";
    
    // ==================== DIALOG SETUP METHODS ====================
    
    /**
     * Setup undecorated dialog with primary styling
     */
    public static void setupDialog(Dialog<?> dialog, String headerText) {
        dialog.setTitle(null);
        dialog.setHeaderText(headerText);
        
        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        dialog.getDialogPane().setStyle(PRIMARY_DIALOG_STYLE);
    }
    
    /**
     * Setup undecorated dialog with danger styling
     */
    public static void setupDangerDialog(Dialog<?> dialog, String headerText) {
        dialog.setTitle(null);
        dialog.setHeaderText(headerText);
        
        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        dialog.getDialogPane().setStyle(DANGER_DIALOG_STYLE);
    }
    
    /**
     * Setup undecorated dialog with warning styling
     */
    public static void setupWarningDialog(Dialog<?> dialog, String headerText) {
        dialog.setTitle(null);
        dialog.setHeaderText(headerText);
        
        dialog.setOnShowing(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        dialog.getDialogPane().setStyle(WARNING_DIALOG_STYLE);
    }
    
    /**
     * Setup undecorated alert with primary styling
     */
    public static void setupAlert(Alert alert, String headerText) {
        alert.setTitle(null);
        alert.setHeaderText(headerText);
        
        alert.setOnShowing(e -> {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        });
        
        alert.getDialogPane().setStyle(PRIMARY_DIALOG_STYLE);
    }
    
    // ==================== LABEL CREATION METHODS ====================
    
    /**
     * Create user info label for admin
     */
    public static Label createUserLabel(String action, String userType) {
        String userInfo = SwitchSceneUtil.currentUserEmail != null ? 
            SwitchSceneUtil.currentUserEmail : "user@library.com";
        
        Label userLabel = new Label(action + " as: " + userInfo + " (" + userType.toUpperCase() + ")");
        userLabel.setStyle("-fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        return userLabel;
    }
    
    /**
     * Create danger user label (for dangerous actions)
     */
    public static Label createDangerUserLabel(String action, String userType) {
        String userInfo = SwitchSceneUtil.currentUserEmail != null ? 
            SwitchSceneUtil.currentUserEmail : "user@library.com";
        
        Label userLabel = new Label(action + " as: " + userInfo + " (" + userType.toUpperCase() + ")");
        userLabel.setStyle("-fx-text-fill: " + DANGER_COLOR + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        return userLabel;
    }
    
    /**
     * Create table header with specified color
     */
    public static Label createTableHeader(String headerText, String color) {
        Label headerLabel = new Label(headerText);
        headerLabel.setStyle(MONOSPACE_STYLE + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return headerLabel;
    }
    
    /**
     * Create table separator line
     */
    public static Label createTableSeparator(String pattern) {
        Label separatorLabel = new Label(pattern);
        separatorLabel.setStyle(MONOSPACE_STYLE + " -fx-font-size: 12px; -fx-text-fill: " + GRAY_COLOR + ";");
        return separatorLabel;
    }
    
    // ==================== GRIDPANE CREATION METHODS ====================
    
    /**
     * Create standard dialog grid with common spacing
     */
    public static GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }
    
    /**
     * Create compact grid for smaller dialogs
     */
    public static GridPane createCompactGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }
    
    // ==================== COMMON TABLE HEADERS ====================
    
    /**
     * Create standard book table header
     */
    public static Label createBookTableHeader() {
        return createTableHeader("ID    | TITLE                           | AUTHOR                     | TOTAL | AVAILABLE", PRIMARY_COLOR);
    }
    
    /**
     * Create book table separator
     */
    public static Label createBookTableSeparator() {
        return createTableSeparator("─────┼─────────────────────────────────┼────────────────────────────┼───────┼──────────");
    }
    
    /**
     * Create borrow table header
     */
    public static Label createBorrowTableHeader() {
        return createTableHeader("TITLE                           | AUTHOR                     | BORROWED DATE | ACTION", WARNING_COLOR);
    }
    
    /**
     * Create borrow table separator
     */
    public static Label createBorrowTableSeparator() {
        return createTableSeparator("─────────────────────────────────┼────────────────────────────┼───────────────┼──────────────");
    }
    
    // ==================== STRING UTILITIES ====================
    
    /**
     * Truncate string if it exceeds max length
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Format string to exact length with padding or truncation
     */
    public static String formatToLength(String str, int length) {
        if (str == null) str = "";
        if (str.length() > length) {
            return truncateString(str, length);
        } else {
            return String.format("%-" + length + "s", str);
        }
    }
    
    // ==================== HOVER EFFECTS ====================
    
    /**
     * Apply hover effect to a label with background color
     */
    public static void applyHoverEffect(Label label, String normalStyle, String hoverColor) {
        label.setOnMouseEntered(e -> 
            label.setStyle(normalStyle + " -fx-background-color: " + hoverColor + "; -fx-cursor: hand;"));
        label.setOnMouseExited(e -> 
            label.setStyle(normalStyle + " -fx-cursor: hand;"));
    }
}