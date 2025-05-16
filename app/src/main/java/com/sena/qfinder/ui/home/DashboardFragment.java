package com.sena.qfinder.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.PerfilPaciente;
import com.sena.qfinder.R;
import com.sena.qfinder.RegistrarPaciente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardFragment extends Fragment {

    private LinearLayout patientsContainer, activitiesContainer;
    private RecyclerView rvMedications;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sharedPreferences = requireContext().getSharedPreferences("prefs_qfinder", Context.MODE_PRIVATE);

        setupUserInfo(root);
        setupPatientsSection(inflater, root);
        setupActivitiesSection(inflater, root);
        setupMedicationsSection(root);

        return root;
    }

    private void setupUserInfo(View root) {
        TextView tvUserName = root.findViewById(R.id.tvUserName);

        // Datos quemados
        String nombre = "Juan";
        String apellido = "Pérez";
        String nombreCompleto = nombre + " " + apellido;

        tvUserName.setText(nombreCompleto);
    }

    private void setupPatientsSection(LayoutInflater inflater, View root) {
        patientsContainer = root.findViewById(R.id.patientsContainer);
        patientsContainer.removeAllViews();

        // Datos quemados para pacientes
        List<HashMap<String, String>> pacientes = new ArrayList<>();

        HashMap<String, String> paciente1 = new HashMap<>();
        paciente1.put("nombres", "Ana");
        paciente1.put("apellidos", "Gómez");
        paciente1.put("diagnostico", "Asma");
        paciente1.put("id", "1");

        HashMap<String, String> paciente2 = new HashMap<>();
        paciente2.put("nombres", "Luis");
        paciente2.put("apellidos", "Martínez");
        paciente2.put("diagnostico", "Diabetes tipo 1");
        paciente2.put("id", "2");

        pacientes.add(paciente1);
        pacientes.add(paciente2);

        for (HashMap<String, String> paciente : pacientes) {
            String nombreCompleto = paciente.get("nombres") + " " + paciente.get("apellidos");
            String diagnostico = paciente.get("diagnostico");
            int patientId = Integer.parseInt(paciente.get("id"));
            addPatientCard(inflater, nombreCompleto, "Paciente", diagnostico, R.drawable.perfil_paciente, patientId);
        }

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
