package com.qers.qers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String API_URL = "http://192.168.0.12:5001/";

    private ImageView imageView;
    private Button logoutButton;
    private int is_pressed;
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
        int user_id = sharedPreferences.getInt("user_id", -1);
        String token = sharedPreferences.getString("token", null);
        String username = sharedPreferences.getString("username", null);
        is_pressed = sharedPreferences.getInt("is_pressed", 0);
        Log.d(TAG, "is_pressed:" + is_pressed);


        if (is_pressed == 1) {
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

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Настройка Retrofit для API запросов с токеном
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        if (token != null) {
                            return chain.proceed(chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)  // Добавляем токен в заголовок
                                    .build());
                        } else {
                            return chain.proceed(chain.request());
                        }
                    }
                })
                .build();

        // Настройка Retrofit для API запросов
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Обработчик нажатия на imageView
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Текущее состояние кнопки: " + is_pressed);
                logToFile("Текущее состояние кнопки перед нажатием: " + is_pressed);

                // Переключение состояния кнопки
                if (is_pressed == 1) {
                    imageView.setImageResource(R.drawable.button_off);
                } else {
                    imageView.setImageResource(R.drawable.button_on);
                }
                is_pressed = (is_pressed + 1) % 2;

                // Сохранение нового состояния
                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("is_pressed", is_pressed);
                editor.apply();
                logToFile("Новое состояние кнопки: " + is_pressed);

                // Отправка запроса на сервер
                if (username != null) {
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
                            Toast.makeText(context, "Ошибка при соединении. Попробуйте снова.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Ошибка: username не может быть null");
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
