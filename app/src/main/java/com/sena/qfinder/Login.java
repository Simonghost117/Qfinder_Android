package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.controller.MainActivityDash;
import com.sena.qfinder.models.LoginRequest;
import com.sena.qfinder.models.LoginResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends Fragment {

    private EditText emailEditText, passwordEditText;
    private TextView btnRegistro, btnOlvidarContrasena;
    private Button btnLogin;
    private AlertDialog progressDialog;
    private AuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRetrofit();
        setupListeners();
    }

    private void initViews(View view) {
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        btnLogin = view.findViewById(R.id.loginButton);
        btnRegistro = view.findViewById(R.id.registerLink);
        btnOlvidarContrasena = view.findViewById(R.id.forgotPassword);
    }

    private void showProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_progress_minimal, null);

        progressDialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MinimalProgressDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configuración adicional para hacerlo transparente
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validarCampos()) {
                iniciarSesionEnBackend();
            }
        });

        btnRegistro.setOnClickListener(v -> navegarARegistro());
        btnOlvidarContrasena.setOnClickListener(v -> forgotPassword());
    }

    private boolean validarCampos() {
        boolean valido = true;
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("El correo es obligatorio");
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Ingrese un correo válido");
            valido = false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("La contraseña es obligatoria");
            valido = false;
        }

        return valido;
    }

    private void iniciarSesionEnBackend() {
        showProgressDialog();

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        LoginRequest request = new LoginRequest(email, password);

        authService.LoginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                dismissProgressDialog();

                if (response.isSuccessful() && response.body() != null) {
                    String mensaje = response.body().getMensaje();
//                    Toast.makeText(getContext(), mensaje != null ? mensaje : "Inicio de sesión exitoso",
//                            Toast.LENGTH_SHORT).show();

                    guardarDatosUsuario(email, response.body().getToken());
                    iniciarSesionExitoso();
                } else {
                    Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                dismissProgressDialog();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarDatosUsuario(String email, String token) {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        preferences.edit()
                .putString("correo_usuario", email)
                .putString("token", token)
                .apply();
    }

    private void iniciarSesionExitoso() {
        try {
            startActivity(new Intent(requireContext(), MainActivityDash.class));
            requireActivity().finish();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void navegarARegistro() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegistroUsuario())
                .addToBackStack(null)
                .commit();
    }

    private void forgotPassword() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Fragment_password_recovery())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissProgressDialog();
    }
}