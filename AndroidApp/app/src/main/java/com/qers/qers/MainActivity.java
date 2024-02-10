package com.qers.qers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.PasswordAuthentication;

public class MainActivity extends AppCompatActivity {

    private Button login_button;
    private EditText login_field, password_field;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        login_button = findViewById(R.id.login_button);
        login_field = findViewById(R.id.login_field);
        password_field = findViewById(R.id.password_field);
        errorText = findViewById(R.id.errorText);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginValue = login_field.getText().toString();
                String passwordValue = password_field.getText().toString();

                if (!loginValue.isEmpty() || !passwordValue.isEmpty()) {
                    if ("Oleg".equals(loginValue) && "123".equals(passwordValue)) {
                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                        startActivity(intent);
                    } else {
                        errorText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}