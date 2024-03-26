module org.example.desktopapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;


    opens org.example.desktopapp to javafx.fxml;
    exports org.example.desktopapp;
}