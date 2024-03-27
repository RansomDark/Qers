package org.example.desktopapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {
    Logger logger = Logger.getLogger(MainApplication.class.getName());

    String username = FileUtils.loadCredentials()[0];

    @Override
    public void start(Stage primaryStage) throws AWTException {
        Button button = new Button("Нажми меня");

        StackPane layout = new StackPane();
        layout.getChildren().add(button);

        Scene scene = new Scene(layout, 300, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Второе окно");
        primaryStage.setResizable(false);

        // Создаем иконку для трея
        if (!SystemTray.isSupported()) {
            logger.log(Level.WARNING, "Tray not supported");
        }

        SystemTray tray = SystemTray.getSystemTray();
        URL imageURL = getClass().getResource("/test.png");

        Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
        TrayIcon trayIcon = new TrayIcon(image, "Qers", null);
        trayIcon.setImageAutoSize(true);

        PopupMenu rootMenu = new PopupMenu();

        MenuItem open = new MenuItem("Открыть");
        rootMenu.add(open);
        MenuItem close = new MenuItem("Закрыть");
        rootMenu.add(close);

        trayIcon.setPopupMenu(rootMenu);

        tray.add(trayIcon);

        primaryStage.show();

        close.addActionListener(e -> primaryStage.hide());

        button.setOnAction(e -> {
            String stateButton = NetworkUtils.getStateButton(username);
            logger.log(Level.INFO, "stateButton: " + stateButton);

            if (stateButton.contains("User is pressed")) {
                logger.log(Level.INFO, "Компьютер выключен");
            } else {
                logger.log(Level.INFO, "Компьютер не выключен");
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (NetworkUtils.getStateButton(username).contains("User is pressed")) {
                        logger.log(Level.INFO, "Компьютер выключен");
                    } else {
                        logger.log(Level.INFO, "Компьютер не выключен");
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }

    private void shutdownComputer() {
        try {
            // Выполняем системный вызов для выключения компьютера
            Process process = Runtime.getRuntime().exec("shutdown -s -t 0");
            process.waitFor(); // Ждем завершения процесса
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}



