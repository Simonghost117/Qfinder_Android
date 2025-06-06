package com.sena.qfinder.ui.cita;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.sena.qfinder.R;
import com.sena.qfinder.data.api.ApiClient;
import com.sena.qfinder.data.api.AuthService;
import com.sena.qfinder.data.models.CitaMedica;
import com.sena.qfinder.data.models.PacienteListResponse;
import com.sena.qfinder.data.models.PacienteResponse;

import org.threeten.bp.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CitasFragment extends Fragment {

    private static final String TAG = "CitasFragment";
    private MaterialCalendarView calendarView;
    private RecyclerView recyclerCitas;
    private LinearLayout patientsContainer;
    private CitaAdapter citaAdapter;
    private int selectedPatientId = -1;
    private String selectedPatientName = "";
    private Map<Integer, String> pacientesMap = new HashMap<>();
    private List<CitaMedica> todasLasCitas = new ArrayList<>();
    private LayoutInflater currentInflater;

    // Decoradores para diferentes estados de citas
    private EventDecorator decoratorCitasPendientes;
    private EventDecorator decoratorCitasCompletadas;
    private EventDecorator decoratorCitasCanceladas;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_citas, container, false);
        currentInflater = inflater;

        // Inicializar vistas
        calendarView = rootView.findViewById(R.id.calendarView);
        recyclerCitas = rootView.findViewById(R.id.recyclerCitas);
        patientsContainer = rootView.findViewById(R.id.patientsContainer);
        Button btnAgregarRecordatorio = rootView.findViewById(R.id.btnAgregarRecordatorio);

        // Configurar RecyclerView
        recyclerCitas.setLayoutManager(new LinearLayoutManager(getContext()));
        citaAdapter = new CitaAdapter(new ArrayList<>());
        recyclerCitas.setAdapter(citaAdapter);

        // Configurar listeners
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected && selectedPatientId != -1) {
                mostrarCitasParaFecha(date);
            }
        });

        // Listener para el botón de agregar recordatorio
        btnAgregarRecordatorio.setOnClickListener(v -> mostrarDialogoAgregarRecordatorio());

        // Cargar pacientes
        loadPacientes();

        return rootView;
    }

    private void loadPacientes() {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<PacienteListResponse> call = authService.listarPacientes("Bearer " + token);

        call.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PacienteListResponse> call, @NonNull Response<PacienteListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PacienteResponse> pacientes = response.body().getData();
                    if (pacientes != null && !pacientes.isEmpty()) {
                        mostrarPacientes(pacientes);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar pacientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PacienteListResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarPacientes(List<PacienteResponse> pacientes) {
        patientsContainer.removeAllViews();

        for (PacienteResponse paciente : pacientes) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";
            String imagenUrl = paciente.getImagen_paciente();

            addPatientCard(nombreCompleto, diagnostico, imagenUrl, paciente.getId());
        }
    }

    private void addPatientCard(String name, String conditions, String imagenUrl, int patientId) {
        View patientCard = currentInflater.inflate(R.layout.item_patient_card, patientsContainer, false);
        patientCard.setTag(patientId);

        TextView tvName = patientCard.findViewById(R.id.tvPatientName);
        TextView tvConditions = patientCard.findViewById(R.id.tvPatientConditions);
        ImageView ivProfile = patientCard.findViewById(R.id.ivPatientProfile);

        tvName.setText(name);
        tvConditions.setText(conditions != null && !conditions.isEmpty() ? "• " + conditions : "• Sin diagnóstico");

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imagenUrl)
                    .placeholder(R.drawable.perfil_familiar) // Imagen por defecto
                    .error(R.drawable.perfil_familiar) // Imagen si hay error
                    .circleCrop() // Para hacerla circular
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.perfil_familiar);
        }


        patientCard.setOnClickListener(v -> {
            selectedPatientId = patientId;
            selectedPatientName = name;
            updatePatientCardsHighlight(patientId);
            loadCitasDelPaciente(patientId);
            Toast.makeText(getContext(), "Mostrando citas de " + name, Toast.LENGTH_SHORT).show();
        });

        patientsContainer.addView(patientCard);
    }

    private void updatePatientCardsHighlight(int selectedId) {
        for (int i = 0; i < patientsContainer.getChildCount(); i++) {
            View child = patientsContainer.getChildAt(i);
            if (child.getTag() instanceof Integer) {
                int patientId = (int) child.getTag();
                if (patientId == selectedId) {
                    child.setBackgroundColor(Color.parseColor("#E3F2FD"));
                } else {
                    child.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    private void loadCitasDelPaciente(int pacienteId) {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<List<CitaMedica>> call = authService.listarCitasMedicas("Bearer " + token, pacienteId);

        call.enqueue(new Callback<List<CitaMedica>>() {
            @Override
            public void onResponse(@NonNull Call<List<CitaMedica>> call, @NonNull Response<List<CitaMedica>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todasLasCitas = response.body();
                    Log.d(TAG, "Citas recibidas: " + todasLasCitas.size());
                    if (todasLasCitas != null && !todasLasCitas.isEmpty()) {
                        marcarDiasConCitasEnCalendario();
                        mostrarCitasParaFecha(calendarView.getSelectedDate());
                    } else {
                        todasLasCitas = new ArrayList<>();
                        limpiarMarcadoresCalendario();
                        citaAdapter.updateData(new ArrayList<>());
                        Toast.makeText(getContext(), "No hay citas programadas para este paciente", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                    todasLasCitas = new ArrayList<>();
                    limpiarMarcadoresCalendario();
                    citaAdapter.updateData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CitaMedica>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                todasLasCitas = new ArrayList<>();
                limpiarMarcadoresCalendario();
                citaAdapter.updateData(new ArrayList<>());
            }
        });
    }

    private void marcarDiasConCitasEnCalendario() {
        limpiarMarcadoresCalendario();

        List<CalendarDay> fechasPendientes = new ArrayList<>();
        List<CalendarDay> fechasCompletadas = new ArrayList<>();
        List<CalendarDay> fechasCanceladas = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (CitaMedica cita : todasLasCitas) {
            if (cita.getFechaCita() != null && !cita.getFechaCita().isEmpty()) {
                try {
                    Date date = sdf.parse(cita.getFechaCita());
                    if (date != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        LocalDate localDate = LocalDate.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                        );
                        CalendarDay calendarDay = CalendarDay.from(localDate);

                        String estado = cita.getEstadoCita() != null ? cita.getEstadoCita().toLowerCase() : "";
                        switch (estado) {
                            case "completada":
                                fechasCompletadas.add(calendarDay);
                                break;
                            case "cancelada":
                                fechasCanceladas.add(calendarDay);
                                break;
                            default: // pendiente u otros
                                fechasPendientes.add(calendarDay);
                                break;
                        }
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error al parsear fecha: " + cita.getFechaCita(), e);
                }
            }
        }

        // Asegúrate de usar colores contrastantes
        if (!fechasPendientes.isEmpty()) {
            decoratorCitasPendientes = new EventDecorator(Color.RED, fechasPendientes); // Cambiado a rojo para prueba
            calendarView.addDecorator(decoratorCitasPendientes);
            Log.d(TAG, "Decorador pendiente agregado para " + fechasPendientes.size() + " fechas");
        }

        if (!fechasCompletadas.isEmpty()) {
            decoratorCitasCompletadas = new EventDecorator(Color.GREEN, fechasCompletadas); // Cambiado a verde
            calendarView.addDecorator(decoratorCitasCompletadas);
        }

        if (!fechasCanceladas.isEmpty()) {
            decoratorCitasCanceladas = new EventDecorator(Color.BLACK, fechasCanceladas); // Cambiado a negro
            calendarView.addDecorator(decoratorCitasCanceladas);
        }

        // Forzar actualización
        calendarView.invalidateDecorators();
        Log.d(TAG, "Decoradores invalidados, forzando refresco");
    }

    private void limpiarMarcadoresCalendario() {
        calendarView.removeDecorators();
        decoratorCitasPendientes = null;
        decoratorCitasCompletadas = null;
        decoratorCitasCanceladas = null;
    }

    private void mostrarCitasParaFecha(CalendarDay date) {
        if (date == null || todasLasCitas.isEmpty()) {
            citaAdapter.updateData(new ArrayList<>());
            return;
        }

        String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                date.getYear(), date.getMonth() + 1, date.getDay());

        List<CitaMedica> citasParaFecha = new ArrayList<>();
        for (CitaMedica cita : todasLasCitas) {
            if (cita.getFechaCita() != null && cita.getFechaCita().startsWith(fechaSeleccionada)) {
                citasParaFecha.add(cita);
            }
        }

        citaAdapter.updateData(citasParaFecha);

        if (citasParaFecha.isEmpty()) {
            Toast.makeText(getContext(), "No hay citas para esta fecha", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoAgregarRecordatorio() {
        if (selectedPatientId == -1) {
            Toast.makeText(getContext(), "Por favor selecciona un paciente primero", Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Por favor selecciona una fecha en el calendario", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_agregar_recordatorio);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView tvTituloDialogo = dialog.findViewById(R.id.tvTituloDialogo);
        EditText etTitulo = dialog.findViewById(R.id.etTitulo);
        EditText etDescripcion = dialog.findViewById(R.id.etDescripcion);
        Spinner spinnerEstado = dialog.findViewById(R.id.spinnerEstado);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.estados_cita, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        tvTituloDialogo.setText("Nueva cita para " + selectedPatientName);

        btnGuardar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String estado = spinnerEstado.getSelectedItem().toString().toLowerCase();

            if (titulo.isEmpty()) {
                etTitulo.setError("El título es obligatorio");
                return;
            }

            if (titulo.length() < 5 || titulo.length() > 100) {
                etTitulo.setError("El título debe tener entre 5 y 100 caracteres");
                return;
            }

            String fechaCita = String.format(Locale.getDefault(), "%04d-%02d-%02dT00:00:00Z",
                    selectedDate.getYear(), selectedDate.getMonth() + 1, selectedDate.getDay());

            String fechaRecordatorio = String.format(Locale.getDefault(), "%04d-%02d-%02dT23:00:00Z",
                    selectedDate.getYear(), selectedDate.getMonth() + 1, selectedDate.getDay() - 1);

            CitaMedica nuevaCita = new CitaMedica();
            nuevaCita.setTitulo(titulo);
            nuevaCita.setDescripcion(descripcion);
            nuevaCita.setFechaCita(fechaCita);
            nuevaCita.setFechaRecordatorio(fechaRecordatorio);
            nuevaCita.setEstadoCita(estado);
            nuevaCita.setIdPaciente(selectedPatientId);

            guardarCita(nuevaCita);
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void guardarCita(CitaMedica cita) {
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);

        Call<CitaMedica> call = authService.crearCitaMedica("Bearer " + token, cita.getIdPaciente(), cita);

        call.enqueue(new Callback<CitaMedica>() {
            @Override
            public void onResponse(@NonNull Call<CitaMedica> call, @NonNull Response<CitaMedica> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();
                    loadCitasDelPaciente(cita.getIdPaciente());
                } else {
                    Toast.makeText(getContext(), "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CitaMedica> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class EventDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> dates;
        private final float dotRadius;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
            this.dotRadius = 12f;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            // Aumenta el tamaño del punto y usa un color más visible
            view.addSpan(new DotSpan(10, color)); // Tamaño aumentado a 10
        }
    }
}