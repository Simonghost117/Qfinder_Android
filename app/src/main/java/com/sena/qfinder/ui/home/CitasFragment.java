package com.sena.qfinder.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import com.sena.qfinder.R;
import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.CitaMedica;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CitasFragment extends Fragment {

    private RecyclerView recyclerViewCitas;
    private CitaAdapter citaAdapter;
    private List<CitaMedica> listaCitas = new ArrayList<>();
    private MaterialCalendarView calendarView;
    private Spinner spinnerPacientesalejo;


    private AuthService authService;
    private String token;
    private int idPacienteSeleccionado;

    private List<PacienteListResponse> listaPacientes = new ArrayList<>();

    public CitasFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas, container, false);

        recyclerViewCitas = view.findViewById(R.id.recyclerCitas);
        recyclerViewCitas.setLayoutManager(new LinearLayoutManager(getContext()));
        citaAdapter = new CitaAdapter(listaCitas);
        recyclerViewCitas.setAdapter(citaAdapter);

        calendarView = view.findViewById(R.id.calendarView);
        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);

        Button btnAgregar = view.findViewById(R.id.btnAgregarRecordatorio);
        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        spinnerPacientesalejo = view.findViewById(R.id.spinnerPacientesalejo);

        authService = ApiClient.getClient().create(AuthService.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (token != null) {
            obtenerPacientes();
        } else {
            Toast.makeText(getContext(), "No se encontró el token", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void obtenerPacientes() {
        authService.obtenerPacientes("Bearer " + token).enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(Call<PacienteListResponse> call, Response<PacienteListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Obtén la lista de pacientes desde el objeto de respuesta
                    List<PacienteResponse> listaPacientes = response.body().getData(); // Cambia esto para obtener la lista de pacientes
                    List<String> nombres = new ArrayList<>();
                    for (PacienteResponse paciente : listaPacientes) {
                        nombres.add(paciente.getNombre()); // Asegúrate de que PacienteResponse tenga el método getNombre()
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPacientesalejo.setAdapter(adapter);

                    spinnerPacientesalejo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // Accede al paciente seleccionado
                            PacienteResponse pacienteSeleccionado = listaPacientes.get(position);
                            idPacienteSeleccionado = pacienteSeleccionado.getId(); // Asegúrate de que PacienteResponse tenga el método getId()
                            obtenerCitasDesdeApi();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Toast.makeText(getContext(), "Error al obtener pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PacienteListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void mostrarDialogoAgregar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_recordatorio, null);
        builder.setView(dialogView);
        builder.setTitle("Nueva cita");

        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        EditText etTitulo = dialogView.findViewById(R.id.etTitulo);
        EditText etFecha = dialogView.findViewById(R.id.etFecha);
        CheckBox check1 = dialogView.findViewById(R.id.checkUnDiaAntes);
        CheckBox check2 = dialogView.findViewById(R.id.checkMismoDia);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        etFecha.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        String selectedDate = sdf.format(calendar.getTime());
                        etFecha.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        btnGuardar.setOnClickListener(v -> {
            String tipo = spinnerTipo.getSelectedItem().toString();
            String titulo = etTitulo.getText().toString();
            String fecha = etFecha.getText().toString();
            String descripcion = etDescripcion.getText().toString();
            boolean recordarUnDiaAntes = check1.isChecked();
            boolean recordarMismoDia = check2.isChecked();

            if (titulo.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(getContext(), "Completa el título y la fecha", Toast.LENGTH_SHORT).show();
                return;
            }

            CitaMedica nuevaCita = new CitaMedica();
            nuevaCita.setTitulo(titulo);
            nuevaCita.setDescripcion(descripcion);
            nuevaCita.setTipo(tipo);
            nuevaCita.setFechaCita(fecha);
            nuevaCita.setRecordar_un_dia_antes(recordarUnDiaAntes);
            nuevaCita.setRecordar_mismo_dia(recordarMismoDia);

            authService.crearCitaMedica("Bearer " + token, idPacienteSeleccionado, nuevaCita).enqueue(new Callback<CitaMedica>() {
                @Override
                public void onResponse(Call<CitaMedica> call, Response<CitaMedica> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        obtenerCitasDesdeApi();
                    } else {
                        Toast.makeText(getContext(), "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CitaMedica> call, Throwable t) {
                    Toast.makeText(getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void obtenerCitasDesdeApi() {
        Call<List<CitaMedica>> call = authService.listarCitasMedicas("Bearer " + token, idPacienteSeleccionado);

        call.enqueue(new Callback<List<CitaMedica>>() {
            @Override
            public void onResponse(@NonNull Call<List<CitaMedica>> call, @NonNull Response<List<CitaMedica>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCitas.clear();
                    listaCitas.addAll(response.body());
                    citaAdapter.notifyDataSetChanged();
                    marcarFechasEnCalendario();
                } else {
                    Toast.makeText(getContext(), "No se pudieron obtener las citas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CitaMedica>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void marcarFechasEnCalendario() {
        Set<CalendarDay> fechas = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (CitaMedica cita : listaCitas) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(cita.getFechaCita()));

                // ✅ USO SEGURO DEL MÉTODO CalendarDay.from
                CalendarDay day = CalendarDay.from(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );
                fechas.add(day);
            } catch (Exception e) {
                Log.e("CalendarParseError", "Fecha inválida: " + cita.getFechaCita(), e);
            }
        }

        calendarView.removeDecorators();
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return fechas.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new DotSpan(10, Color.GREEN));
            }
        });
    }
}
