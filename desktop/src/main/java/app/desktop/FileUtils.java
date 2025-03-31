package app.desktop;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private static final String CREDENTIALS_FILE = "credentials.properties";
    private static final Logger logger = LogConfig.getLogger();

    public static String[] loadCredentials() {
        String[] credentials = new String[1];
        try (InputStream input = new FileInputStream(CREDENTIALS_FILE)) {
            Properties properties = new Properties();
            properties.load(input);
            credentials[0] = properties.getProperty("username");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке учетных данных", e);
        }
        return credentials;
    }

    public static void saveCredentials(String username) {
        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE)) {
            Properties properties = new Properties();
            properties.setProperty("username", username);
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
