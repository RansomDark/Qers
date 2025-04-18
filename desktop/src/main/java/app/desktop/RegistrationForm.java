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


public class RegistrationForm extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static final Logger logger = LogConfig.getLogger();

    @Override
    public void start(Stage primaryStage) {

        String[] credentials = FileUtils.loadCredentials();

        if (credentials[0] != null && credentials[1] != null) {
            logger.log(Level.INFO, "Данные аутентификации найдены. Открываем главное окно ...");
            MainApplication mainApp = new MainApplication();  // Создаем экземпляр MainApplication
            Stage mainStage = new Stage();
            try {
                mainApp.start(mainStage);  // Запускаем главную программу
            } catch (AWTException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        primaryStage.setTitle("Qers");

        int fieldWidth = 400;
        int fieldHeight = 50;

        TextField loginField = new TextField();
        loginField.setPromptText("Введите логин");
        loginField.setMaxSize(fieldWidth, fieldHeight);
        loginField.getStyleClass().add("text-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Введите электронную почту");
        emailField.setMaxSize(fieldWidth, fieldHeight);
        emailField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Придумайте пароль");
        passwordField.setMaxSize(fieldWidth, fieldHeight);
        passwordField.getStyleClass().add("text-field");

        PasswordField password2Field = new PasswordField();
        password2Field.setPromptText("Повторите пароль");
        password2Field.setMaxSize(fieldWidth, fieldHeight);
        password2Field.getStyleClass().add("text-field");

        Button registerButton = new Button("Зарегистрироваться");

        Text loginErrorText = new Text("");
        loginErrorText.setFill(Color.RED);
        loginErrorText.setVisible(false);

        Text emailErrorText = new Text("Некорректный формат почты");
        emailErrorText.setFill(Color.RED);
        emailErrorText.setVisible(false);

        Text passwordErrorText = new Text("");
        passwordErrorText.setFill(Color.RED);
        passwordErrorText.setVisible(false);

        Text password2ErrorText = new Text("Пароли не совпадают");
        password2ErrorText.setFill(Color.RED);
        password2ErrorText.setVisible(false);

        Hyperlink loginLink = new Hyperlink("Войти");  // Создаем гиперссылку

        BorderPane layout = new BorderPane();
        layout.setCenter(PaneUtils.createRegisterPane(loginField, loginErrorText,
                                         emailField, emailErrorText,
                                         passwordField, passwordErrorText,
                                         password2Field, password2ErrorText));
        layout.setBottom(PaneUtils.createBottomPane(registerButton, loginLink));

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

        primaryStage.show();

        layout.setOnMouseReleased(event -> layout.requestFocus());

        loginLink.setOnAction(e -> {
            logger.log(Level.INFO, "Переход к форме авторизации");
            // Открыть форму авторизации

            primaryStage.close();  // Закрываем текущее окно

            // Создаем и отображаем окно авторизации
            LoginForm loginApp = new LoginForm();  // Создаем экземпляр LoginForm
            Stage loginStage = new Stage();
            loginApp.start(loginStage);  // Запускаем окно авторизации
        });

        registerButton.setOnAction(e -> {
            logger.log(Level.INFO, "Кнопка регистрации нажата");
            // Обработка нажатия кнопки регистрации

            String login = loginField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String password2 = password2Field.getText();

            if (!email.isEmpty() && !login.isEmpty() && !password.isEmpty() && !password2.isEmpty()) {
                if (!ValidationUtils.isValidEmail(email)) {
                    emailErrorText.setVisible(true);
                    emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);

                    return;
                }

                String passwordValidationResult = ValidationUtils.validatePassword(password);

                if (passwordValidationResult != null) {
                    passwordErrorText.setText(passwordValidationResult);
                    passwordErrorText.setVisible(true);
                    passwordField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);

                    return;
                }

                if (!password.equals(password2)) {
                    password2ErrorText.setVisible(true);
                    password2Field.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);

                    return;
                }

                String registrationResponse = NetworkUtils.sendRegistrationDetails(login, email, password);
                logger.log(Level.INFO, "Ответ от сервера: ", registrationResponse);

                if (registrationResponse != null) {
                    if (registrationResponse.contains("User registered successfully")) {
                        logger.log(Level.INFO, "Пользователь с логином: " + login
                                + ", адресом электронной почты: " + email + " успешно зарегистрирован");

                        primaryStage.close();  // Закрываем текущее окно

                        // Создаем и отображаем окно авторизации
                        LoginForm loginApp = new LoginForm();  // Создаем экземпляр LoginForm
                        Stage loginStage = new Stage();
                        loginApp.start(loginStage);  // Запускаем окно авторизации


                    } else if (registrationResponse.contains("Username already exists")) {
                        logger.log(Level.INFO, "Такой логин уже существует");

                        loginErrorText.setText("Такой логин уже существует");
                        loginErrorText.setVisible(true);
                        loginField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
                    } else if (registrationResponse.contains("Email already registered")) {
                        logger.log(Level.INFO, "Такая почта уже существует");

                        emailErrorText.setText("Такой адрес электронной почты уже используется");
                        emailErrorText.setVisible(true);
                        emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
                    } else {
                        logger.log(Level.WARNING, "Пользователь с логином: " + login
                                + ", адресом электронной почты: " + email + " Сервер выдал ошибку");

                    }
                } else {
                    // Обработка случая, когда ответ от сервера null или произошла ошибка
                    logger.log(Level.WARNING, "Ошибка обработки ответа от сервера");
                }
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 32) {
                emailField.setText(oldValue);
            }
            emailErrorText.setVisible(false);
            emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
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

        password2Field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 24) {
                passwordField.setText(oldValue);
            }
            password2ErrorText.setVisible(false);
            password2Field.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
        });

        registerButton.setOnMousePressed(e -> registerButton.setStyle("-fx-background-color: #5E35B1; " +
                "-fx-font-size: 16px; -fx-text-fill: #FFFFFF;"));
        registerButton.setOnMouseReleased(e -> registerButton.setStyle("-fx-background-color: #673AB7; " +
                "-fx-font-size: 16px; -fx-text-fill: #FFFFFF;"));
    }
}


