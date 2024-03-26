package org.example.desktopapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PaneUtils {
    public static VBox createCenterPane(TextField loginField, Text loginErrorText,
                                  TextField emailField, Text emailErrorText,
                                  PasswordField passwordField, Text passwordErrorText,
                                  PasswordField password2Field, Text password2ErrorText ) {
        VBox centerPane = new VBox(10);
        centerPane.setPadding(new Insets(10));
        centerPane.setAlignment(Pos.CENTER);

        centerPane.getStyleClass().add("form-container");

        centerPane.getChildren().addAll(loginField, loginErrorText,
                emailField, emailErrorText,
                passwordField, passwordErrorText,
                password2Field, password2ErrorText);

        return centerPane;
    }

    public static BorderPane createBottomPane(Button registerButton) {
        BorderPane bottomPane = new BorderPane();
        bottomPane.setPadding(new Insets(10));

        BorderPane.setMargin(registerButton, new Insets(10));

        registerButton.setMinWidth(300);
        registerButton.setMinHeight(60);
        registerButton.setStyle("-fx-background-color: #673AB7; -fx-font-size: 16px; -fx-text-fill: #FFFFFF;");

        bottomPane.setCenter(registerButton);

        return bottomPane;
    }
}
