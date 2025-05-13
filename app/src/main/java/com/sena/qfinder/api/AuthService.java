package com.sena.qfinder.api;

import com.sena.qfinder.models.RegisterRequest;
import com.sena.qfinder.models.RegisterResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);
}