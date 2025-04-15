package app.desktop;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private static final Logger logger = LogConfig.getLogger();

    private static final String APP_DATA_DIR = System.getenv("APPDATA");
    private static final String CREDENTIALS_FILE  = Paths.get(APP_DATA_DIR, "Qers", "credentials.properties").toString();

    static {
        try {
            // Создаем директорию, если она не существует
            java.nio.file.Files.createDirectories(Paths.get(APP_DATA_DIR, "Qers"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при создании директории конфигурации", e);
        }
    }


    public static String[] loadCredentials() {
        String[] credentials = new String[2];
        try (InputStream input = new FileInputStream(CREDENTIALS_FILE )) {
            Properties properties = new Properties();
            properties.load(input);
            credentials[0] = properties.getProperty("username");
            credentials[1] = properties.getProperty("token");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке учетных данных", e);
        }
        return credentials;
    }

    public static void saveCredentials(String username, String token) {
        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE)) {
            Properties properties = new Properties();
            properties.setProperty("username", username);
            properties.setProperty("token", token);
            properties.store(output, null);
            logger.log(Level.INFO, "Учетные данные для пользователя {0} успешно сохранены.", username);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке учетных данных", e);
        }
    }

    public static void removeCredentials() {
        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE)) {
            // Записываем пустые данные в файл, тем самым очищая его
            output.write(new byte[0]);
            logger.log(Level.INFO, "Учетные данные были удалены из файла.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении учетных данных", e);
        }
    }
}
