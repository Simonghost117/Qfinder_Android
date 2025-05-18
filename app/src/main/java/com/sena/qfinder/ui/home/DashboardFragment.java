package com.sena.qfinder.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.PerfilPaciente;
import com.sena.qfinder.R;
import com.sena.qfinder.RegistrarPaciente;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;
import com.sena.qfinder.models.PerfilUsuarioResponse;

import java.util.ArrayList;
import java.util.List;

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

    private final String BASE_URL = "https://qfinder-production.up.railway.app/";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sharedPreferences = requireContext().getSharedPreferences("prefs_qfinder", Context.MODE_PRIVATE);

        tvUserName = root.findViewById(R.id.tvUserName);
        setupUserInfo();

        setupPatientsSection(inflater, root);
        setupActivitiesSection(inflater, root);
        setupMedicationsSection(root);

        return root;
    }

    private void setupUserInfo() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            tvUserName.setText("Usuario desconocido");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

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

    private void setupPatientsSection(LayoutInflater inflater, View root) {
        patientsContainer = root.findViewById(R.id.patientsContainer);
        patientsContainer.removeAllViews();

        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            }
            // Mostrar solo la tarjeta de agregar si no hay token
            mostrarPacientes(inflater, new ArrayList<>());
            return;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
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
                            mostrarPacientes(inflater, pacientes != null ? pacientes : new ArrayList<>());

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
                        // Mostrar solo la tarjeta de agregar si hay error
                        mostrarPacientes(inflater, new ArrayList<>());
                    }
                } catch (Exception e) {
                    Log.e("API", "Error procesando respuesta", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error procesando datos", Toast.LENGTH_SHORT).show();
                    }
                    // Mostrar solo la tarjeta de agregar si hay excepción
                    mostrarPacientes(inflater, new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PacienteListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Fallo en la conexión", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // Mostrar solo la tarjeta de agregar si falla la conexión
                mostrarPacientes(inflater, new ArrayList<>());
            }
        });
    }

    private void mostrarPacientes(LayoutInflater inflater, List<PacienteResponse> pacientes) {
        patientsContainer.removeAllViews();

        // Añadir tarjetas de pacientes
        for (PacienteResponse paciente : pacientes) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";

            String relacion = paciente.isEs_cuidador_principal() ?
                    "Cuidador principal" :
                    (paciente.getParentesco() != null ?
                            paciente.getParentesco() : "Paciente");

            addPatientCard(inflater, nombreCompleto, relacion, diagnostico,
                    R.drawable.perfil_paciente, paciente.getId());
        }

        // Añadir tarjeta de agregar paciente al final
        View addCard = inflater.inflate(R.layout.item_add_patient_card, patientsContainer, false);
        addCard.setOnClickListener(v -> navigateToAddPatient());
        patientsContainer.addView(addCard);
    }

    private void addPatientCard(LayoutInflater inflater, String name, String relation, String conditions, int imageResId, int patientId) {
        View patientCard = inflater.inflate(R.layout.item_patient_card, patientsContainer, false);

        TextView tvName = patientCard.findViewById(R.id.tvPatientName);
        TextView tvRelation = patientCard.findViewById(R.id.tvPatientRelation);
        TextView tvConditions = patientCard.findViewById(R.id.tvPatientConditions);
        ImageView ivProfile = patientCard.findViewById(R.id.ivPatientProfile);

        tvName.setText(name);
        tvRelation.setText(relation);
        tvConditions.setText(conditions);
        ivProfile.setImageResource(imageResId);

        patientCard.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, PerfilPaciente.newInstance(patientId));
            transaction.addToBackStack("dashboard");
            transaction.commit();
        });

        patientsContainer.addView(patientCard);
    }
    private void setupActivitiesSection(LayoutInflater inflater, View root) {
        activitiesContainer = root.findViewById(R.id.activitiesContainer);
        activitiesContainer.removeAllViews();

        List<Activity> activities = new ArrayList<>();
        activities.add(new Activity("Cita médica", "Revisión mensual con el pediatra", "Pendiente", "10:30 AM"));
        activities.add(new Activity("Examen de sangre", "Laboratorio clínico", "Completado", "08:00 AM"));
        activities.add(new Activity("Consulta nutricional", "Control de peso", "Pendiente", "11:00 AM"));
        activities.add(new Activity("Vacunación", "Refuerzo antigripal", "Completado", "14:00 PM"));
        activities.add(new Activity("Chequeo dental", "Higiene bucal", "Pendiente", "09:30 AM"));

        for (Activity act : activities) {
            View actView = inflater.inflate(R.layout.item_activity_card, activitiesContainer, false);
            ((TextView) actView.findViewById(R.id.tvActivitiesTitle)).setText(act.title);
            ((TextView) actView.findViewById(R.id.tvActivityDescription)).setText(act.description);
            ((TextView) actView.findViewById(R.id.tvActivityStatus)).setText(act.status);
            ((TextView) actView.findViewById(R.id.tvActivityTime)).setText(act.time);
            activitiesContainer.addView(actView);
        }
    }

    private void setupMedicationsSection(View root) {
        rvMedications = root.findViewById(R.id.rvMedications);
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

    // Clases internas
    static class Activity {
        String title, description, status, time;

        Activity(String title, String description, String status, String time) {
            this.title = title;
            this.description = description;
            this.status = status;
            this.time = time;
        }
    }

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