package com.sena.qfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.controller.MainActivityDash;
import com.sena.qfinder.models.LoginRequest;
import com.sena.qfinder.models.LoginResponse;
import com.sena.qfinder.ui.home.DashboardFragment;

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
    private ProgressDialog progressDialog;
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

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Iniciando sesión");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);
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
        progressDialog.show();

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        LoginRequest request = new LoginRequest(email, password);

        // Log the request
        Log.d("LOGIN", "Attempting login with: " + email);
        Log.d("LOGIN", "Request body: " + new Gson().toJson(request));

        authService.LoginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                progressDialog.dismiss();

                // Log the response
                Log.d("LOGIN", "Response code: " + response.code());
                if (response.errorBody() != null) {
                    try {
                        Log.d("LOGIN", "Error response: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                    guardarEmailUsuario(email);
                    iniciarSesionExitoso();
                } else {
                    Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarEmailUsuario(String email) {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("correo_usuario", email);
        editor.apply();
    }

    private void iniciarSesionExitoso() {
        Intent intent = new Intent(requireContext(), MainActivityDash.class);
        startActivity(intent);
        requireActivity().finish();
    }
    private void navegarARegistro() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new RegistroUsuario())
                .addToBackStack(null)
                .commit();
    }

    private void forgotPassword() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new Fragment_password_recovery())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
