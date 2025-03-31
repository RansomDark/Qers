package com.qers.qers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";
    private static final String API_URL = "http://192.168.0.10:5001/";

    private ImageView imageView;
    private Button logoutButton;
    private boolean is_pressed;
    private Context context;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        // Инициализация UI элементов
        imageView = findViewById(R.id.switches);
        imageView.setImageResource(R.drawable.button_off);
        logoutButton = findViewById(R.id.logout_button);
        context = MainActivity.this;

        // Получение данных пользователя из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        is_pressed = sharedPreferences.getBoolean("is_button_pressed", false);

        if (is_pressed) {
            imageView.setImageResource(R.drawable.button_on);
        } else {
            imageView.setImageResource(R.drawable.button_off);
        }

        // Логирование состояния кнопки
        logToFile("Кнопка включена: " + is_pressed);

        // Настройка кнопки выхода
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                logToFile("Пользователь вышел из системы");

                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Настройка Retrofit для API запросов
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Обработчик нажатия на изображение
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Текущее состояние кнопки: " + is_pressed);
                logToFile("Текущее состояние кнопки перед нажатием: " + is_pressed);

                // Переключение состояния кнопки
                if (is_pressed) {
                    imageView.setImageResource(R.drawable.button_off);
                } else {
                    imageView.setImageResource(R.drawable.button_on);
                }
                is_pressed = !is_pressed;

                // Сохранение нового состояния
                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_button_pressed", is_pressed);
                editor.apply();
                logToFile("Новое состояние кнопки: " + is_pressed);

                // Отправка запроса на сервер
                PressRequest pressRequest = new PressRequest();
                pressRequest.setUsername(username);
                Call<PressResponse> call = apiService.pressButton(pressRequest);

                call.enqueue(new Callback<PressResponse>() {
                    @Override
                    public void onResponse(Call<PressResponse> call, Response<PressResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Успешный запрос: кнопка нажата");
                            logToFile("Успешный запрос: кнопка нажата пользователем " + username);
                        }
                    }

                    @Override
                    public void onFailure(Call<PressResponse> call, Throwable t) {
                        Log.e(TAG, "Ошибка запроса", t);
                        logToFile("Ошибка запроса: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
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