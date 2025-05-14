package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.RegisterPacienteRequest;
import com.sena.qfinder.models.RegisterPacienteResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistrarPaciente extends Fragment {

    private EditText editNombre, editApellido, editFechaNacimiento, editSexo, editDiagnostico, editIdentificacion;
    private Button btnRegistrar;

    public RegistrarPaciente() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registrar_paciente, container, false);

        editNombre = view.findViewById(R.id.edtNombre);
        editApellido = view.findViewById(R.id.edtApellido);
        editFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        editSexo = view.findViewById(R.id.etSexo);
        editDiagnostico = view.findViewById(R.id.etDiagnostico);
        editIdentificacion = view.findViewById(R.id.etIdentificacion);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> registrarPaciente());

        return view;
    }

    private void registrarPaciente() {
        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String fechaNacimiento = editFechaNacimiento.getText().toString().trim();
        String sexo = editSexo.getText().toString().trim();
        String diagnostico = editDiagnostico.getText().toString().trim();
        String identificacionStr = editIdentificacion.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || TextUtils.isEmpty(fechaNacimiento) ||
                TextUtils.isEmpty(sexo) || TextUtils.isEmpty(diagnostico) || TextUtils.isEmpty(identificacionStr)) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el id_usuario dinámicamente desde SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("id_usuario", -1); // -1 si no existe

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto de solicitud
        RegisterPacienteRequest request = new RegisterPacienteRequest(
                idUsuario, nombre, apellido, fechaNacimiento, sexo, diagnostico, identificacionStr
        );

        // Crear Retrofit y AuthService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService authService = retrofit.create(AuthService.class);

        // Enviar solicitud al backend
        Call<RegisterPacienteResponse> call = authService.registrarPaciente(request);
        call.enqueue(new Callback<RegisterPacienteResponse>() {
            @Override
            public void onResponse(Call<RegisterPacienteResponse> call, Response<RegisterPacienteResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Paciente registrado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                } else {
                    Toast.makeText(getContext(), "Error al registrar paciente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterPacienteResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarCampos() {
        editNombre.setText("");
        editApellido.setText("");
        editFechaNacimiento.setText("");
        editSexo.setText("");
        editDiagnostico.setText("");
        editIdentificacion.setText("");
    }
}
