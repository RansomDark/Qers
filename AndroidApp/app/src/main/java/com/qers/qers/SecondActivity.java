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
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SecondActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button logoutButton;
    private boolean is_pressed;

    private Context context;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        imageView = findViewById(R.id.switches);
        imageView.setImageResource(R.drawable.button_off);

        logoutButton = findViewById(R.id.logout_button);

        context = SecondActivity.this;

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        is_pressed = sharedPreferences.getBoolean("is_button_pressed", false);

        if (is_pressed) {
            imageView.setImageResource(R.drawable.button_off);
        } else {
            imageView.setImageResource(R.drawable.button_on);
        }
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://5.35.95.143:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("is_pressed = ", Boolean.toString(is_pressed));
                if (is_pressed) {
                    imageView.setImageResource(R.drawable.button_on);
                } else {
                    imageView.setImageResource(R.drawable.button_off);
                }

                is_pressed = !is_pressed;
                SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_button_pressed", is_pressed);
                editor.apply();

                PressRequest pressRequest = new PressRequest();
                pressRequest.setUsername(username);

                Call<PressResponse> call = apiService.pressButton(pressRequest);

                call.enqueue(new Callback<PressResponse>() {
                    @Override
                    public void onResponse(Call<PressResponse> call, Response<PressResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d("Good request", "Everything is OK");
                        }
                    }

                    @Override
                    public void onFailure(Call<PressResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

            }
        });
    }
}
