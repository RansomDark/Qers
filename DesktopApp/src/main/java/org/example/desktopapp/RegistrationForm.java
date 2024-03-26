package org.example.desktopapp;

import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.logging.*;
import java.io.*;


public class RegistrationForm extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Logger logger = Logger.getLogger(RegistrationForm.class.getName());

        String[] credentials = FileUtils.loadCredentials();

        primaryStage.setTitle("Регистрация пользователя");

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

        BorderPane layout = new BorderPane();
        layout.setCenter(PaneUtils.createCenterPane(loginField, loginErrorText,
                                         emailField, emailErrorText,
                                         passwordField, passwordErrorText,
                                         password2Field, password2ErrorText));
        layout.setBottom(PaneUtils.createBottomPane(registerButton));

        Scene scene = new Scene(layout, 800, 600);
        scene.getRoot().requestFocus();

        String cssPath = "/org/example/desktopapp/styles.css";
        if (getClass().getResource(cssPath) != null) {
            logger.log(Level.INFO, "Css found");
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } else {
            // Обработка случая, когда ресурс не найден
            logger.log(Level.WARNING, "Css not found");
        }

        primaryStage.setScene(scene);
        primaryStage.requestFocus();
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(false);

        layout.setOnMouseReleased(event -> layout.requestFocus());

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

                if (registrationResponse != null) {
                    if (registrationResponse.contains("User created successfully")) {
                        logger.log(Level.INFO, "Пользователь с логином: " + login
                                + ", адресом электронной почты: " + email + " успешно зарегистрирован");

                        // Сохраняем учетные данные пользователя
                        FileUtils.saveCredentials(login, password);

                        registerButton.getScene().getWindow().hide();
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/org/example/desktopapp/hello-view.fxml"));

                        try {
                            loader.load();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        Parent root = loader.getRoot();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.showAndWait();

                    } else if (registrationResponse.contains("There is already such a login")) {
                        logger.log(Level.INFO, "Такой логин уже существует");

                        loginErrorText.setText("Такой логин уже существует");
                        loginErrorText.setVisible(true);
                        loginField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
                    } else if (registrationResponse.contains("There is already such an email")) {
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

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
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

        if (credentials[0] != null && credentials[1] != null) {
            // Данные аутентификации найдены, выполняем автоматическую аутентификацию
            logger.log(Level.INFO, "Данные аунтецикации найдены");

            primaryStage.close();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/desktopapp/hello-view.fxml"));

            try {
                loader.load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();  // Открыть новое окно
        } else {
            // Данные аутентификации отсутствуют, пользователь должен зарегестрироваться
            logger.log(Level.INFO, "Данные аунтецикации не найдены");

            primaryStage.show();
        }
    }


}


