package project.Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertMsg {
    
    /**
     * Show error alert dialog
     * @param title Alert title (can be null)
     * @param message Error message to display
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert dialog
     * @param title Alert title
     * @param message Warning message to display
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show information alert dialog
     * @param title Alert title
     * @param message Information message to display
     */
    public static void showInformation(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
