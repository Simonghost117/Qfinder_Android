package com.sena.qfinder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.AsignarMedicamentoRequest;
import com.sena.qfinder.models.AsignarMedicamentoResponse;
import com.sena.qfinder.models.MedicamentoResponse;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ListaAsignarMedicamentos extends Fragment {

    private Button btnOpenModalAsignar;
    private Calendar startDate, endDate;
    private boolean isSelectingStartDate = true;
    private SimpleDateFormat dateFormatter;
    private LinearLayout patientsContainer;
    private Spinner spinnerPatientsMain;
    private SharedPreferences sharedPreferences;
    private Map<Integer, String> pacientesMap = new HashMap<>();
    private Map<Integer, String> medicamentosMap = new HashMap<>();
    private int selectedPatientId = -1;
    private String selectedPatientName = "";

    public ListaAsignarMedicamentos() {}

    public static ListaAsignarMedicamentos newInstance() {
        return new ListaAsignarMedicamentos();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_asignar_medicamentos, container, false);

        btnOpenModalAsignar = view.findViewById(R.id.btnOpenModalAsignar);
        spinnerPatientsMain = view.findViewById(R.id.spinner_patients_main);

        setupPatientsSection(view, inflater);

        btnOpenModalAsignar.setOnClickListener(view1 -> mostrarDialogoAgregarMedicamento());

        return view;
    }

    private void setupPatientsSection(View rootView, LayoutInflater inflater) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.setScrollbarFadingEnabled(true);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);

        patientsContainer = new LinearLayout(getContext());
        patientsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        patientsContainer.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScrollView.addView(patientsContainer);

        LinearLayout layoutMedicamentos = rootView.findViewById(R.id.layoutMedicamentos);
        layoutMedicamentos.addView(horizontalScrollView, 1);

        loadPatients();
    }

    private void loadPatients() {
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<PacienteListResponse> call = authService.listarPacientes("Bearer " + token);

        call.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PacienteListResponse> call, @NonNull Response<PacienteListResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<PacienteResponse> pacientes = response.body().getData();
                    if (pacientes != null) {
                        pacientesMap.clear();
                        List<String> pacientesNombres = new ArrayList<>();

                        for (PacienteResponse paciente : pacientes) {
                            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
                            pacientesMap.put(paciente.getId(), nombreCompleto);
                            pacientesNombres.add(nombreCompleto);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_spinner_item,
                                pacientesNombres);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPatientsMain.setAdapter(adapter);

                        mostrarPacientes(pacientes);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PacienteListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarPacientes(List<PacienteResponse> pacientes) {
        patientsContainer.removeAllViews();

        for (PacienteResponse paciente : pacientes) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";
            String fechaNacimiento = paciente.getFecha_nacimiento();

            addPatientCard(nombreCompleto, fechaNacimiento, diagnostico,
                    R.drawable.perfil_paciente, paciente.getId());
        }
    }

    private void addPatientCard(String name, String birthDate, String conditions,
                                int imageResId, int patientId) {
        View patientCard = LayoutInflater.from(getContext())
                .inflate(R.layout.item_patient_card, patientsContainer, false);
        patientCard.setTag(patientId);

        TextView tvName = patientCard.findViewById(R.id.tvPatientName);
        TextView tvRelation = patientCard.findViewById(R.id.tvPatientRelation);
        TextView tvConditions = patientCard.findViewById(R.id.tvPatientConditions);
        ImageView ivProfile = patientCard.findViewById(R.id.ivPatientProfile);

        tvName.setText(name);
        tvRelation.setText(birthDate);

        if (conditions != null && !conditions.isEmpty()) {
            String[] conditionsList = conditions.split(",");
            StringBuilder formattedConditions = new StringBuilder();
            for (String condition : conditionsList) {
                formattedConditions.append("• ").append(condition.trim()).append("\n");
            }
            tvConditions.setText(formattedConditions.toString().trim());
        } else {
            tvConditions.setText("• Sin diagnóstico");
        }

        ivProfile.setImageResource(imageResId);

        patientCard.setOnClickListener(v -> {
            selectedPatientId = patientId;
            selectedPatientName = name;
            updatePatientSelection();
            Toast.makeText(getContext(), "Mostrando medicamentos de " + name, Toast.LENGTH_SHORT).show();
        });

        patientsContainer.addView(patientCard);
    }

    private void updatePatientSelection() {
        if (selectedPatientId != -1 && pacientesMap.containsKey(selectedPatientId)) {
            String selectedName = pacientesMap.get(selectedPatientId);
            int position = ((ArrayAdapter<String>) spinnerPatientsMain.getAdapter())
                    .getPosition(selectedName);
            if (position >= 0) {
                spinnerPatientsMain.setSelection(position);
            }
        }
    }

    private void mostrarDialogoAgregarMedicamento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_agregar_medicamento_usuario, null);
        builder.setView(viewInflated);

        Spinner spinnerPatients = viewInflated.findViewById(R.id.spinner_patients);
        Spinner spinnerMedications = viewInflated.findViewById(R.id.spinner_medications);
        LinearLayout layoutDatePicker = viewInflated.findViewById(R.id.layout_date_picker);
        TextView tvSelectedDates = viewInflated.findViewById(R.id.tv_selected_dates);
        EditText etDosage = viewInflated.findViewById(R.id.et_dosage);
        EditText etFrequency = viewInflated.findViewById(R.id.et_description);
        Button btnSave = viewInflated.findViewById(R.id.btn_save);

        setupSpinners(spinnerPatients, spinnerMedications);
        setupDatePicker(layoutDatePicker, tvSelectedDates);

        btnSave.setOnClickListener(v -> {
            if (validarCampos(spinnerPatients, spinnerMedications, etDosage, tvSelectedDates)) {
                // Obtener los datos del formulario
                String pacienteNombre = spinnerPatients.getSelectedItem().toString();
                String medicamentoNombre = spinnerMedications.getSelectedItem().toString();
                String dosis = etDosage.getText().toString();
                String frecuencia = etFrequency.getText().toString();
                String[] fechas = tvSelectedDates.getText().toString().split(" - ");

                // Obtener IDs de los maps
                int idPaciente = obtenerIdPorNombre(pacientesMap, pacienteNombre);
                int idMedicamento = obtenerIdPorNombre(medicamentosMap, medicamentoNombre);

                if (idPaciente == -1 || idMedicamento == -1) {
                    Toast.makeText(getContext(), "Error al obtener datos del paciente o medicamento", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crear request
                AsignarMedicamentoRequest request = new AsignarMedicamentoRequest(
                        idPaciente,
                        idMedicamento,
                        fechas[0], // fecha inicio
                        fechas[1], // fecha fin
                        dosis,
                        frecuencia
                );

                // Llamar al API
                asignarMedicamento(request);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    private int obtenerIdPorNombre(Map<Integer, String> map, String nombre) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(nombre)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private void asignarMedicamento(AsignarMedicamentoRequest request) {
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<AsignarMedicamentoResponse> call = authService.asignarMedicamento("Bearer " + token, request);

        call.enqueue(new Callback<AsignarMedicamentoResponse>() {
            @Override
            public void onResponse(@NonNull Call<AsignarMedicamentoResponse> call,
                                   @NonNull Response<AsignarMedicamentoResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    AsignarMedicamentoResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Medicamento asignado correctamente", Toast.LENGTH_SHORT).show();
                        // Aquí puedes actualizar la UI si es necesario
                    } else {
                        Toast.makeText(getContext(), "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Toast.makeText(getContext(), "Error en la respuesta: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AsignarMedicamentoResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Error al asignar medicamento", t);
            }
        });
    }

    private void setupSpinners(Spinner spinnerPatients, Spinner spinnerMedications) {
        // Configurar spinner de pacientes
        List<String> pacientesNombres = new ArrayList<>(pacientesMap.values());
        ArrayAdapter<String> patientsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                pacientesNombres
        );
        patientsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatients.setAdapter(patientsAdapter);

        // Configurar spinner de medicamentos
        ArrayAdapter<String> medicationsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        medicationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedications.setAdapter(medicationsAdapter);

        // Cargar medicamentos desde la API
        loadMedications(spinnerMedications);
    }

    private void loadMedications(Spinner spinnerMedications) {
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<List<MedicamentoResponse>> call = authService.listarMedicamentos("Bearer " + token);

        call.enqueue(new Callback<List<MedicamentoResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MedicamentoResponse>> call,
                                   @NonNull Response<List<MedicamentoResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<MedicamentoResponse> medicamentos = response.body();
                    medicamentosMap.clear();
                    List<String> nombresMedicamentos = new ArrayList<>();

                    for (MedicamentoResponse medicamento : medicamentos) {
                        medicamentosMap.put(medicamento.getId_medicamento(), medicamento.getNombre());
                        nombresMedicamentos.add(medicamento.getNombre());
                    }

                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerMedications.getAdapter();
                    adapter.clear();
                    adapter.addAll(nombresMedicamentos);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al obtener medicamentos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MedicamentoResponse>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePicker(LinearLayout layout, TextView tv) {
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 7); // Fecha fin por defecto: 7 días después

        // Mostrar fechas iniciales
        String fechaInicio = dateFormatter.format(startDate.getTime());
        String fechaFin = dateFormatter.format(endDate.getTime());
        tv.setText(fechaInicio + " - " + fechaFin);

        layout.setOnClickListener(v -> {
            Calendar currentDate = isSelectingStartDate ? startDate : endDate;

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        currentDate.set(year, month, dayOfMonth);
                        isSelectingStartDate = !isSelectingStartDate;

                        // Asegurar que fecha fin no sea anterior a fecha inicio
                        if (endDate.before(startDate)) {
                            endDate = (Calendar) startDate.clone();
                            endDate.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        String fechaInicioStr = dateFormatter.format(startDate.getTime());
                        String fechaFinStr = dateFormatter.format(endDate.getTime());
                        tv.setText(fechaInicioStr + " - " + fechaFinStr);
                    },
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    currentDate.get(Calendar.DAY_OF_MONTH));

            // Establecer fecha mínima si es para fecha fin
            if (!isSelectingStartDate) {
                datePickerDialog.getDatePicker().setMinDate(startDate.getTimeInMillis());
            }

            datePickerDialog.show();
        });
    }

    private boolean validarCampos(Spinner spinnerPatients, Spinner spinnerMedications,
                                  EditText etDosage, TextView tvSelectedDates) {
        if (spinnerPatients.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Selecciona un paciente", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerMedications.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Selecciona un medicamento", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDosage.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Ingresa la dosis", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvSelectedDates.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Selecciona las fechas", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}