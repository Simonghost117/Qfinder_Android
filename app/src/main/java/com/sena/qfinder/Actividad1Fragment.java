package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.ActividadGetResponse;
import com.sena.qfinder.models.ActividadListResponse;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Actividad1Fragment extends Fragment {

    private Spinner spinnerPacientes;
    private RecyclerView recyclerViewActividades;
    private Button btnAgregarActividad;
    private ActividadAdapter actividadAdapter;
    private Map<Integer, PacienteResponse> pacientesMap = new HashMap<>();
    private List<PacienteResponse> listaPacientes = new ArrayList<>();
    private int selectedPatientId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actividad1, container, false);

        spinnerPacientes = view.findViewById(R.id.spinnerPacientes);
        recyclerViewActividades = view.findViewById(R.id.recyclerViewActividades);
        btnAgregarActividad = view.findViewById(R.id.btnAgregarActividad);

        recyclerViewActividades.setLayoutManager(new LinearLayoutManager(getContext()));
        actividadAdapter = new ActividadAdapter(new ArrayList<>());
        recyclerViewActividades.setAdapter(actividadAdapter);

        cargarPacientes();

        spinnerPacientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    PacienteResponse pacienteSeleccionado = listaPacientes.get(position - 1);
                    selectedPatientId = pacienteSeleccionado.getId();
                    cargarActividades(pacienteSeleccionado.getId());
                } else {
                    actividadAdapter.setActividades(new ArrayList<>());
                    selectedPatientId = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                actividadAdapter.setActividades(new ArrayList<>());
                selectedPatientId = -1;
            }
        });

        btnAgregarActividad.setOnClickListener(v -> {
            if (selectedPatientId == -1) {
                Toast.makeText(getContext(), "Seleccione un paciente primero", Toast.LENGTH_SHORT).show();
                return;
            }

            AgregarActividadDialogFragment dialog = AgregarActividadDialogFragment.newInstance(selectedPatientId);
            dialog.setOnActividadGuardadaListener(() -> {
                cargarActividades(selectedPatientId);
            });
            dialog.show(getParentFragmentManager(), "AgregarActividadDialog");
        });

        return view;
    }

    private void cargarPacientes() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthService authService = ApiClient.getClient().create(AuthService.class);
        Call<PacienteListResponse> call = authService.listarPacientes("Bearer " + token);

        call.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PacienteListResponse> call, @NonNull Response<PacienteListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPacientes = response.body().getData();
                    if (listaPacientes != null && !listaPacientes.isEmpty()) {
                        configurarSpinnerPacientes();
                    } else {
                        Toast.makeText(getContext(), "No hay pacientes registrados", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PacienteListResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerPacientes() {
        List<String> nombresPacientes = new ArrayList<>();
        nombresPacientes.add("Seleccione un paciente");

        for (PacienteResponse paciente : listaPacientes) {
            nombresPacientes.add(paciente.getNombre() + " " + paciente.getApellido());
            pacientesMap.put(paciente.getId(), paciente);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombresPacientes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPacientes.setAdapter(adapter);
    }

    private void cargarActividades(int idPaciente) {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthService authService = ApiClient.getClient().create(AuthService.class);
        Call<ActividadListResponse> call = authService.listarActividades("Bearer " + token, idPaciente);

        call.enqueue(new Callback<ActividadListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActividadListResponse> call, @NonNull Response<ActividadListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<ActividadGetResponse> actividadesResponse = response.body().getData();
                    List<ActividadGetResponse> actividadesEnriquecidas = enriquecerActividades(actividadesResponse);
                    actividadAdapter.setActividades(actividadesEnriquecidas);
                } else {
                    Toast.makeText(getContext(), "No se encontraron actividades", Toast.LENGTH_SHORT).show();
                    actividadAdapter.setActividades(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActividadListResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                actividadAdapter.setActividades(new ArrayList<>());
            }
        });
    }

    private List<ActividadGetResponse> enriquecerActividades(List<ActividadGetResponse> actividadesResponse) {
        List<ActividadGetResponse> actividadesEnriquecidas = new ArrayList<>();

        for (ActividadGetResponse actividad : actividadesResponse) {
            PacienteResponse paciente = pacientesMap.get(actividad.getIdPaciente());
            String nombrePaciente = paciente != null ?
                    paciente.getNombre() + " " + paciente.getApellido() : "Paciente desconocido";

            ActividadGetResponse actividadEnriquecida = new ActividadGetResponse();
            actividadEnriquecida.setId(actividad.getId());
            actividadEnriquecida.setIdPaciente(actividad.getIdPaciente());
            actividadEnriquecida.setTitulo(actividad.getTitulo());
            actividadEnriquecida.setFecha(actividad.getFecha());
            actividadEnriquecida.setHora(actividad.getHora());
            actividadEnriquecida.setDescripcion(actividad.getDescripcion());
            actividadEnriquecida.setEstado(actividad.getEstado());

            actividadesEnriquecidas.add(actividadEnriquecida);
        }

        return actividadesEnriquecidas;
    }
}