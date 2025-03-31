package app.desktop;

import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LoginForm extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static final Logger logger = LogConfig.getLogger();

    @Override
    public void start(Stage primaryStage) {

        String[] credentials = FileUtils.loadCredentials();

        primaryStage.setTitle("Qers");

        int fieldWidth = 400;
        int fieldHeight = 50;

        TextField loginField = new TextField();
        loginField.setPromptText("Введите логин");
        loginField.setMaxSize(fieldWidth, fieldHeight);
        loginField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setMaxSize(fieldWidth, fieldHeight);
        passwordField.getStyleClass().add("text-field");

        Button loginButton = new Button("Войти");

        Text loginErrorText = new Text("");
        loginErrorText.setFill(Color.RED);
        loginErrorText.setVisible(false);

        Text passwordErrorText = new Text("");
        passwordErrorText.setFill(Color.RED);
        passwordErrorText.setVisible(false);

        Hyperlink registerLink = new Hyperlink("Зарегистрироваться");  // Создаем гиперссылку

        BorderPane layout = new BorderPane();
        layout.setCenter(PaneUtils.createLoginPane(loginField, loginErrorText,
                passwordField, passwordErrorText));
        layout.setBottom(PaneUtils.createBottomPane(loginButton, registerLink));

        Scene scene = new Scene(layout, 800, 600);
        scene.getRoot().requestFocus();

        String cssPath = "/app/desktop/styles.css";
        if (getClass().getResource(cssPath) != null) {
            logger.log(Level.INFO, "Css found");
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } else {
            // Обработка случая, когда ресурс не найден
            logger.log(Level.WARNING, "Css not found");
        }

        // Создание иконки
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        primaryStage.setScene(scene);
        primaryStage.requestFocus();
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(false);

        layout.setOnMouseReleased(event -> layout.requestFocus());

        registerLink.setOnAction(e -> {
            logger.log(Level.INFO, "Переход к форме регистрации");
            // Открыть форму регистрации

            primaryStage.close();  // Закрываем окно авторизации

            // Создаем и отображаем окно регистрации
            RegistrationForm registerApp = new RegistrationForm();  // Создаем экземпляр RegisterForm
            Stage registerStage = new Stage();
            registerApp.start(registerStage);  // Запускаем окно регистрации
        });

        loginButton.setOnAction(e -> {
            logger.log(Level.INFO, "Кнопка авторизации нажата");
            // Обработка нажатия кнопки регистрации

            String login = loginField.getText();
            String password = passwordField.getText();

            if (!login.isEmpty() && !password.isEmpty()) {

                String loginResponse = NetworkUtils.sendLoginDetails(login, password);
                logger.log(Level.INFO, loginResponse);

                if (loginResponse != null) {
                    if (loginResponse.contains("id")) {
                        logger.log(Level.INFO, "Пользователь с логином: " + login
                                + "успешно авторизован");

                        // Сохраняем учетные данные пользователя
                        FileUtils.saveCredentials(login);

                        primaryStage.close();  // Закрываем окно авторизации

                        // Создаем и отображаем главное окно
                        MainApplication mainApp = new MainApplication();  // Создаем экземпляр MainApplication
                        Stage mainStage = new Stage();
                        try {
                            mainApp.start(mainStage);  // Запускаем главную программу
                        } catch (AWTException ex) {
                            throw new RuntimeException(ex);
                        }

                    } else if (loginResponse.contains("Invalid username or password")) {
                        logger.log(Level.INFO, "Неправильный логин или пароль");

                        loginErrorText.setText("Неправильный логин или пароль");
                        loginErrorText.setVisible(true);
                        loginField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
                    } else {
                        logger.log(Level.WARNING, "Пользователь с логином: " + login
                                + "Сервер выдал ошибку");
                    }
                } else {
                    // Обработка случая, когда ответ от сервера null или произошла ошибка
                    logger.log(Level.WARNING, "Ошибка обработки ответа от сервера");
                }
            }
        });

        loginField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 24) {
                loginField.setText(oldValue);
            }
            loginErrorText.setVisible(false);
            loginField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 24) {
                passwordField.setText(oldValue);
            }
            passwordErrorText.setVisible(false);
            passwordField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        });

        loginButton.setOnMousePressed(e -> loginButton.setStyle("-fx-background-color: #5E35B1; " +
                "-fx-font-size: 16px; -fx-text-fill: #FFFFFF;"));
        loginButton.setOnMouseReleased(e -> loginButton.setStyle("-fx-background-color: #673AB7; " +
                "-fx-font-size: 16px; -fx-text-fill: #FFFFFF;"));

        if (credentials[0] != null) {
            // Данные аутентификации найдены, выполняем автоматическую аутентификацию
            logger.log(Level.INFO, "Данные аунтецикации найдены");

            // Закрываем текущее окно
            primaryStage.close();  // Закрываем окно авторизации

            // Создаем и отображаем главное окно
            MainApplication mainApp = new MainApplication();  // Создаем экземпляр MainApplication
            Stage mainStage = new Stage();
            try {
                mainApp.start(mainStage);  // Запускаем главную программу
            } catch (AWTException ex) {
                throw new RuntimeException(ex);
            }

        } else {
            // Данные аутентификации отсутствуют, пользователь должен авторизаваться
            logger.log(Level.INFO, "Данные аунтецикации не найдены");

            primaryStage.show();
        }
    }
}


