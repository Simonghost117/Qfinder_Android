package com.sena.qfinder;

import android.os.Bundle;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actividad1, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewActividades);
        btnAgregar = view.findViewById(R.id.btnAgregarActividad);

        adapter = new ActividadAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        actividadViewModel = new ViewModelProvider(requireActivity()).get(ActividadViewModel.class);

        actividadViewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            adapter.setActividades(actividades);
        });

        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar(null, -1));

        adapter.setOnItemClickListener(new ActividadAdapter.OnItemClickListener() {
            @Override
            public void onEditarClick(int position) {
                Actividad actividad = actividadViewModel.getActividades().getValue().get(position);
                mostrarDialogoAgregar(actividad, position);
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

        dialog.setOnActividadGuardadaListener(new AgregarActividadDialogFragment.OnActividadGuardadaListener() {
            @Override
            public void onActividadGuardada(Actividad nuevaActividad) {
                if (position >= 0) {
                    actividadViewModel.actualizarActividad(position, nuevaActividad);
                } else {
                    actividadViewModel.agregarActividad(nuevaActividad);
                }
            }
        });

        dialog.show(getParentFragmentManager(), "DialogoActividad");
    }
}