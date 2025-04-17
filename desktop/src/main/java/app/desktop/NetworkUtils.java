package app.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkUtils {
    private static final String SERVER_URL = "http://62.217.176.242:5001/";


    public static String sendRegistrationDetails(String login, String email, String password) {
        try {
            URL url = new URL(SERVER_URL + "register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"username\":\"" + login + "\","
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + password + "\""
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sendLoginDetails(String login, String password) {
        try {
            URL url = new URL(SERVER_URL + "login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"username\":\"" + login + "\","
                    + "\"password\":\"" + password + "\""
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStateButton(String login, String token) {
        Logger logger = Logger.getLogger(RegistrationForm.class.getName());
        try {
            String encodedLogin = URLEncoder.encode(login, "UTF-8");
            URL url = new URL(SERVER_URL + "is_pressed" + "?" + "username=" + encodedLogin);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            connection.setConnectTimeout(3500);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            logger.log(Level.INFO, "Код ответа сервера: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String response = in.readLine();
                    logger.log(Level.INFO, "Ответ сервера: " + response); // Проверяем ответ
                    return response;
                }
            } else {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String response = in.readLine();
                    logger.log(Level.WARNING, "Ошибка от сервера: " + response);
                    return response;
                }
            }

        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Сервер недоступен (нет интернета или неверный домен)");
            return "NETWORK_ERROR: Сервер недоступен";
        } catch (SocketTimeoutException e) {
            logger.log(Level.SEVERE, "Таймаут соединения с сервером");
            return "NETWORK_ERROR: Таймаут соединения";
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Неверный URL: " + SERVER_URL);
            return "INTERNAL_ERROR: Неверный URL";
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка ввода-вывода: " + e.getMessage());
            return "NETWORK_ERROR: Ошибка подключения";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Неизвестная ошибка: " + e.getMessage());
            return "INTERNAL_ERROR: " + e.getMessage();
        }
    }
}
