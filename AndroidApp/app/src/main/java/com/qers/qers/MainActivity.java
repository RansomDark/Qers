package com.qers.qers;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Button login_button;
    private EditText login_field, password_field;
    private TextView errorText;
    private Context context;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_button = findViewById(R.id.login_button);
        login_field = findViewById(R.id.login_field);
        password_field = findViewById(R.id.password_field);
        errorText = findViewById(R.id.errorText);

        context = MainActivity.this;

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            Intent intent = new Intent(context, SecondActivity.class);
            startActivity(intent);
            finish();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://5.35.95.143:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginValue = login_field.getText().toString();
                String passwordValue = password_field.getText().toString();

                if (!loginValue.isEmpty() && !passwordValue.isEmpty()) {
                    AuthRequest authRequest = new AuthRequest();
                    authRequest.setUsername(loginValue);
                    authRequest.setPassword(passwordValue);

                    // Вызов метода API для авторизации
                    Call<AuthResponse> call = authService.loginUser(authRequest);

                    call.enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            if (response.isSuccessful()) {
                                AuthResponse authResponse = response.body();

                                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("user_id", authResponse.getId());
                                editor.putString("email", authResponse.getEmail());
                                editor.putBoolean("is_button_pressed", authResponse.isPressed());
                                editor.putString("username", authResponse.getUsername());
                                editor.apply();

                                Intent intent = new Intent(context, SecondActivity.class);
                                context.startActivity(intent);
                                finish();
                            } else {
                                errorText.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            t.printStackTrace();
                            errorText.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }
}
