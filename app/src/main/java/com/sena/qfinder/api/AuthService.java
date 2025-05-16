package com.sena.qfinder.api;

import com.sena.qfinder.models.CodeVerificationRequest;
import com.sena.qfinder.models.CodeVerificationResponse;
import com.sena.qfinder.models.LoginRequest;
import com.sena.qfinder.models.LoginResponse;
import com.sena.qfinder.models.PerfilUsuarioResponse;
import com.sena.qfinder.models.SendCodeRequest;
import com.sena.qfinder.models.SendCodeResponse;
import com.sena.qfinder.models.RegisterRequest;
import com.sena.qfinder.models.RegisterResponse;
import com.sena.qfinder.models.VerificarCodigoRequest;
import com.sena.qfinder.models.CambiarPasswordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("api/auth/verify")
    Call<CodeVerificationResponse> verificarCodigo(@Body CodeVerificationRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> LoginUser(@Body LoginRequest request);
    @POST("api/auth/recuperar")
    Call<SendCodeResponse> SendCode(@Body SendCodeRequest request);
    @POST("api/auth/verificar-codigo")
    Call<Void> verificarCodigo(@Body VerificarCodigoRequest request);

    @POST("api/auth/cambiar-password") // o "/api/auth/change-password" según tu backend
    Call<Void> cambiarPassword(
            @Header("Authorization") String authToken,
            @Body CambiarPasswordRequest request
    );
    @GET("api/auth/perfil")
    Call<PerfilUsuarioResponse> obtenerPerfil(@Header("Authorization") String token);
}
