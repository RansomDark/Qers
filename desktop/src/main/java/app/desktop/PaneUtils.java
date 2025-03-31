package app.desktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PaneUtils {
    public static VBox createRegisterPane(TextField loginField, Text loginErrorText,
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

    public static VBox createLoginPane(TextField loginField, Text loginErrorText,
                                          PasswordField passwordField, Text passwordErrorText ) {
        VBox centerPane = new VBox(10);
        centerPane.setPadding(new Insets(10));
        centerPane.setAlignment(Pos.CENTER);

        centerPane.getStyleClass().add("form-container");

        centerPane.getChildren().addAll(loginField, loginErrorText,
                passwordField, passwordErrorText);

        return centerPane;
    }

    public static BorderPane createBottomPane(Button button, Hyperlink hyperlink) {
        BorderPane bottomPane = new BorderPane();
        bottomPane.setPadding(new Insets(10));

        BorderPane.setMargin(button, new Insets(10));

        button.setMinWidth(300);
        button.setMinHeight(60);

        // Применение стилей
        button.getStyleClass().add("button");
        hyperlink.getStyleClass().add("hyperlink");

        // Создание VBox для кнопки и гиперссылки
        VBox vbox = new VBox(10);  // Отступы между элементами
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(button, hyperlink);

        // Устанавливаем VBox в нижнюю часть BorderPane
        bottomPane.setCenter(vbox);

        return bottomPane;
    }
}
