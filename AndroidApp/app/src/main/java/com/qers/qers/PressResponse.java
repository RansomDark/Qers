package com.qers.qers;

import com.google.gson.annotations.SerializedName;

public class PressResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}
