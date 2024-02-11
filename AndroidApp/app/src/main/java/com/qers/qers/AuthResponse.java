package com.qers.qers;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("is_pressed")
    private boolean isPressed;

    @SerializedName("message")
    private String message;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPressed() {
        return isPressed;
    }
}
