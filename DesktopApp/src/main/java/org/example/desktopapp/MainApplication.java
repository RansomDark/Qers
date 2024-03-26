package org.example.desktopapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class MainApplication extends Application {

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
            System.err.println("System tray feature is not supported");
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

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                primaryStage.show();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}


