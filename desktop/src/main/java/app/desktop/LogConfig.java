package app.desktop;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogConfig {
    private static final Logger logger = Logger.getLogger(LogConfig.class.getName());

    static {
        try {
            String appDataPath = System.getenv("APPDATA");
            String logFilePath = Paths.get(appDataPath, "Qers", "logs", "app.log").toString();

            java.nio.file.Files.createDirectories(Paths.get(appDataPath, "Qers", "logs"));

            // Создаем обработчик для записи логов в файл
            FileHandler fileHandler = new FileHandler(logFilePath, true);  // Параметр "true" добавляет записи в конец файла
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);  // Устанавливаем уровень логирования, чтобы ловить все логи
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Произошла ошибка при настройке логирования", e);
        }
    }

    // Метод для получения общего логгера
    public static Logger getLogger() {
        return logger;
    }
}
