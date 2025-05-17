package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.material.card.MaterialCardView;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PerfilUsuarioResponse;
import com.sena.qfinder.model.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class perfil_usuario extends Fragment {

    private TextView tvNombre, tvApellido, tvTelefono, tvCorreo, tvDireccion, tvIdentificacion;
    private MaterialCardView cardCerrarSesion;

    private AuthService authService;
    private Call<PerfilUsuarioResponse> perfilCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvApellido = view.findViewById(R.id.tvApellido);
        tvTelefono = view.findViewById(R.id.tvTelefono);
        tvCorreo = view.findViewById(R.id.tvCorreo);
        tvDireccion = view.findViewById(R.id.tvDireccion);
        tvIdentificacion = view.findViewById(R.id.tvIdentificacion);

        cardCerrarSesion = view.findViewById(R.id.cardCerrarSesion);

        cardCerrarSesion.setOnClickListener(v -> {
            logout();
        });

        setupRetrofit();
        cargarPerfil();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);
    }

    private void logout() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Borra todos los datos del usuario
        editor.apply();

        Toast.makeText(requireContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // Redirigir a MainActivity
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void cargarPerfil() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "No se encontró token. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        perfilCall = authService.obtenerPerfil("Bearer " + token);

        perfilCall.enqueue(new Callback<PerfilUsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<PerfilUsuarioResponse> call, @NonNull Response<PerfilUsuarioResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    PerfilUsuarioResponse usuario = response.body();

                    tvNombre.setText(usuario.getNombre_usuario());
                    tvApellido.setText(usuario.getApellido_usuario());
                    tvTelefono.setText(usuario.getTelefono_usuario());
                    tvCorreo.setText(usuario.getCorreo_usuario());
                    tvDireccion.setText(usuario.getDireccion_usuario());
                    tvIdentificacion.setText(usuario.getIdentificacion_usuario());
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener el perfil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PerfilUsuarioResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PerfilUsuario", "Error al cargar perfil", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (perfilCall != null && !perfilCall.isCanceled()) {
            perfilCall.cancel();
        }
    }
}