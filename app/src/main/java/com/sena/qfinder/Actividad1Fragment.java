package com.sena.qfinder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class Actividad1Fragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnAgregar;
    private ActividadAdapter adapter;
    private ActividadViewModel actividadViewModel;

    public Actividad1Fragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actividad1, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewActividades);
        btnAgregar = view.findViewById(R.id.btnAgregarActividad);

        adapter = new ActividadAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 🔁 Obtener el ViewModel compartido
        actividadViewModel = new ViewModelProvider(requireActivity()).get(ActividadViewModel.class);

        // 📌 Observar la lista de actividades
        actividadViewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            adapter.setActividades(actividades);
        });

        // Botón para agregar nueva actividad
        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar(null, -1));

        // Escuchar clics de editar y eliminar
        adapter.setOnItemClickListener(new ActividadAdapter.OnItemClickListener() {
            @Override
            public void onEditarClick(int position) {
                // Mostrar mensaje
                Toast.makeText(getContext(), "Actividad completada", Toast.LENGTH_SHORT).show();

                // Eliminar la actividad de la lista
                actividadViewModel.marcarActividadComoRealizada(position);
            }

            @Override
            public void onEliminarClick(int position) {
                actividadViewModel.eliminarActividad(position);
            }
        });

        return view;
    }

    private void mostrarDialogoAgregar(Actividad actividadExistente, int position) {
        AgregarActividadDialogFragment dialog = AgregarActividadDialogFragment.newInstance(actividadExistente);
        dialog.setOnActividadGuardadaListener(nuevaActividad -> {
            if (position >= 0) {
                actividadViewModel.marcarActividadComoRealizada(position);
            } else {
                actividadViewModel.agregarActividad(nuevaActividad);
            }
        });
        dialog.show(getParentFragmentManager(), "DialogoActividad");
    }
}
