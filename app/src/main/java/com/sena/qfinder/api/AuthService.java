package com.sena.qfinder.api;

import com.sena.qfinder.models.CodeVerificationRequest;
import com.sena.qfinder.models.CodeVerificationResponse;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteRequest;
import com.sena.qfinder.models.PacienteResponse;
import com.sena.qfinder.models.RegisterPacienteRequest;
import com.sena.qfinder.models.RegisterPacienteResponse;
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
import retrofit2.http.PUT;
import retrofit2.http.Path;

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
    @POST("api/paciente/register")
    Call<RegisterPacienteResponse> registerPaciente(
            @Header("Authorization") String token,
            @Body RegisterPacienteRequest request
    );
    // En tu AuthService, añade este método
    @GET("api/paciente/listarPacientes/{id_paciente}")
    Call<PacienteResponse> obtenerPacientePorId(
            @Header("Authorization") String token,
            @Path("id_paciente") int pacienteId
    );
    @GET("api/paciente/listarPacientes")
    Call<PacienteListResponse> listarPacientes(@Header("Authorization") String token);
    @POST("api/auth/logout")
    Call<Void> logout();

    @PUT("api/paciente/actualizarPaciente/{id_paciente}")
    Call<Void> actualizarPaciente(
            @Header("Authorization") String token,
            @Path("id_paciente") int pacienteId,
            @Body PacienteRequest pacienteRequest
    );


}
