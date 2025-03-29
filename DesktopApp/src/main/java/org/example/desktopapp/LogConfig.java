package org.example.desktopapp;

import java.io.IOException;
import java.util.logging.*;

public class LogConfig {
    private static final Logger logger = Logger.getLogger(LogConfig.class.getName());

    static {
        try {
            // Создаем обработчик для записи логов в файл
            FileHandler fileHandler = new FileHandler("app.log", true);  // Параметр "true" добавляет записи в конец файла
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);  // Устанавливаем уровень логирования, чтобы ловить все логи
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Произошла ошибка", e);
        }
    }

    // Метод для получения общего логгера
    public static Logger getLogger() {
        return logger;
    }
}
