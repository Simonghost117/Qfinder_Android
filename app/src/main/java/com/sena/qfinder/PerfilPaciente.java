package com.sena.qfinder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sena.qfinder.model.ManagerDB;
import com.sena.qfinder.ui.home.DashboardFragment;

import java.util.HashMap;

public class PerfilPaciente extends Fragment {

    private static final String ARG_PACIENTE_ID = "id";

    private int pacienteId = -1;
    private ManagerDB managerDB;

    private TextView tvNombreApellido, tvFechaNacimiento, tvSexo, tvDiagnostico, tvIdentificacion;
    private ImageView btnBack;

    public PerfilPaciente() {
        // Required empty public constructor
    }

    public static PerfilPaciente newInstance(int pacienteId) {
        PerfilPaciente fragment = new PerfilPaciente();
        Bundle args = new Bundle();
        args.putInt(ARG_PACIENTE_ID, pacienteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pacienteId = getArguments().getInt(ARG_PACIENTE_ID, -1);
            Log.d("PerfilPaciente", "ID del paciente recibido: " + pacienteId);
        }

        Context context = getContext();
        if (context != null) {
            managerDB = new ManagerDB(context);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_paciente, container, false);

        // Referencias UI
        tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento);
        tvSexo = view.findViewById(R.id.tvSexo);
        tvDiagnostico = view.findViewById(R.id.tvDiagnostico);
        tvIdentificacion = view.findViewById(R.id.tvIdentificacion);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> volverADashboard());

        if (managerDB != null) {
            mostrarInformacionPaciente();
        } else {
            tvNombreApellido.setText("Error al cargar datos");
        }

        return view;
    }

    private void mostrarInformacionPaciente() {
        if (pacienteId == -1) {
            tvNombreApellido.setText("ID de paciente no proporcionado");
            return;
        }

        HashMap<String, String> paciente = managerDB.obtenerPaciente(pacienteId);

        if (paciente != null) {
            tvNombreApellido.setText(paciente.get("nombres") + " " + paciente.get("apellidos"));
            tvFechaNacimiento.setText(paciente.get("fechaNacimiento"));
            tvSexo.setText(paciente.get("sexo"));
            tvDiagnostico.setText(paciente.get("diagnostico"));
            tvIdentificacion.setText(paciente.get("identificacion"));
        } else {
            tvNombreApellido.setText("Paciente no encontrado");
        }
    }

    private void volverADashboard() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
