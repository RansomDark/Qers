package org.example.desktopapp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class FileUtils {
    private static final String CREDENTIALS_FILE = "credentials.properties";

    public static String[] loadCredentials() {
        String[] credentials = new String[2];
        try (InputStream input = new FileInputStream(CREDENTIALS_FILE)) {
            Properties properties = new Properties();
            properties.load(input);
            credentials[0] = properties.getProperty("username");
            credentials[1] = properties.getProperty("password");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return credentials;
    }

    public static void saveCredentials(String username, String password) {
        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE)) {
            Properties properties = new Properties();
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
