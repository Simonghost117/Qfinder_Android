package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.NotaEpisodio;
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
    private ListView listViewNotas;
    private ArrayAdapter<String> notasAdapter;
    private List<String> notasStrings = new ArrayList<>();
    private TextView cantidadRegistros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_episodios_salud_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerPacientes = findViewById(R.id.SeleccionarPaciente);
        listViewNotas = findViewById(R.id.listViewNotas);
        cantidadRegistros = findViewById(R.id.cantidadRegistros);
        notasAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notasStrings);
        listViewNotas.setAdapter(notasAdapter);

        // Obtener token guardado
        SharedPreferences preferences = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String rawToken = preferences.getString("token", null);
        if (rawToken != null) {
            token = "Bearer " + rawToken;
        } else {
            Toast.makeText(this, "Token no encontrado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cargarPacientes();

        FloatingActionButton btnNuevaNota = findViewById(R.id.btnNuevaNota);
        btnNuevaNota.setOnClickListener(v -> {
            if (pacienteIdSeleccionado != -1) {
                Intent intent = new Intent(episodios_salud_menu.this, episodios_salud_nota.class);
                intent.putExtra("id_paciente", pacienteIdSeleccionado);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Por favor selecciona un paciente primero.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPacientes() {
        if (token == null || token.isEmpty()) {
            Log.e("episodios_salud_menu", "Token no encontrado");
            return;
        }

        AuthService authService = ApiClient.getAuthService();
        authService.listarPacientes(token).enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(Call<PacienteListResponse> call, Response<PacienteListResponse> response) {
                if (response.isSuccessful()) {
                    PacienteListResponse pacienteListResponse = response.body();
                    if (pacienteListResponse != null && pacienteListResponse.isSuccess()) {
                        List<PacienteResponse> pacientes = pacienteListResponse.getData();
                        List<String> nombresPacientes = new ArrayList<>();
                        nombreIdMap.clear();

                        for (PacienteResponse paciente : pacientes) {
                            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
                            nombresPacientes.add(nombreCompleto);
                            nombreIdMap.put(nombreCompleto, paciente.getId());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(episodios_salud_menu.this,
                                android.R.layout.simple_spinner_item, nombresPacientes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPacientes.setAdapter(adapter);

                        spinnerPacientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String pacienteSeleccionado = (String) parent.getItemAtPosition(position);
                                pacienteIdSeleccionado = nombreIdMap.get(pacienteSeleccionado);
                                cargarNotasDelPaciente(pacienteIdSeleccionado);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                pacienteIdSeleccionado = -1;
                                notasStrings.clear();
                                notasAdapter.notifyDataSetChanged();
                                actualizarCantidadRegistros();
                            }
                        });

                    } else {
                        Toast.makeText(episodios_salud_menu.this, "No se encontraron pacientes", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("episodios_salud_menu", "Error en la respuesta: " + response.code());
                    Toast.makeText(episodios_salud_menu.this, "Error al cargar pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PacienteListResponse> call, Throwable t) {
                Log.e("episodios_salud_menu", "Error de conexión: " + t.getMessage());
                Toast.makeText(episodios_salud_menu.this, "Error al cargar pacientes: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarNotasDelPaciente(int idPaciente) {
        AuthService authService = ApiClient.getAuthService();
        authService.obtenerEpisodios(token, idPaciente).enqueue(new Callback<List<NotaEpisodio>>() {
            @Override
            public void onResponse(Call<List<NotaEpisodio>> call, Response<List<NotaEpisodio>> response) {
                if (response.isSuccessful()) {
                    List<NotaEpisodio> notas = response.body();
                    notasStrings.clear();

                    if (notas != null) {
                        for (NotaEpisodio nota : notas) {
                            String resumen = "Fecha: " + nota.getFechaHoraInicio() +
                                    "\nSeveridad: " + nota.getSeveridad() +
                                    "\nDescripción: " + nota.getDescripcion();
                            notasStrings.add(resumen);
                        }
                    }

                    notasAdapter.notifyDataSetChanged();
                    actualizarCantidadRegistros();

                } else {
                    Toast.makeText(episodios_salud_menu.this, "Error al cargar notas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<NotaEpisodio>> call, Throwable t) {
                Toast.makeText(episodios_salud_menu.this, "Error al cargar notas: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarCantidadRegistros() {
        int cantidad = notasStrings.size();
        cantidadRegistros.setText(cantidad + (cantidad == 1 ? " Registro" : " Registros"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga las notas para el paciente seleccionado al volver
        if (pacienteIdSeleccionado != -1) {
            cargarNotasDelPaciente(pacienteIdSeleccionado);
        }
    }
}
