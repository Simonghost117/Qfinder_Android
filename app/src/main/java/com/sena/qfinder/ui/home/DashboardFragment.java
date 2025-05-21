package com.sena.qfinder.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.R;
import com.sena.qfinder.RegistrarPaciente;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.models.ActividadGetResponse;
import com.sena.qfinder.models.ActividadListResponse;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;
import com.sena.qfinder.models.PerfilUsuarioResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardFragment extends Fragment {

    private LinearLayout patientsContainer, activitiesContainer;
    private RecyclerView rvMedications;
    private SharedPreferences sharedPreferences;
    private TextView tvUserName;
    private int selectedPatientId = -1;
    private String selectedPatientName = "";
    private LayoutInflater currentInflater;
    private View rootView;
    private Map<Integer, String> pacientesMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        currentInflater = inflater;

        sharedPreferences = requireContext().getSharedPreferences("prefs_qfinder", Context.MODE_PRIVATE);

        tvUserName = rootView.findViewById(R.id.tvUserName);
        setupUserInfo();

        setupPatientsSection();
        setupActivitiesSection();
        setupMedicationsSection();

        return rootView;
    }

    private void setupUserInfo() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            tvUserName.setText("Usuario desconocido");
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<PerfilUsuarioResponse> call = authService.obtenerPerfil("Bearer " + token);

        call.enqueue(new Callback<PerfilUsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<PerfilUsuarioResponse> call, @NonNull Response<PerfilUsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PerfilUsuarioResponse usuario = response.body();
                    String nombreCompleto = usuario.getNombre_usuario() + " " + usuario.getApellido_usuario();
                    tvUserName.setText(nombreCompleto);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("nombre_completo", nombreCompleto);
                    editor.apply();
                } else {
                    String nombreGuardado = preferences.getString("nombre_completo", "Usuario");
                    tvUserName.setText(nombreGuardado);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PerfilUsuarioResponse> call, @NonNull Throwable t) {
                Log.e("DashboardFragment", "Error al obtener perfil", t);
                String nombreGuardado = preferences.getString("nombre_completo", "Usuario");
                tvUserName.setText(nombreGuardado);
            }
        });
    }

    private void setupPatientsSection() {
        patientsContainer = rootView.findViewById(R.id.patientsContainer);
        patientsContainer.removeAllViews();

        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            }
            mostrarPacientes(new ArrayList<>());
            return;
        }

        // Configurar interceptor de logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService authService = retrofit.create(AuthService.class);
        Call<PacienteListResponse> call = authService.listarPacientes("Bearer " + token);

        call.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PacienteListResponse> call, @NonNull Response<PacienteListResponse> response) {
                if (!isAdded()) return;

                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<PacienteResponse> pacientes = response.body().getData();
                            // Guardar nombres de pacientes en el mapa
                            if (pacientes != null) {
                                for (PacienteResponse paciente : pacientes) {
                                    String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
                                    pacientesMap.put(paciente.getId(), nombreCompleto);
                                }
                            }
                            mostrarPacientes(pacientes != null ? pacientes : new ArrayList<>());

                            if (pacientes == null || pacientes.isEmpty()) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "No hay pacientes registrados", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin detalles";
                        Log.e("API", "Error: " + response.code() + " - " + errorBody);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al obtener pacientes", Toast.LENGTH_SHORT).show();
                        }
                        mostrarPacientes(new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e("API", "Error procesando respuesta", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error procesando datos", Toast.LENGTH_SHORT).show();
                    }
                    mostrarPacientes(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PacienteListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Fallo en la conexión", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
                mostrarPacientes(new ArrayList<>());
            }
        });
    }

    private void mostrarPacientes(List<PacienteResponse> pacientes) {
        patientsContainer.removeAllViews();

        for (PacienteResponse paciente : pacientes) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";

//            String relacion = paciente.isEs_cuidador_principal() ?
//                    "Cuidador principal" :
//                    (paciente.getParentesco() != null ?
//                            paciente.getParentesco() : "Paciente");
              String fecha_nacimiento = paciente.getFecha_nacimiento();
            addPatientCard(nombreCompleto, fecha_nacimiento, diagnostico,
                    R.drawable.perfil_paciente, paciente.getId());
        }

        View addCard = currentInflater.inflate(R.layout.item_add_patient_card, patientsContainer, false);
        addCard.setOnClickListener(v -> navigateToAddPatient());
        patientsContainer.addView(addCard);
    }

    private void addPatientCard(String name, String relation, String conditions, int imageResId, int patientId) {
        View patientCard = currentInflater.inflate(R.layout.item_patient_card, patientsContainer, false);
        patientCard.setTag(patientId);

        // Resaltar paciente seleccionado


        TextView tvName = patientCard.findViewById(R.id.tvPatientName);
        TextView tvRelation = patientCard.findViewById(R.id.tvPatientRelation);
        TextView tvConditions = patientCard.findViewById(R.id.tvPatientConditions);
        ImageView ivProfile = patientCard.findViewById(R.id.ivPatientProfile);

        tvName.setText(name);
        tvRelation.setText(relation);

        // Formatear condiciones como lista con viñetas
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
            updatePatientCardsHighlight();
            loadPatientActivities();
            Toast.makeText(getContext(), "Mostrando actividades de " + name, Toast.LENGTH_SHORT).show();
        });

        patientsContainer.addView(patientCard);
    }

    private void updatePatientCardsHighlight() {
        for (int i = 0; i < patientsContainer.getChildCount(); i++) {
            View child = patientsContainer.getChildAt(i);
            if (child.getTag() instanceof Integer) {
//                int cardPatientId = (Integer) child.getTag();
//                child.setBackgroundColor(
//                        cardPatientId == selectedPatientId ?
//                                ContextCompat.getColor(requireContext(), R.color.iconInactive) :
//                                ContextCompat.getColor(requireContext(), R.color.card_background)
//                );
            }
        }
    }

    private void setupActivitiesSection() {
        activitiesContainer = rootView.findViewById(R.id.activitiesContainer);
        activitiesContainer.removeAllViews();

        // Agregar encabezado
        View headerView = currentInflater.inflate(R.layout.section_header, activitiesContainer, false);
        TextView tvTitle = headerView.findViewById(R.id.tvSectionTitle);
        TextView tvSubtitle = headerView.findViewById(R.id.tvSectionSubtitle);

        if (selectedPatientId == -1) {
            tvTitle.setText("Actividades");
            tvSubtitle.setText("Seleccione un paciente");
            activitiesContainer.addView(headerView);

            View noPatientView = currentInflater.inflate(R.layout.item_no_selection, activitiesContainer, false);
            ((TextView) noPatientView.findViewById(R.id.tvMessage)).setText("Selecciona un paciente para ver sus actividades");
            activitiesContainer.addView(noPatientView);
        } else {
            tvTitle.setText("Actividades de " + selectedPatientName);
            tvSubtitle.setText("Últimas actividades registradas");
            activitiesContainer.addView(headerView);

            // Add loading indicator
            View loadingView = currentInflater.inflate(R.layout.item_loading, activitiesContainer, false);
            activitiesContainer.addView(loadingView);

            loadPatientActivities();
        }
    }

    private void loadPatientActivities() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPatientId == -1) {
            setupActivitiesSection(); // Refresh to show "select a patient" message
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<ActividadListResponse> call = authService.listarActividades("Bearer " + token, selectedPatientId);

        call.enqueue(new Callback<ActividadListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActividadListResponse> call, @NonNull Response<ActividadListResponse> response) {
                if (!isAdded()) return;

                Log.d("API_ACTIVIDADES", "Código de respuesta: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ActividadListResponse respuesta = response.body();
                    if (respuesta.isSuccess() && respuesta.getData() != null) {
                        List<ActividadGetResponse> actividades = respuesta.getData();
                        Log.d("API_ACTIVIDADES", "Actividades recibidas: " + actividades.size());
                        mostrarActividades(actividades);
                    } else {
                        Toast.makeText(getContext(), "No se encontraron actividades", Toast.LENGTH_SHORT).show();
                        mostrarActividades(new ArrayList<>());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin detalles";
                        Log.e("API_ACTIVIDADES", "Error: " + response.code() + " - " + errorBody);
                        if (response.code() == 500) {
                            Toast.makeText(getContext(), "Error del servidor al obtener actividades", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al obtener actividades", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("API_ACTIVIDADES", "Error al leer errorBody", e);
                        Toast.makeText(getContext(), "Error procesando respuesta", Toast.LENGTH_SHORT).show();
                    }
                    mostrarActividades(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActividadListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API_ACTIVIDADES", "Fallo en la conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                mostrarActividades(new ArrayList<>());
            }
        });
    }

    private void mostrarActividades(List<ActividadGetResponse> actividades) {
        activitiesContainer.removeAllViews();

        if (actividades == null || actividades.isEmpty()) {
            View noActivitiesView = currentInflater.inflate(R.layout.item_no_selection, activitiesContainer, false);
            ((TextView) noActivitiesView.findViewById(R.id.tvMessage)).setText("No hay actividades registradas");
            activitiesContainer.addView(noActivitiesView);
            return;
        }

        // Inflar el layout de tabla
        View tableView = currentInflater.inflate(R.layout.item_activity_table, activitiesContainer, false);
        TableLayout tableLayout = tableView.findViewById(R.id.activitiesTable);

        // Agregar filas de datos
        for (ActividadGetResponse actividad : actividades) {
            TableRow row = new TableRow(getContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // Obtener valores con manejo de nulos
            String titulo = actividad.getTitulo() != null ?
                    actividad.getTitulo() : "Sin título";
            String descripcion = actividad.getDescripcion() != null ?
                    actividad.getDescripcion() : "Sin descripción";

            String fecha = actividad.getFecha() != null ?
                    formatDate(actividad.getFecha()) :
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            String hora = actividad.getHora() != null ?
                    formatTime(actividad.getHora()) :
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            String estado = actividad.getEstado() != null ?
                    actividad.getEstado() : "pendiente";

            // Configurar celdas con pesos diferentes
            addDataCell(row, titulo, 1.5f, 14); // Título más ancho, texto 12sp
//            addDataCell(row, descripcion, 2f, 12); // Descripción más ancha, texto 12sp
            addDataCell(row, fecha, 1f, 12);
            addDataCell(row, hora, 1f, 14);

            // Celda de estado con estilo especial
            TextView statusCell = new TextView(getContext());
            statusCell.setText(estado);
            statusCell.setTextSize(12); // Texto más pequeño
            statusCell.setPadding(4, 4, 4, 4); // Padding reducido
            statusCell.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f));

            // Aplicar color según estado
            switch(estado.toLowerCase()) {
                case "cancelada":
                    statusCell.setTextColor(ContextCompat.getColor(getContext(), R.color.rojopasion));
                    break;
                case "completada":
                    statusCell.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                    break;
                case "en_progreso":
                    statusCell.setTextColor(ContextCompat.getColor(getContext(), R.color.azulito));
                    break;
                default:
                    statusCell.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }

            row.addView(statusCell);
            tableLayout.addView(row);

            // Agregar divisor entre filas
            View divider = new View(getContext());
            divider.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1));
            divider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.azulitoPrincipio));
            tableLayout.addView(divider);
        }

        activitiesContainer.addView(tableView);
    }

    // Método auxiliar mejorado para aceptar peso y tamaño de texto
    private void addDataCell(TableRow row, String text, float weight, int textSizeSp) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        textView.setPadding(4, 4, 4, 4); // Padding reducido
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setMaxLines(1);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                weight);
        textView.setLayoutParams(params);
        row.addView(textView);
    }

    // Métodos para formatear fecha y hora
    private String formatDate(String fecha) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(fecha);
            return outputFormat.format(date);
        } catch (Exception e) {
            return fecha; // Si falla el parseo, devolver la fecha original
        }
    }

    private String formatTime(String hora) {
        try {
            if (hora.length() > 8) hora = hora.substring(0, 8);
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date time = inputFormat.parse(hora);

            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(time);
        } catch (Exception e) {
            return hora.length() > 5 ? hora.substring(0, 5) : hora;
        }
    }
    private void setupMedicationsSection() {
        rvMedications = rootView.findViewById(R.id.rvMedications);
        rvMedications.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Medication> medications = new ArrayList<>();
        medications.add(new Medication("Paracetamol", "500mg", "Cada 8 horas"));
        medications.add(new Medication("Loratadina", "10mg", "Una vez al día"));
        medications.add(new Medication("Ibuprofeno", "400mg", "Cada 6 horas"));
        medications.add(new Medication("Amoxicilina", "250mg", "Cada 12 horas"));
        medications.add(new Medication("Omeprazol", "20mg", "Antes del desayuno"));

        rvMedications.setAdapter(new MedicationAdapter(medications));
    }

    private void navigateToAddPatient() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new RegistrarPaciente());
        transaction.addToBackStack("dashboard");
        transaction.commit();
    }

    // Clases internas para medicamentos
    static class Medication {
        String name, dosage, schedule;

        Medication(String name, String dosage, String schedule) {
            this.name = name;
            this.dosage = dosage;
            this.schedule = schedule;
        }
    }

    static class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
        private final List<Medication> medications;

        MedicationAdapter(List<Medication> medications) {
            this.medications = medications;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDosage, tvSchedule;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMedicationName);
                tvDosage = itemView.findViewById(R.id.tvDosage);
                tvSchedule = itemView.findViewById(R.id.tvSchedule);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Medication med = medications.get(position);
            holder.tvName.setText(med.name);
            holder.tvDosage.setText(med.dosage);
            holder.tvSchedule.setText(med.schedule);
        }

        @Override
        public int getItemCount() {
            return medications.size();
        }
    }
}