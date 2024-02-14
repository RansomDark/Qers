package org.example.desktopapp;

import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.*;

public class RegistrationForm extends Application {
    private static final String SERVER_URL = "http://5.35.96.143:5000/create_user";
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

            Logger logger = Logger.getLogger(RegistrationForm.class.getName());

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

        Text loginErrorText = new Text("Некорректный формат почты");
        loginErrorText.setFill(Color.RED);
        loginErrorText.setVisible(false);

        Text emailErrorText = new Text("Некорректный формат почты");
        emailErrorText.setFill(Color.RED);
        emailErrorText.setVisible(false);

        Text passwordErrorText = new Text("Некорректный формат почты");
        passwordErrorText.setFill(Color.RED);
        passwordErrorText.setVisible(false);

        Text password2ErrorText = new Text("Некорректный формат почты");
        password2ErrorText.setFill(Color.RED);
        password2ErrorText.setVisible(false);

        BorderPane layout = new BorderPane();
        layout.setCenter(createCenterPane(loginField, loginErrorText,
                                         emailField, emailErrorText,
                                         passwordField, passwordErrorText,
                                         password2Field, password2ErrorText));
        layout.setBottom(createBottomPane(registerButton));

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

        layout.setOnMouseClicked(event -> layout.requestFocus());

        registerButton.setOnAction(e -> {
            logger.log(Level.INFO, "Кнопка регистрации нажата");
            // Обработка нажатия кнопки регистрации
            String login = loginField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String password2 = password2Field.getText();

            if (!email.isEmpty() && !login.isEmpty() && !password.isEmpty() && !password2.isEmpty())
                if (!isValidEmail(email)) {
                    emailErrorText.setVisible(true);
                    emailField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), true);
                } else {
                    boolean registrationSuccessful = sendRegistrationDetails(login, email, password);

                    if (registrationSuccessful) {
                        logger.log(Level.INFO, "User with login: " + login
                                                    + ", email: " + email + "registered successfully");
                    } else {
                        logger.log(Level.WARNING, "User with login: " + login
                                + ", email: " + email + "registered invalid");
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
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 24) {
                passwordField.setText(oldValue);
            }
            passwordErrorText.setVisible(false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 24) {
                passwordField.setText(oldValue);
            }
            password2ErrorText.setVisible(false);
        });

        registerButton.setOnMousePressed(e -> registerButton.setStyle("-fx-background-color: #5E35B1; " +
                "-fx-font-size: 16px; -fx-text-fill: #FFFFFF;"));
        registerButton.setOnMouseReleased(e -> registerButton.setStyle("-fx-background-color: #673AB7; " +
                "-fx-font-size: 16px; -fx-text-fill: #FFFFFF;"));


        primaryStage.show();


    }

    private VBox createCenterPane(TextField loginField, Text loginErrorText,
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


    private BorderPane createBottomPane(Button registerButton) {
        BorderPane bottomPane = new BorderPane();
        bottomPane.setPadding(new Insets(10));

        BorderPane.setMargin(registerButton, new Insets(10));

        registerButton.setMinWidth(300);
        registerButton.setMinHeight(60);
        registerButton.setStyle("-fx-background-color: #673AB7; -fx-font-size: 16px; -fx-text-fill: #FFFFFF;");

        bottomPane.setCenter(registerButton);

        return bottomPane;
    }

    private boolean sendRegistrationDetails(String login, String email, String password) {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"login\":\"" + login + "\","
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + password + "\""
                    + "}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                return response.toString().equals("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }
}


