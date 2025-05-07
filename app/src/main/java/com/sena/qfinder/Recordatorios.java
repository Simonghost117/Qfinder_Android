package com.example.qfinder.ui.recordatorios;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import com.example.qfinder.R;

public class RecordatoriosFragment extends Fragment {

    private CalendarView calendarView;
    private LinearLayout layoutRecordatorios;
    private Button btnAgregar;
    private long fechaSeleccionada;

    public RecordatoriosFragment() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_recordatorios, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        layoutRecordatorios = view.findViewById(R.id.layout_recordatorios);
        btnAgregar = view.findViewById(R.id.btn_agregar_recordatorio);

        fechaSeleccionada = calendarView.getDate();

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Actualizar fechaSeleccionada y cargar recordatorios correspondientes
        });

        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        return view;
    }

    private void mostrarDialogoAgregar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_agregar_recordatorio, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Configurar elementos del diálogo y manejar eventos

        dialog.show();
    }
}
