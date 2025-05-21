package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class episodios_salud_menu extends AppCompatActivity {

    private Spinner spinnerPacientes;
    private int pacienteIdSeleccionado = -1;
    private Map<String, Integer> nombreIdMap = new HashMap<>();
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_episodios_salud_menu);

        // Ajuste de padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar Spinner de organización
        Spinner spinnerOrganizar = findViewById(R.id.spinner_organizar);
        ArrayAdapter<CharSequence> adapterOrganizar = ArrayAdapter.createFromResource(
                this,
                R.array.Opciones_de_organizar,
                android.R.layout.simple_spinner_item
        );
        adapterOrganizar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrganizar.setAdapter(adapterOrganizar);

        // Leer token desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String rawToken = preferences.getString("token", null);
        if (rawToken != null) {
            token = "Bearer " + rawToken; // Aquí formateamos el token correctamente
        } else {
            Toast.makeText(this, "Token no encontrado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            finish(); // O redirigir al login
            return;
        }

        // Inicializar spinner de pacientes y cargar datos
        spinnerPacientes = findViewById(R.id.SeleccionarPaciente);
        cargarPacientes();

        // Botón flotante para nueva nota
        FloatingActionButton btnNuevaNota = findViewById(R.id.btnNuevaNota);
        btnNuevaNota.setOnClickListener(v -> {
            if (pacienteIdSeleccionado != -1) {
                Intent intent = new Intent(episodios_salud_menu.this, episodios_salud_nota.class);
                intent.putExtra("paciente_id", pacienteIdSeleccionado);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Por favor selecciona un paciente primero.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cargarPacientes() {
        // Verificar si el token existe
        if (token == null || token.isEmpty()) {
            Log.e("ERROR", "Token no encontrado");
            return;
        }

        // Realizar la llamada Retrofit para obtener los pacientes
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        authService.listarPacientes(token).enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(Call<PacienteListResponse> call, Response<PacienteListResponse> response) {
                if (response.isSuccessful()) {
                    PacienteListResponse pacienteListResponse = response.body();
                    if (pacienteListResponse != null && pacienteListResponse.isSuccess()) {
                        // Llenar el Spinner con los pacientes
                        List<PacienteResponse> pacientes = pacienteListResponse.getData();
                        List<String> nombresPacientes = new ArrayList<>();
                        nombreIdMap.clear(); // Limpiar el mapa antes de llenarlo
                        for (PacienteResponse paciente : pacientes) {
                            nombresPacientes.add(paciente.getNombre() + " " + paciente.getApellido());
                            nombreIdMap.put(paciente.getNombre() + " " + paciente.getApellido(), paciente.getId());
                        }

                        // Configurar el Adapter para el Spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(episodios_salud_menu.this, android.R.layout.simple_spinner_item, nombresPacientes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPacientes.setAdapter(adapter);

                        // Configurar el listener para la selección de paciente
                        spinnerPacientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                String pacienteSeleccionado = (String) parentView.getItemAtPosition(position);
                                pacienteIdSeleccionado = nombreIdMap.get(pacienteSeleccionado);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                pacienteIdSeleccionado = -1;
                            }
                        });
                    } else {
                        Toast.makeText(episodios_salud_menu.this, "No se encontraron pacientes", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ERROR", "Error en la respuesta: " + response.code());
                    Toast.makeText(episodios_salud_menu.this, "Error al cargar pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PacienteListResponse> call, Throwable t) {
                Log.e("ERROR", "Error de conexión: " + t.getMessage());
                Toast.makeText(episodios_salud_menu.this, "Error al cargar pacientes: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
