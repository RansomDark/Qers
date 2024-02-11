package com.qers.qers;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("login")
    Call<AuthResponse> loginUser(@Body AuthRequest authRequest);
}