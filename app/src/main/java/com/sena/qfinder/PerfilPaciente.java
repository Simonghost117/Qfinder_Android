package com.sena.qfinder;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sena.qfinder.model.ManagerDB;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PerfilPaciente#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilPaciente extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Variables necesarias para conexin con la BD
    private static final String ARG_PACIENTE_ID = "id";
    private int pacienteId = -1; // Inicializar con un valor por defecto

    //Inicializacion de variables de TextView
    TextView tvNombreApellido, tvFechaNacimiento, tvSexo, tvDiagnostico, tvIdentificacion;

    //Instancia de ManagerDB
    private ManagerDB managerDB;

    public PerfilPaciente() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilPaciente.
     */
    // TODO: Rename and change types and number of parameters
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
            Log.d("debugger", "idPaciente: "+pacienteId);
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Inicializa ManagerDB aquí, asegurándote de que el Context no sea nulo
        Context context = getContext();
        if (context != null) {
            managerDB = new ManagerDB(context);
        } else {
            // Manejar el caso en que el Context es nulo (puede ocurrir en etapas tempranas del ciclo de vida)
            // Quizás mostrar un mensaje de error o evitar operaciones de base de datos.
            managerDB = null; // Para evitar NullPointerException más tarde
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_paciente, container, false);

        //Asignacion de elementos de XML a variables de JAVA
        tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento);
        tvSexo = view.findViewById(R.id.tvSexo);
        tvDiagnostico = view.findViewById(R.id.tvDiagnostico);
        tvIdentificacion = view.findViewById(R.id.tvIdentificacion);

        if (managerDB != null) {
            mostrarInformacionPaciente();
        } else {
            // Manejar el caso en que managerDB no se inicializó (Context nulo)
            tvNombreApellido.setText("Error al cargar el perfil");
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void mostrarInformacionPaciente() {
        if (pacienteId != -1 && managerDB != null) {
            HashMap<String, String> paciente = managerDB.obtenerPaciente(pacienteId);
            Log.d("debugger", "Paciente: "+ paciente);
            if (paciente != null) {
                tvNombreApellido.setText(paciente.get("nombres") + " " + paciente.get("apellidos"));
                tvFechaNacimiento.setText(paciente.get("fechaNacimiento"));
                tvSexo.setText(paciente.get("sexo"));
                tvDiagnostico.setText(paciente.get("diagnostico"));
                tvIdentificacion.setText(paciente.get("identificacion"));
            } else {
                // Manejar el caso en que no se encuentra el paciente con ese ID

                tvNombreApellido.setText("Paciente no encontrado");
            }
        } else {
            // Manejar el caso en que no se proporcionó un ID o managerDB es nulo
            if (pacienteId == -1) {
                tvNombreApellido.setText("ID de paciente no proporcionado");
            } else if (managerDB == null) {
                tvNombreApellido.setText("Error al acceder a la base de datos");
            }
        }
    }
}