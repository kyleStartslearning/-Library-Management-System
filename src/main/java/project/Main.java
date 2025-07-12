package project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import project.Databases.Connect;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        System.out.println("Hello Try to commit");
        System.out.println("Testing 1");
        System.out.println("Testing 2");

        try {
            // Initialize database
            Connect dbConnection = Connect.getInstance();
            dbConnection.getConnection(); // This will initialize schema if needed
            
            // Load FXML and CSS
            Parent root = FXMLLoader.load(getClass().getResource("/project/FXML/LogIn.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/project/CSS/LogIn.css").toExternalForm());
            
            // Configure and show stage
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setTitle("Login Page");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            UtilityClass.ShowError("Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        // Close database connection when application stops
        Connect dbConnection = Connect.getInstance();
        dbConnection.closeConnection();
    }
}