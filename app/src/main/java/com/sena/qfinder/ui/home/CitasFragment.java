package com.sena.qfinder.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.sena.qfinder.R;
import com.sena.qfinder.ui.home.CitaAdapter;
import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.CitaMedica;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;



public class CitasFragment extends Fragment {

    private RecyclerView recyclerViewCitas;
    private CitaAdapter citaAdapter;
    private List<CitaMedica> listaCitas = new ArrayList<>();
    private MaterialCalendarView calendarView;

    private AuthService authService;
    private String token;
    private int idPacienteSeleccionado;

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

        Button btnAgregar = view.findViewById(R.id.btnAgregarRecordatorio);
        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        authService = ApiClient.getClient().create(AuthService.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);
        idPacienteSeleccionado = prefs.getInt("id_paciente", -1);

        if (token != null && idPacienteSeleccionado != -1) {
            obtenerCitasDesdeApi();
        } else {
            Toast.makeText(getContext(), "No se encontró el token o ID del paciente", Toast.LENGTH_SHORT).show();
        }

        return view;
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
            nuevaCita.setFecha(fecha);
            nuevaCita.setRecordar_un_dia_antes(recordarUnDiaAntes);
            nuevaCita.setRecordar_mismo_dia(recordarMismoDia);

            authService.crearCita("Bearer " + token, idPacienteSeleccionado, nuevaCita).enqueue(new Callback<CitaMedica>() {
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
                cal.setTime(sdf.parse(cita.getFecha()));
                fechas.add(CalendarDay.from(cal));
            } catch (Exception e) {
                e.printStackTrace();
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
