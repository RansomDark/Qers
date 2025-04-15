package com.qers.qers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String API_URL = "http://62.217.176.242:5001/";

    private Button login_button;
    private EditText login_field, password_field;
    private TextView errorText;
    private Context context;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация UI элементов
        login_button = findViewById(R.id.login_button);
        login_field = findViewById(R.id.login_field);
        password_field = findViewById(R.id.password_field);
        errorText = findViewById(R.id.errorText);
        context = LoginActivity.this;

        // Проверка сохраненного пользователя
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        if (username != null) {
            logToFile("Пользователь уже авторизован: " + username);
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Создание экземпляра Retrofit для работы с API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        // Установка обработчика нажатия кнопки
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginValue = login_field.getText().toString();
                String passwordValue = password_field.getText().toString();

                if (!loginValue.isEmpty() && !passwordValue.isEmpty()) {
                    AuthRequest authRequest = new AuthRequest();
                    authRequest.setUsername(loginValue);
                    authRequest.setPassword(passwordValue);

                    logToFile("Попытка авторизации пользователя: " + loginValue);

                    // Вызов API для авторизации
                    Call<AuthResponse> call = authService.loginUser(authRequest);
                    call.enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            if (response.isSuccessful()) {
                                AuthResponse authResponse = response.body();

                                // Сохранение данных пользователя
                                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("user_id", authResponse.getId());
                                editor.putString("email", authResponse.getEmail());
                                editor.putBoolean("is_button_pressed", authResponse.isPressed());
                                editor.putString("username", authResponse.getUsername());
                                editor.apply();

                                logToFile("Успешная авторизация пользователя: " + authResponse.getUsername());

                                // Переход на второй экран
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                                finish();
                            } else {
                                logToFile("Ошибка авторизации: Неверный логин или пароль");
                                errorText.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            logToFile("Ошибка соединения: " + t.getMessage());
                            t.printStackTrace();
                            errorText.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    // Метод для логирования в файл
    private void logToFile(String message) {
        try {
            File logFile = new File(getFilesDir(), "app_log.txt");
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(System.currentTimeMillis() + ": " + message + "\n");
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Ошибка записи в лог-файл", e);
        }
    }
}
