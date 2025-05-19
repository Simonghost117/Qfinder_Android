package com.sena.qfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sena.qfinder.model.Registro;

import java.text.SimpleDateFormat;
import java.util.*;

public class CuidadosFragment extends Fragment {

    private List<Registro> registros = new ArrayList<>();
    private RegistroAdapter adapter;
    private TextView txtCantidad;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cuidados, container, false);

        RecyclerView recycler = root.findViewById(R.id.recyclerRegistros);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        txtCantidad = root.findViewById(R.id.txtCantidad);
        Spinner spinner = root.findViewById(R.id.spinnerOrden);
        FloatingActionButton fab = root.findViewById(R.id.fabAgregar);

        // Datos simulados
        agregarDatosFicticios();

        adapter = new RegistroAdapter(registros);
        recycler.setAdapter(adapter);
        txtCantidad.setText(registros.size() + " Registro");

        // Ordenar por spinner
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Collections.sort(registros, Comparator.comparingLong(Registro::getTimestamp));
                } else {
                    Collections.sort(registros, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                }
                adapter.actualizar(registros);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Botón flotante
        fab.setOnClickListener(v -> {
            // Acción al presionar
        });

        return root;
    }

    private void agregarDatosFicticios() {
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM hh:mm a", new Locale("es", "ES"));
        registros.add(new Registro("Registro de cuidados", sdf.format(new Date(now)), now));
    }
}
