package com.sena.qfinder.api;

import com.sena.qfinder.models.CodeVerificationRequest;
import com.sena.qfinder.models.CodeVerificationResponse;
import com.sena.qfinder.models.LoginRequest;
import com.sena.qfinder.models.LoginResponse;
import com.sena.qfinder.models.RegisterRequest;
import com.sena.qfinder.models.RegisterResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);
    @POST("api/auth/verify")
    Call<CodeVerificationResponse> verificarCodigo(@Body CodeVerificationRequest request);
    @POST("api/auth/login")
    Call<LoginResponse> LoginUser(@Body LoginRequest request);

}