package com.sena.qfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sena.qfinder.model.ManagerDB;

import java.util.ArrayList;
import java.util.HashMap;

public class GestionPacienteFragment extends Fragment {

    private LinearLayout patientsContainer;
    private ManagerDB managerDB;

    public GestionPacienteFragment() {
        // Constructor vacío requerido
    }

    public static GestionPacienteFragment newInstance(String param1, String param2) {
        GestionPacienteFragment fragment = new GestionPacienteFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        managerDB = new ManagerDB(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gestion_paciente, container, false);
        setupPatientsSection(inflater, root);
        return root;
    }

    private void setupPatientsSection(LayoutInflater inflater, View root) {
        patientsContainer = root.findViewById(R.id.containerPacientes);
        patientsContainer.removeAllViews();

        ArrayList<HashMap<String, String>> pacientes = managerDB.obtenerPacientes();
        for (HashMap<String, String> paciente : pacientes) {
            String nombreCompleto = paciente.get("nombres") + " " + paciente.get("apellidos");
            String edad = paciente.get("identificacion");
            String diagnostico = paciente.get("diagnostico");

            // Inflar la tarjeta de cada paciente
            View card = inflater.inflate(R.layout.item_paciente, patientsContainer, false);

            TextView nombreTextView = card.findViewById(R.id.nombrePaciente);
            TextView edadTextView = card.findViewById(R.id.edadPaciente);
            TextView enfermedadTextView = card.findViewById(R.id.enfermedadPaciente);
            ImageView fotoPaciente = card.findViewById(R.id.imagenPaciente);

            nombreTextView.setText(nombreCompleto);
            edadTextView.setText(edad);
            enfermedadTextView.setText(diagnostico);
            fotoPaciente.setImageResource(R.drawable.perfil_paciente);

            // Navegar al perfil del paciente al hacer clic
            card.setOnClickListener(v -> {
                try {
                    int pacienteId = Integer.parseInt(paciente.get("id")); // Aquí usamos el id correcto
                    PerfilPaciente perfilFragment = PerfilPaciente.newInstance(pacienteId); // Pasamos el id

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, perfilFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } catch (NumberFormatException e) {
                    Log.e("GestionPacienteFragment", "ID inválido: " + paciente.get("id"));
                }
            });

            patientsContainer.addView(card);
        }

        // Configurar botón de agregar paciente
        ImageView addButton = root.findViewById(R.id.PatientAdd);
        if (addButton != null) {
            addButton.setOnClickListener(v -> navigateToRegisterPatient());
        } else {
            Log.e("GestionPacienteFragment", "No se encontró el ImageView con id PatientAdd.");
        }
    }

    private void navigateToRegisterPatient() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new RegistrarPaciente());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
