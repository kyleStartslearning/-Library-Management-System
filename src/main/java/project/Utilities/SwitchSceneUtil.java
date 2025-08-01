package project.Utilities;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SwitchSceneUtil {

    public static String currentUserEmail;
    public static String currentUserType; // 'admin' or 'member'
    public static String currentUserName;

    /**
     * Switch scenes in JavaFX application
     * @param event ActionEvent from button/control
     * @param fxmlFile FXML file name (e.g., "LogIn.fxml")
     * @param cssFile CSS file name (e.g., "LogIn.css") - can be null
     * @throws IOException if files cannot be loaded
     */
    public static void switchScene(ActionEvent event, String fxmlFile, String cssFile) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(SwitchSceneUtil.class.getResource("/project/FXML/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            if (cssFile != null) {
                scene.getStylesheets().add(SwitchSceneUtil.class.getResource("/project/CSS/" + cssFile).toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            AlertMsg.showError("Navigation Error", "Failed to load the requested screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

