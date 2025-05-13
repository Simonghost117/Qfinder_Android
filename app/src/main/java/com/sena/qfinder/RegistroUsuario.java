package com.sena.qfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.RegisterRequest;
import com.sena.qfinder.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroUsuario extends Fragment {

    ImageView btnBack;
    private TextInputEditText edtNombre, edtApellido, edtCorreo, edtIdentificacion, edtDirrecion, edtTelefono;
    private Button btnContinuar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro_usuario, container, false);

        // Asignación de elementos UI
        edtNombre = view.findViewById(R.id.edtNombre);
        edtApellido = view.findViewById(R.id.edtApellido);
        edtCorreo = view.findViewById(R.id.edtCorreo);
        edtIdentificacion = view.findViewById(R.id.edtIdentificacion);
        edtDirrecion = view.findViewById(R.id.edtDireccion);
        edtTelefono = view.findViewById(R.id.edtTelefono);
        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnBack = view.findViewById(R.id.btnBack);

        // Botón para volver al login
        btnBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new Login());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Botón para continuar y registrar usuario
        btnContinuar.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarUsuarioEnBackend();
            }
        });

        return view;
    }

    private boolean validarCampos() {
        if (edtNombre.getText().toString().isEmpty()) {
            mostrarError("El nombre es obligatorio", edtNombre);
            return false;
        }

        if (edtApellido.getText().toString().isEmpty()) {
            mostrarError("El apellido es obligatorio", edtApellido);
            return false;
        }

        if (edtCorreo.getText().toString().isEmpty()) {
            mostrarError("El correo es obligatorio", edtCorreo);
            return false;
        }

        if (edtIdentificacion.getText().toString().isEmpty()) {
            mostrarError("La identificación es obligatoria", edtIdentificacion);
            return false;
        }

        if (edtDirrecion.getText().toString().isEmpty()) {
            mostrarError("La dirección es obligatoria", edtDirrecion);
            return false;
        }

        if (edtTelefono.getText().toString().isEmpty()) {
            mostrarError("El teléfono es obligatorio", edtTelefono);
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje, TextInputEditText campo) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        campo.requestFocus();
    }

    private void registrarUsuarioEnBackend() {
        String nombre = edtNombre.getText().toString();
        String correo = edtCorreo.getText().toString();
        String password = edtIdentificacion.getText().toString(); // Puedes cambiar por un campo real de contraseña

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-backend.onrender.com/") // URL base del backend
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService authService = retrofit.create(AuthService.class);
        RegisterRequest request = new RegisterRequest(nombre, correo, password);

        authService.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, new Login())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Error al registrar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
