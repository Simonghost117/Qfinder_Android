package com.sena.qfinder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AgregarActividadDialogFragment extends DialogFragment {

    private Spinner spinnerPacientes, spinnerFrecuencia, spinnerRecordarAntes;
    private EditText editDescripcion;
    private Button btnGuardar;
    private TextView tvFecha, tvHora;

    private String fechaSeleccionada = "";
    private String horaSeleccionada = "";

    private Actividad actividadExistente;
    private final String BASE_URL = "https://qfinder-production.up.railway.app/";

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

        cargarPacientesDesdeAPI();
        cargarFrecuenciaYRecordatorio();

        tvFecha.setOnClickListener(v -> mostrarDatePicker());
        tvHora.setOnClickListener(v -> mostrarTimePicker());

        if (actividadExistente != null) {
            fechaSeleccionada = actividadExistente.getFecha();
            horaSeleccionada = actividadExistente.getHora();
            editDescripcion.setText(actividadExistente.getDescripcion());

            tvFecha.setText("Fecha: " + fechaSeleccionada);
            tvHora.setText("Hora: " + horaSeleccionada);

            String recordatorio = actividadExistente.getRecordarAntes();
            ArrayAdapter<CharSequence> adapterRecordar = (ArrayAdapter<CharSequence>) spinnerRecordarAntes.getAdapter();
            if (adapterRecordar != null) {
                int spinnerPosRecordar = adapterRecordar.getPosition(recordatorio);
                if (spinnerPosRecordar >= 0) {
                    spinnerRecordarAntes.setSelection(spinnerPosRecordar);
                }
            }

            String frecuencia = actividadExistente.getRepetirCada();
            ArrayAdapter<CharSequence> adapterFrecuencia = (ArrayAdapter<CharSequence>) spinnerFrecuencia.getAdapter();
            if (adapterFrecuencia != null) {
                int spinnerPosFrecuencia = adapterFrecuencia.getPosition(frecuencia);
                if (spinnerPosFrecuencia >= 0) {
                    spinnerFrecuencia.setSelection(spinnerPosFrecuencia);
                }
            }
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

    private void cargarPacientesDesdeAPI() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService authService = retrofit.create(AuthService.class);
        Call<PacienteListResponse> call = authService.listarPacientes("Bearer " + token);

        List<String> nombresPacientes = new ArrayList<>();
        nombresPacientes.add("Seleccione un paciente");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nombresPacientes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPacientes.setAdapter(adapter);

        call.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(Call<PacienteListResponse> call, Response<PacienteListResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<PacienteResponse> pacientes = response.body().getData();

                    nombresPacientes.clear();
                    nombresPacientes.add("Seleccione un paciente");

                    if (pacientes != null && !pacientes.isEmpty()) {
                        for (PacienteResponse paciente : pacientes) {
                            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
                            nombresPacientes.add(nombreCompleto);
                        }
                    } else {
                        Toast.makeText(getContext(), "No hay pacientes registrados", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();

                    if (actividadExistente != null) {
                        String pacienteExistente = actividadExistente.getPaciente();
                        int pos = nombresPacientes.indexOf(pacienteExistente);
                        if (pos >= 0) {
                            spinnerPacientes.setSelection(pos);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PacienteListResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Error de conexión", t);
                Toast.makeText(getContext(), "Error de conexión al cargar pacientes", Toast.LENGTH_SHORT).show();
            }
        });
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