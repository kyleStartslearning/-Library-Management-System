module project { 
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;

    opens project to javafx.fxml;
    exports project;
    opens project.controllers to javafx.fxml;
    exports project.controllers;
}
