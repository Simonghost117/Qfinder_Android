package com.sena.qfinder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AgregarActividadDialogFragment extends DialogFragment {

    private Spinner spinnerPacientes, spinnerFrecuencia, spinnerRecordarAntes;
    private EditText editDescripcion;
    private Button btnGuardar;
    private TextView tvFecha, tvHora;

    private String fechaSeleccionada = "";
    private String horaSeleccionada = "";

    private Actividad actividadExistente;

    public static AgregarActividadDialogFragment newInstance(Actividad actividad) {
        AgregarActividadDialogFragment fragment = new AgregarActividadDialogFragment();
        fragment.actividadExistente = actividad;
        return fragment;
    }

    public interface OnActividadGuardadaListener {
        void onActividadGuardada(Actividad actividad);
    }

    private OnActividadGuardadaListener listener;

    public void setOnActividadGuardadaListener(OnActividadGuardadaListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_agregar_actividad, null);

        spinnerPacientes = view.findViewById(R.id.spinnerPaciente);
        spinnerFrecuencia = view.findViewById(R.id.spinnerFrecuencia);
        spinnerRecordarAntes = view.findViewById(R.id.spinnerRecordarAntes);
        editDescripcion = view.findViewById(R.id.etDescripcion);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        tvFecha = view.findViewById(R.id.tvFecha);
        tvHora = view.findViewById(R.id.tvHora);

        cargarPacientesDesdeBD();
        cargarFrecuenciaYRecordatorio();

        tvFecha.setOnClickListener(v -> mostrarDatePicker());
        tvHora.setOnClickListener(v -> mostrarTimePicker());

        if (actividadExistente != null) {
            fechaSeleccionada = actividadExistente.getFecha();
            horaSeleccionada = actividadExistente.getHora();
            editDescripcion.setText(actividadExistente.getDescripcion());

            ((TextView) tvFecha).setText("Fecha: " + fechaSeleccionada);
            ((TextView) tvHora).setText("Hora: " + horaSeleccionada);

            String recordatorio = actividadExistente.getRecordarAntes();
            ArrayAdapter<CharSequence> adapterRecordar = (ArrayAdapter<CharSequence>) spinnerRecordarAntes.getAdapter();
            int spinnerPosRecordar = adapterRecordar.getPosition(recordatorio);
            spinnerRecordarAntes.setSelection(spinnerPosRecordar);

            String frecuencia = actividadExistente.getRepetirCada();
            ArrayAdapter<CharSequence> adapterFrecuencia = (ArrayAdapter<CharSequence>) spinnerFrecuencia.getAdapter();
            int spinnerPosFrecuencia = adapterFrecuencia.getPosition(frecuencia);
            spinnerFrecuencia.setSelection(spinnerPosFrecuencia);
        }

        btnGuardar.setOnClickListener(v -> {
            String descripcion = editDescripcion.getText().toString().trim();

            Object pacienteObj = spinnerPacientes.getSelectedItem();
            if (pacienteObj == null || pacienteObj.toString().equals("Seleccione un paciente")) {
                Toast.makeText(getContext(), "Por favor seleccione un paciente válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(getContext(), "Por favor seleccione una fecha", Toast.LENGTH_SHORT).show();
                return;
            }

            if (horaSeleccionada.isEmpty()) {
                Toast.makeText(getContext(), "Por favor seleccione una hora", Toast.LENGTH_SHORT).show();
                return;
            }

            if (descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show();
                return;
            }

            String paciente = pacienteObj.toString();

            String repetirCada = spinnerFrecuencia.getSelectedItem() != null
                    ? spinnerFrecuencia.getSelectedItem().toString()
                    : "";
            if (repetirCada.equals("Seleccione frecuencia")) {
                Toast.makeText(getContext(), "Por favor seleccione una frecuencia", Toast.LENGTH_SHORT).show();
                return;
            }

            String recordarAntes = spinnerRecordarAntes.getSelectedItem() != null
                    ? spinnerRecordarAntes.getSelectedItem().toString()
                    : "";
            if (recordarAntes.equals("Seleccione recordatorio")) {
                Toast.makeText(getContext(), "Por favor seleccione un recordatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            Actividad nueva = new Actividad(paciente, fechaSeleccionada, horaSeleccionada, descripcion, recordarAntes, repetirCada);

            if (listener != null) {
                listener.onActividadGuardada(nueva);
            }

            dismiss();
        });

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(view);
        return dialog;
    }

    // Ajusta tamaño del diálogo
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.8)
            );
        }
    }

    private void cargarFrecuenciaYRecordatorio() {
        ArrayAdapter<CharSequence> adapterFrecuencia = ArrayAdapter.createFromResource(
                getContext(),
                R.array.frecuencia_opciones,
                android.R.layout.simple_spinner_item
        );
        adapterFrecuencia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrecuencia.setAdapter(adapterFrecuencia);

        ArrayAdapter<CharSequence> adapterRecordatorio = ArrayAdapter.createFromResource(
                getContext(),
                R.array.recordatorio_opciones,
                android.R.layout.simple_spinner_item
        );
        adapterRecordatorio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordarAntes.setAdapter(adapterRecordatorio);
    }

    private void cargarPacientesDesdeBD() {
        SQLiteDatabase db = getContext().openOrCreateDatabase("QfinderAndroid2", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT nombres, apellidos FROM Paciente", null);

        List<String> nombresPacientes = new ArrayList<>();
        nombresPacientes.add("Seleccione un paciente");

        if (cursor.moveToFirst()) {
            do {
                String nombreCompleto = cursor.getString(0) + " " + cursor.getString(1);
                nombresPacientes.add(nombreCompleto);
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(getContext(), "No hay pacientes registrados", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nombresPacientes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPacientes.setAdapter(adapter);
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getContext(), (view, y, m, d) -> {
            fechaSeleccionada = d + "/" + (m + 1) + "/" + y;
            tvFecha.setText("Fecha: " + fechaSeleccionada);
        }, year, month, day).show();
    }

    private void mostrarTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(getContext(), (view, h, m) -> {
            horaSeleccionada = String.format("%02d:%02d", h, m);
            tvHora.setText("Hora: " + horaSeleccionada);
        }, hour, minute, true).show();
    }
}
