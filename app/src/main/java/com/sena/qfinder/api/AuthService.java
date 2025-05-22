package com.sena.qfinder.api;

import com.sena.qfinder.models.ActividadGetResponse;
import com.sena.qfinder.models.ActividadListResponse;
import com.sena.qfinder.models.ActividadRequest;
import com.sena.qfinder.models.ActividadResponse;
import com.sena.qfinder.models.CitaMedica;
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
import com.sena.qfinder.models.UsuarioRequest;
import com.sena.qfinder.models.VerificarCodigoRequest;
import com.sena.qfinder.models.CambiarPasswordRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @POST("api/auth/cambiar-password")
        // o "/api/auth/change-password" según tu backend
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


    // En tu AuthService.java
    @POST("api/actividades/crearActivdad/{id_paciente}")
    Call<ActividadResponse> crearActividad(
            @Header("Authorization") String token,
            @Path("id_paciente") int idPaciente,
            @Body ActividadRequest request
    );


    @PUT("api/auth/actualizarUser")
    Call<ResponseBody> actualizarUsuario(@Body UsuarioRequest usuario, @Header("Authorization") String token);

    @GET("api/actividades/listarActividades/{id_paciente}")
    Call<ActividadListResponse> listarActividades(
            @Header("Authorization") String token,
            @Path("id_paciente") int pacienteId
    );


    @POST("crearCita/{id_paciente}")
    Call<CitaMedica> crearCitaMedica(
            @Header("Authorization") String token,
            @Path("id_paciente") int idPaciente,
            @Body CitaMedica cita
    );

    @GET("listarCitas/{id_paciente}")
    Call<List<CitaMedica>> listarCitasMedicas(
            @Header("Authorization") String token,
            @Path("id_paciente") int idPaciente
    );

    @GET("listarCitasId/{id_paciente}/{id_cita}")
    Call<List<CitaMedica>> obtenerCitaPorId(
            @Header("Authorization") String token,
            @Path("id_paciente") int idPaciente,
            @Path("id_cita") int idCita
    );

    @PUT("actualizarCita/{id_paciente}/{id_cita}")
    Call<Void> actualizarCita(
            @Header("Authorization") String token,
            @Path("id_paciente") int idPaciente,
            @Path("id_cita") int idCita,
            @Body CitaMedica cita
    );

    @DELETE("eliminarCita/{id_paciente}/{id_cita}")
    Call<Void> eliminarCita(
            @Header("Authorization") String token,
            @Path("id_paciente") int idPaciente,
            @Path("id_cita") int idCita
    );

}


