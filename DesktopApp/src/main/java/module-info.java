module org.example.desktopapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens org.example.desktopapp to javafx.fxml;
    exports org.example.desktopapp;
}