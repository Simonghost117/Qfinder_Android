package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PerfilUsuarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class perfil_usuario extends Fragment {

    private TextView tvNombre, tvApellido, tvTelefono, tvCorreo, tvDireccion, tvIdentificacion;
    private AuthService authService;
    private Call<List<PerfilUsuarioResponse>> perfilCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar TextViews
        tvNombre = view.findViewById(R.id.tvNombre);
        tvApellido = view.findViewById(R.id.tvApellido);
        tvTelefono = view.findViewById(R.id.tvTelefono);
        tvCorreo = view.findViewById(R.id.tvCorreo);
        tvDireccion = view.findViewById(R.id.tvDireccion);
        tvIdentificacion = view.findViewById(R.id.tvIdentificacion);

        // Inicializar Retrofit
        setupRetrofit();

        // Cargar datos del perfil
        cargarPerfil();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/") // Asegúrate que termine en /
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);
    }

    private void cargarPerfil() {
        // Leer token desde SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "No se encontró token. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        perfilCall = authService.obtenerPerfil("Bearer " + token);

        perfilCall.enqueue(new Callback<List<PerfilUsuarioResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<PerfilUsuarioResponse>> call, @NonNull Response<List<PerfilUsuarioResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    PerfilUsuarioResponse usuario = response.body().get(0);

                    // Mostrar datos del usuario en los TextViews
                    tvNombre.setText(usuario.getNombre_usuario());
                    tvApellido.setText(usuario.getApellido_usuario());
                    tvTelefono.setText(usuario.getTelefono_usuario());
                    tvCorreo.setText(usuario.getCorreo_usuario());
                    tvDireccion.setText(usuario.getDireccion_usuario());
                    tvIdentificacion.setText(usuario.getIdentificacion_usuario());

                } else if (response.code() == 401) {
                    Toast.makeText(requireContext(), "Sesión expirada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                    // Aquí puedes redirigir al login si lo deseas
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener la información del perfil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PerfilUsuarioResponse>> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PerfilUsuario", "Error al cargar perfil", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancelar llamada si está activa
        if (perfilCall != null && !perfilCall.isCanceled()) {
            perfilCall.cancel();
        }
    }
}
