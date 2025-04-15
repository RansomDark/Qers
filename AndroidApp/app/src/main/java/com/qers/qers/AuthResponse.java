package com.qers.qers;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("user_id")
    private int user_id;

    @SerializedName("is_pressed")
    private int is_pressed;

    @SerializedName("token")
    private String token;

    public int getId() {
        return user_id;
    }

    public String getToken() { return token; }

    public int checkPressed() { return is_pressed; }
}
