package app.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {
    private static final Logger logger = LogConfig.getLogger();
    private String username;
    private String token;
    private Stage primaryStage;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private static final String iconImageLoc = "/icon.png";

    private volatile boolean monitoringActive = true;

    @Override
    public void start(Stage primaryStage) throws AWTException {
        this.primaryStage = primaryStage;
        String[] credentials = FileUtils.loadCredentials();

        if (credentials[0] == null && credentials[1] == null) {
            logger.log(Level.INFO, "Данные аутентификации не найдены. Открываем окно регистрации...");
            RegistrationForm registrationForm = new RegistrationForm();
            registrationForm.start(new Stage());
            return;
        }

        username = credentials[0];
        token = credentials[1];


        Button logoutButton = new Button("Выйти");
        logoutButton.getStyleClass().add("button");
        StackPane layout = new StackPane();
        layout.getChildren().add(logoutButton);
        Scene scene = new Scene(layout, 300, 300);

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
        primaryStage.setTitle("Qers");
        primaryStage.setResizable(false);

        Platform.setImplicitExit(false);

        Platform.runLater(this::createTrayIcon);

        primaryStage.show();

        logoutButton.setOnAction(e -> handleLogout());

        startMonitoring(); // Запуск фонового потока мониторинга
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            logger.log(Level.WARNING, "Системный трей не поддерживается.");
            return;
        }

        try {
            java.awt.Toolkit.getDefaultToolkit();

            tray = java.awt.SystemTray.getSystemTray();
            URL imageURL = getClass().getResource(iconImageLoc);
            if (imageURL == null) {
                logger.log(Level.SEVERE, "Иконка для трея не найдена.");
                return;
            }
            java.awt.Image image = ImageIO.read(imageURL);

            trayIcon = new TrayIcon(image);

            PopupMenu menu = new PopupMenu();
            MenuItem closeItem = new MenuItem("Закрыть");

            menu.add(closeItem);

            trayIcon.setPopupMenu(menu);
            tray.add(trayIcon);

            logger.log(Level.INFO, "Иконка добавлена в трей.");

            trayIcon.addActionListener(event -> Platform.runLater(() -> {
                if (primaryStage != null) {
                    primaryStage.show();
                    primaryStage.toFront();
                }
            }));

            closeItem.addActionListener(e -> {
                logger.log(Level.INFO, "Закрытие приложения.");
                tray.remove(trayIcon);
                Platform.exit();
                System.exit(0);
            });

        } catch (AWTException e) {
            logger.log(Level.SEVERE, "Ошибка при создании иконки трея", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLogout() {
        stopMonitoring();
        FileUtils.removeCredentials();
        tray.remove(trayIcon);
        logger.log(Level.INFO, "Трей-иконка удалена.");
        primaryStage.close();

        Platform.runLater(() -> {
            LoginForm loginForm = new LoginForm();
            Stage loginStage = new Stage();
            try {
                loginForm.start(loginStage);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Ошибка при открытии окна входа", ex);
            }
        });
    }


    private void startMonitoring() {
        monitoringActive = true;
        Thread thread = new Thread(() -> {
            while (monitoringActive) {
                String status = NetworkUtils.getStateButton(username, token);
                if (status.contains("true")) {
                    FileUtils.saveCredentials(username, token, 1);
                    shutdownComputer();
                    logger.log(Level.INFO, "Компьютер выключен");
                } else if (status.contains("false")) {
                    FileUtils.saveCredentials(username, token, 0);
                    logger.log(Level.INFO, "Компьютер включен");
                } else {
                    String[] credentials = FileUtils.loadCredentials();
                    if (credentials[2].contains("1")) {
                        shutdownComputer();
                        logger.log(Level.INFO, "Компьютер выключен по данным из файла");
                    } else {
                        logger.log(Level.INFO, "Компьютер включен по данным из файла");
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Ошибка в потоке мониторинга", e);
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void stopMonitoring() {
        monitoringActive = false; // Останавливаем мониторинг
    }

    private void shutdownComputer() {
        try {
            logger.log(Level.INFO, "Выключение компьютера...");
            Process process = Runtime.getRuntime().exec("rundll32.exe user32.dll,LockWorkStation");
            process.waitFor();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при выключении компьютера", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
