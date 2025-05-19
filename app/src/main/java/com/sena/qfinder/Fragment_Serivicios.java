package com.sena.qfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Fragment_Serivicios extends Fragment {

    public Fragment_Serivicios() {
        // Constructor requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__serivicios, container, false);

        LinearLayout cardGestionPacientes = view.findViewById(R.id.cardGestionPacientes);
        cardGestionPacientes.setOnClickListener(v -> {
            // Reemplazar fragmento actual por GestionPacienteFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new GestionPacienteFragment()); // Asegúrate de que R.id.fragment_container exista
            transaction.addToBackStack(null); // Para poder volver atrás
            transaction.commit();
        });

        LinearLayout cardRegistroSalud = view.findViewById(R.id.registrosalud);
        cardRegistroSalud.setOnClickListener(v -> {
            // Reemplazar fragmento actual por GestionPacienteFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new registro_salud()); // Asegúrate de que R.id.fragment_container exista
            transaction.addToBackStack(null); // Para poder volver atrás
            transaction.commit();
        });




        return view;

    }
}
