package com.sena.qfinder;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
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

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroUsuario extends Fragment {

    private TextInputEditText edtNombre, edtApellido, edtCorreo, edtIdentificacion, edtDirrecion, edtTelefono, edtContrasena;
    private Button btnContinuar;
    private ImageView btnBack;
    private ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro_usuario, container, false);

        // Inicializar vistas
        initViews(view);

        // Configurar listeners
        setupListeners();

        return view;

    }

    private void initViews(View view) {
        edtNombre = view.findViewById(R.id.edtNombre);
        edtApellido = view.findViewById(R.id.edtApellido);
        edtCorreo = view.findViewById(R.id.edtCorreo);
        edtIdentificacion = view.findViewById(R.id.edtIdentificacion);
        edtDirrecion = view.findViewById(R.id.edtDireccion);
        edtTelefono = view.findViewById(R.id.edtTelefono);
        edtContrasena = view.findViewById(R.id.edtContrasena);
        btnContinuar = view.findViewById(R.id.btnContinuar);
        btnBack = view.findViewById(R.id.btnBack);

        // Configurar ProgressDialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Registrando");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> volverALogin());

        btnContinuar.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarUsuarioEnBackend();
            }
        });
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (edtNombre.getText().toString().trim().isEmpty()) {
            edtNombre.setError("El nombre es obligatorio");
            valido = false;
        }

        if (edtApellido.getText().toString().trim().isEmpty()) {
            edtApellido.setError("El apellido es obligatorio");
            valido = false;
        }

        if (edtCorreo.getText().toString().trim().isEmpty()) {
            edtCorreo.setError("El correo es obligatorio");
            valido = false;
        } else if (!esCorreoValido(edtCorreo.getText().toString().trim())) {
            edtCorreo.setError("Ingrese un correo válido");
            valido = false;
        }

        if (edtIdentificacion.getText().toString().trim().isEmpty()) {
            edtIdentificacion.setError("La identificación es obligatoria");
            valido = false;
        }

        if (edtDirrecion.getText().toString().trim().isEmpty()) {
            edtDirrecion.setError("La dirección es obligatoria");
            valido = false;
        }

        if (edtTelefono.getText().toString().trim().isEmpty()) {
            edtTelefono.setError("El teléfono es obligatorio");
            valido = false;
        }

        return valido;
    }

    private boolean esCorreoValido(String correo) {
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches();
    }

    private void registrarUsuarioEnBackend() {
        progressDialog.show();

        // Obtener datos del formulario
        String nombre = edtNombre.getText().toString().trim();
        String apellido = edtApellido.getText().toString().trim();
        String identificacion = edtIdentificacion.getText().toString().trim();
        String direccion = edtDirrecion.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String contrasena = generarContrasenaSegura(edtContrasena.getText().toString().trim());

        // Crear objeto de solicitud
        RegisterRequest request = new RegisterRequest(
                nombre,
                apellido,
                identificacion,
                direccion,
                telefono,
                correo,
                contrasena
        );

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService authService = retrofit.create(AuthService.class);

        // Realizar la llamada al servidor
        Call<RegisterResponse> call = authService.registerUser(request);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    manejarRegistroExitoso();

                } else {
                    manejarErrorRegistro(response);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generarContrasenaSegura(String identificacion) {
        // Genera una contraseña segura basada en la identificación
        // Esto es solo un ejemplo - considera usar un método más seguro en producción
        return identificacion;
    }

    private void manejarRegistroExitoso() {
        Toast.makeText(getContext(), "Registro exitoso. Verifica tu correo electrónico.", Toast.LENGTH_LONG).show();
        confirmCorreo();
    }

    private void manejarErrorRegistro(Response<RegisterResponse> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
            Toast.makeText(getContext(), "Error al registrar: " + errorBody, Toast.LENGTH_LONG).show();
            System.out.println(errorBody);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
        }
    }

    private void volverALogin() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new Login())
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void confirmCorreo() {
        String correo = edtCorreo.getText().toString().trim();
        VerficacionCorreo fragment = VerficacionCorreo.newInstance(correo);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}