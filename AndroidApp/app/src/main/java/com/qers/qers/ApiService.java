package com.qers.qers;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("press/")
    Call<PressResponse> pressButton(@Body PressRequest pressRequest);
}
