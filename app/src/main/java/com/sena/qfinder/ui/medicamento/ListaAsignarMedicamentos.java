package com.sena.qfinder.ui.medicamento;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sena.qfinder.R;
import com.sena.qfinder.data.api.ApiClient;
import com.sena.qfinder.data.api.AuthService;
import com.sena.qfinder.data.models.AsignarMedicamentoRequest;
import com.sena.qfinder.data.models.AsignarMedicamentoResponse;
import com.sena.qfinder.data.models.MedicamentoResponse;
import com.sena.qfinder.data.models.PacienteListResponse;
import com.sena.qfinder.data.models.PacienteMedicamento;
import com.sena.qfinder.data.models.PacienteResponse;
import com.sena.qfinder.data.models.AsignacionMedicamentoResponse;
import com.sena.qfinder.database.DatabaseHelper;
import com.sena.qfinder.database.entity.AlarmaEntity;
import com.sena.qfinder.ui.home.DashboardFragment;
import com.sena.qfinder.ui.home.Fragment_Serivicios;
import com.sena.qfinder.utils.ActivityAlarmReceiver;
import com.sena.qfinder.utils.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ListaAsignarMedicamentos extends Fragment {

    private Button btnOpenModalAsignar;
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormatter, timeFormatter;
    private LinearLayout patientsContainer, medicamentosContainer;
    private Spinner spinnerPatientsMain;
    private ImageView btnBack;
    private SharedPreferences sharedPreferences;
    private Map<Integer, String> pacientesMap = new HashMap<>();
    private Map<Integer, String> medicamentosMap = new HashMap<>();
    private int selectedPatientId = -1;
    private String selectedPatientName = "";
    private ProgressBar progressBar;
    private TimePickerDialog timePickerDialog;
    private TextView tvStartTime;

    private Call<PacienteListResponse> pacientesCall;
    private Call<List<AsignacionMedicamentoResponse>> medicamentosCall;
    private Call<AsignarMedicamentoResponse> asignarMedicamentoCall;
    private Call<List<MedicamentoResponse>> listarMedicamentosCall;

    public ListaAsignarMedicamentos() {
    }

    public static ListaAsignarMedicamentos newInstance() {
        return new ListaAsignarMedicamentos();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 7);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_asignar_medicamentos, container, false);

        btnOpenModalAsignar = view.findViewById(R.id.btnOpenModalAsignar);
        btnBack = view.findViewById(R.id.btnBack);
        spinnerPatientsMain = view.findViewById(R.id.spinner_patients_main);
        medicamentosContainer = view.findViewById(R.id.medicamentosContainer);
        progressBar = view.findViewById(R.id.progressBar);

        spinnerPatientsMain.setVisibility(View.GONE);
        setupPatientsSection(view, inflater);

        btnOpenModalAsignar.setOnClickListener(view1 -> mostrarDialogoAgregarMedicamento());

        btnBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment_Serivicios ServiciosFragment = new Fragment_Serivicios(); // Asegúrate de tener esta clase creada
            fragmentTransaction.replace(R.id.fragment_container, ServiciosFragment); // Usa el ID correcto de tu contenedor
            fragmentTransaction.addToBackStack(null); // Opcional
            fragmentTransaction.commit();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pacientesCall != null) pacientesCall.cancel();
        if (medicamentosCall != null) medicamentosCall.cancel();
        if (asignarMedicamentoCall != null) asignarMedicamentoCall.cancel();
        if (listarMedicamentosCall != null) listarMedicamentosCall.cancel();
    }

    private void programarAlarmaMedicamento(PacienteMedicamento pm) {
        try {
            if (pm == null || pm.getFecha_inicio() == null ||
                    pm.getFrecuencia() == null || pm.getHora_inicio() == null) {
                Log.e("AlarmaMedicamento", "Datos incompletos en PacienteMedicamento");
                return;
            }

            // Parsear frecuencia (ejemplo: "2 horas", "1 día", "30 minutos")
            String[] partesFrecuencia = pm.getFrecuencia().split(" ");
            if (partesFrecuencia.length < 2) {
                Log.e("AlarmaMedicamento", "Formato de frecuencia inválido: " + pm.getFrecuencia());
                return;
            }

            int cantidad = Integer.parseInt(partesFrecuencia[0]);
            String unidad = partesFrecuencia[1].toLowerCase();

            // Calcular intervalo en milisegundos
            long intervaloMillis = calcularIntervaloMillis(cantidad, unidad);
            if (intervaloMillis <= 0) {
                Log.e("AlarmaMedicamento", "Intervalo inválido calculado");
                return;
            }

            // Parsear hora de inicio
            String[] partesHora = pm.getHora_inicio().split(":");
            int hora = Integer.parseInt(partesHora[0]);
            int minutos = Integer.parseInt(partesHora[1]);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(pm.getFecha_inicio()));

            // Configurar hora inicial
            calendar.set(Calendar.HOUR_OF_DAY, hora);
            calendar.set(Calendar.MINUTE, minutos);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Si la fecha/hora ya pasó, calcular la próxima ocurrencia
            Calendar ahora = Calendar.getInstance();
            if (calendar.before(ahora)) {
                long diferencia = ahora.getTimeInMillis() - calendar.getTimeInMillis();
                long ocurrenciasPasadas = diferencia / intervaloMillis;
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (ocurrenciasPasadas + 1) * intervaloMillis);
            }

            // Configurar información de la alarma
            String titulo = "Tomar medicamento (ID: " + pm.getId_medicamento() + ")";
            String descripcion = "Dosis: " + pm.getDosis() + "\n" +
                    "Frecuencia: " + pm.getFrecuencia();

            int alarmaId = pm.getId_pac_medicamento();

            // Programar la alarma inicial
            ActivityAlarmReceiver.programarAlarma(
                    requireContext(),
                    alarmaId,
                    titulo,
                    descripcion,
                    sdf.format(calendar.getTime()),
                    pm.getHora_inicio(),
                    calendar.getTimeInMillis()
            );

            // Programar alarmas recurrentes si es necesario
            if (intervaloMillis > 0) {
                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(requireContext(), AlarmReceiver.class);
                intent.putExtra("actividad_id", alarmaId);
                intent.putExtra("titulo", titulo);
                intent.putExtra("descripcion", descripcion);
                intent.putExtra("fecha", sdf.format(calendar.getTime()));
                intent.putExtra("hora", pm.getHora_inicio());
                intent.putExtra("es_recurrente", intervaloMillis > 0);
                intent.putExtra("intervalo_millis", intervaloMillis);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        requireContext(),
                        alarmaId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                }
            }

            // Guardar en base de datos
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
            AlarmaEntity alarma = new AlarmaEntity(
                    alarmaId,
                    titulo,
                    descripcion,
                    sdf.format(calendar.getTime()),
                    pm.getHora_inicio(),
                    calendar.getTimeInMillis(),
                    true,
                    intervaloMillis > 0, // esRecurrente
                    intervaloMillis
            );
            dbHelper.guardarAlarma(alarma);

        } catch (Exception e) {
            Log.e("AlarmaMedicamento", "Error al programar alarma", e);
            Toast.makeText(getContext(), "Error al programar alarma", Toast.LENGTH_SHORT).show();
        }
    } private long calcularIntervaloMillis(int cantidad, String unidad) {
        switch (unidad.toLowerCase()) {
            case "hora":
            case "horas":
                return cantidad * 60 * 60 * 1000L;
            case "dia":
            case "día":
            case "dias":
            case "días":
                return cantidad * 24 * 60 * 60 * 1000L;
            case "minuto":
            case "minutos":
                return cantidad * 60 * 1000L;
            case "semana":
            case "semanas":
                return cantidad * 7 * 24 * 60 * 60 * 1000L;
            case "mes":
            case "meses":
                return cantidad * 30 * 24 * 60 * 60 * 1000L; // Aproximación
            default:
                Log.e("Frecuencia", "Unidad de tiempo no reconocida: " + unidad);
                return 0;
        }
    }
    private void cancelarAlarmaMedicamento(int idAsignacion) {
        // Cancelar alarma principal
        ActivityAlarmReceiver.cancelarAlarma(requireContext(), idAsignacion);

        // Cancelar alarmas recurrentes
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                idAsignacion,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        // Eliminar de la base de datos
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        dbHelper.eliminarAlarma(idAsignacion);
    }
    private void asignarMedicamento(AsignarMedicamentoRequest request, AlertDialog dialog) {
        showLoading(true);
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        asignarMedicamentoCall = authService.asignarMedicamento("Bearer " + token, request);

        asignarMedicamentoCall.enqueue(new Callback<AsignarMedicamentoResponse>() {
            @Override
            public void onResponse(@NonNull Call<AsignarMedicamentoResponse> call,
                                   @NonNull Response<AsignarMedicamentoResponse> response) {
                showLoading(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    AsignarMedicamentoResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Medicamento asignado correctamente", Toast.LENGTH_SHORT).show();

                        if (apiResponse.getData() != null) {
                            programarAlarmaMedicamento(apiResponse.getData());
                        }

                        dialog.dismiss();
                        if (selectedPatientId != -1) {
                            cargarMedicamentosPaciente(selectedPatientId);
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                "Error: " + response.errorBody().string() :
                                "Error: Código " + response.code();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AsignarMedicamentoResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Error al asignar medicamento", t);
            }
        });
    }

    private void actualizarMedicamento(int idAsignacion, AsignarMedicamentoRequest request, AlertDialog dialog) {
        showLoading(true);
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<AsignarMedicamentoResponse> call = authService.actualizarMedicamento("Bearer " + token, idAsignacion, request);

        call.enqueue(new Callback<AsignarMedicamentoResponse>() {
            @Override
            public void onResponse(@NonNull Call<AsignarMedicamentoResponse> call, @NonNull Response<AsignarMedicamentoResponse> response) {
                showLoading(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    AsignarMedicamentoResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Medicamento actualizado correctamente", Toast.LENGTH_SHORT).show();

                        cancelarAlarmaMedicamento(idAsignacion);
                        if (apiResponse.getData() != null) {
                            programarAlarmaMedicamento(apiResponse.getData());
                        }

                        dialog.dismiss();
                        if (selectedPatientId != -1) {
                            cargarMedicamentosPaciente(selectedPatientId);
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                "Error: " + response.errorBody().string() :
                                "Error: Código " + response.code();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AsignarMedicamentoResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Error al actualizar medicamento", t);
            }
        });
    }

    private void eliminarMedicamento(AsignacionMedicamentoResponse asignacion) {
        showLoading(true);
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        Call<AsignarMedicamentoResponse> call = authService.eliminarMedicamento("Bearer " + token, asignacion.getIdAsignacion());

        call.enqueue(new Callback<AsignarMedicamentoResponse>() {
            @Override
            public void onResponse(@NonNull Call<AsignarMedicamentoResponse> call, @NonNull Response<AsignarMedicamentoResponse> response) {
                showLoading(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    AsignarMedicamentoResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Medicamento eliminado correctamente", Toast.LENGTH_SHORT).show();

                        cancelarAlarmaMedicamento(asignacion.getIdAsignacion());

                        if (selectedPatientId != -1) {
                            cargarMedicamentosPaciente(selectedPatientId);
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                "Error: " + response.errorBody().string() :
                                "Error: Código " + response.code();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AsignarMedicamentoResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Error al eliminar medicamento", t);
            }
        });
    }

    private void agregarItemMedicamento(AsignacionMedicamentoResponse asignacion) {
        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_medicamento_asignado, medicamentosContainer, false);

        TextView tvNombre = itemView.findViewById(R.id.tvNombreMedicamento);
        TextView tvDosis = itemView.findViewById(R.id.tvDosis);
        TextView tvFrecuencia = itemView.findViewById(R.id.tvFrecuencia);
        TextView tvFechas = itemView.findViewById(R.id.tvFechas);
        TextView tvHoraInicio = itemView.findViewById(R.id.tvHoraInicio);
        TextView tvEstado = itemView.findViewById(R.id.tvEstado);
        ImageView ivEdit = itemView.findViewById(R.id.ivEdit);
        ImageView ivDelete = itemView.findViewById(R.id.ivDelete);
        ImageView ivAlarm = itemView.findViewById(R.id.ivAlarm);

        if (asignacion.getMedicamento() != null) {
            tvNombre.setText(asignacion.getMedicamento().getNombre());
            Log.d("MEDICAMENTO_DEBUG", "Medicamento: " + asignacion.getMedicamento().getNombre());
        } else {
            tvNombre.setText("Medicamento no disponible");
            Log.e("MEDICAMENTO_ERROR", "Medicamento es null para asignación ID: " + asignacion.getIdAsignacion());
        }

        tvDosis.setText("Dosis: " + (asignacion.getDosis() != null ? asignacion.getDosis() : "No especificada"));
        tvFrecuencia.setText("Frecuencia: " + (asignacion.getFrecuencia() != null ? asignacion.getFrecuencia() : "No especificada"));

        if (asignacion.getHoraInicio() != null) {
            tvHoraInicio.setText("Hora: " + asignacion.getHoraInicio());
        } else {
            tvHoraInicio.setText("Hora no especificada");
        }

        String textoFechas = "Período: ";
        if (asignacion.getFechaInicio() != null && asignacion.getFechaFin() != null) {
            textoFechas += asignacion.getFechaInicio() + " - " + asignacion.getFechaFin();
            Log.d("FECHAS_DEBUG", "Fechas: " + textoFechas);
        } else {
            if (asignacion.getFechaInicio() == null) Log.e("FECHA_ERROR", "Fecha inicio es null");
            if (asignacion.getFechaFin() == null) Log.e("FECHA_ERROR", "Fecha fin es null");
            textoFechas += "Fechas no especificadas";
        }
        tvFechas.setText(textoFechas);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar hoy = Calendar.getInstance();

            if (asignacion.getFechaInicio() != null && asignacion.getFechaFin() != null) {
                Calendar inicio = Calendar.getInstance();
                inicio.setTime(sdf.parse(asignacion.getFechaInicio()));
                Calendar fin = Calendar.getInstance();
                fin.setTime(sdf.parse(asignacion.getFechaFin()));

                if (hoy.before(inicio)) {
                    tvEstado.setText("Pendiente");
                    tvEstado.setTextColor(getResources().getColor(R.color.rojopasion));
                } else if (hoy.after(fin)) {
                    tvEstado.setText("Finalizado");
                    tvEstado.setTextColor(getResources().getColor(R.color.green));
                } else {
                    tvEstado.setText("En curso");
                    tvEstado.setTextColor(getResources().getColor(R.color.azul_link));
                }
            } else {
                tvEstado.setText("Fechas incompletas");
                tvEstado.setTextColor(getResources().getColor(R.color.gray));
            }
        } catch (Exception e) {
            Log.e("DateError", "Error al parsear fechas", e);
            tvEstado.setText("Error en fechas");
            tvEstado.setTextColor(getResources().getColor(R.color.gray));
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getContext());
        boolean tieneAlarma = dbHelper.tieneAlarma(asignacion.getIdAsignacion());
        ivAlarm.setVisibility(tieneAlarma ? View.VISIBLE : View.GONE);

        ivEdit.setOnClickListener(v -> mostrarDialogoEditarMedicamento(asignacion));
        ivDelete.setOnClickListener(v -> mostrarDialogoConfirmarEliminacion(asignacion));
        ivAlarm.setOnClickListener(v -> mostrarDialogoGestionAlarmas(asignacion));

        medicamentosContainer.addView(itemView);
    }

    private void mostrarDialogoGestionAlarmas(AsignacionMedicamentoResponse asignacion) {
        new AlertDialog.Builder(getContext())
                .setTitle("Gestión de Alarmas")
                .setMessage("¿Qué deseas hacer con las alarmas de este medicamento?")
                .setPositiveButton("Reprogramar", (dialog, which) -> {
                    cancelarAlarmaMedicamento(asignacion.getIdAsignacion());

                    // Convertir AsignacionMedicamentoResponse a PacienteMedicamento
                    PacienteMedicamento pm = new PacienteMedicamento();
                    pm.setId_pac_medicamento(asignacion.getIdAsignacion());
                    pm.setFecha_inicio(asignacion.getFechaInicio());
                    pm.setFecha_fin(asignacion.getFechaFin());
                    pm.setHora_inicio(asignacion.getHoraInicio());
                    pm.setDosis(asignacion.getDosis());
                    pm.setFrecuencia(asignacion.getFrecuencia());

                    programarAlarmaMedicamento(pm); // Ahora funciona

                    Toast.makeText(getContext(), "Alarmas reprogramadas", Toast.LENGTH_SHORT).show();
                    if (selectedPatientId != -1) {
                        cargarMedicamentosPaciente(selectedPatientId);
                    }
                })
                .setNegativeButton("Cancelar Alarmas", (dialog, which) -> {
                    cancelarAlarmaMedicamento(asignacion.getIdAsignacion());
                    Toast.makeText(getContext(), "Alarmas canceladas", Toast.LENGTH_SHORT).show();
                    if (selectedPatientId != -1) {
                        cargarMedicamentosPaciente(selectedPatientId);
                    }
                })
                .setNeutralButton("Cerrar", null)
                .show();
    }
    private void setupPatientsSection(View rootView, LayoutInflater inflater) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.setScrollbarFadingEnabled(true);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);

        patientsContainer = new LinearLayout(getContext());
        patientsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        patientsContainer.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScrollView.addView(patientsContainer);

        LinearLayout layoutMedicamentos = rootView.findViewById(R.id.layoutMedicamentos);
        layoutMedicamentos.addView(horizontalScrollView, 1);

        loadPatients();
    }

    private void loadPatients() {
        showLoading(true);
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        pacientesCall = authService.listarPacientes("Bearer " + token);

        pacientesCall.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PacienteListResponse> call, @NonNull Response<PacienteListResponse> response) {
                showLoading(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<PacienteResponse> pacientes = response.body().getData();
                    if (pacientes != null && !pacientes.isEmpty()) {
                        pacientesMap.clear();
                        List<String> pacientesNombres = new ArrayList<>();

                        for (PacienteResponse paciente : pacientes) {
                            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
                            pacientesMap.put(paciente.getId(), nombreCompleto);
                            pacientesNombres.add(nombreCompleto);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_spinner_item,
                                pacientesNombres);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPatientsMain.setAdapter(adapter);

                        mostrarPacientes(pacientes);

                        if (!pacientes.isEmpty()) {
                            selectedPatientId = pacientes.get(0).getId();
                            selectedPatientName = pacientesNombres.get(0);
                            cargarMedicamentosPaciente(selectedPatientId);
                        }
                    } else {
                        showEmptyState("No hay pacientes registrados");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                "Error: " + response.errorBody().string() :
                                "Error: Código " + response.code();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al obtener pacientes", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PacienteListResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Error al obtener pacientes", t);
            }
        });
    }

    private void mostrarPacientes(List<PacienteResponse> pacientes) {
        patientsContainer.removeAllViews();

        for (PacienteResponse paciente : pacientes) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";
            String fechaNacimiento = paciente.getFecha_nacimiento() != null ?
                    paciente.getFecha_nacimiento() : "Fecha desconocida";
            String imagenUrl = paciente.getImagen_paciente();

            addPatientCard(nombreCompleto, fechaNacimiento, diagnostico,
                    imagenUrl, paciente.getId());
        }
    }

    private void addPatientCard(String name, String birthDate, String conditions,
                                String imagenUrl, int patientId) {
        View patientCard = LayoutInflater.from(getContext())
                .inflate(R.layout.item_patient_card, patientsContainer, false);
        patientCard.setTag(patientId);

        TextView tvName = patientCard.findViewById(R.id.tvPatientName);
        TextView tvConditions = patientCard.findViewById(R.id.tvPatientConditions);
        ImageView ivProfile = patientCard.findViewById(R.id.ivPatientProfile);

        tvName.setText(name);

        if (conditions != null && !conditions.isEmpty()) {
            String[] conditionsList = conditions.split(",");
            StringBuilder formattedConditions = new StringBuilder();
            for (String condition : conditionsList) {
                formattedConditions.append("• ").append(condition.trim()).append("\n");
            }
            tvConditions.setText(formattedConditions.toString().trim());
        } else {
            tvConditions.setText("• Sin diagnóstico");
        }

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imagenUrl)
                    .placeholder(R.drawable.perfil_familiar)
                    .error(R.drawable.perfil_familiar)
                    .circleCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.perfil_familiar);
        }

        patientCard.setOnClickListener(v -> {
            selectedPatientId = patientId;
            selectedPatientName = name;
            updatePatientSelection();
            cargarMedicamentosPaciente(patientId);
        });

        patientsContainer.addView(patientCard);
    }

    private void cargarMedicamentosPaciente(int pacienteId) {
        showLoading(true);
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        medicamentosCall = authService.listarAsignacionesMedicamentos("Bearer " + token, pacienteId);

        medicamentosCall.enqueue(new Callback<List<AsignacionMedicamentoResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<AsignacionMedicamentoResponse>> call,
                                   @NonNull Response<List<AsignacionMedicamentoResponse>> response) {
                showLoading(false);
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    List<AsignacionMedicamentoResponse> asignaciones = response.body();

                    Log.d("API_RESPONSE", "Respuesta recibida: " + new Gson().toJson(asignaciones));

                    if (asignaciones != null && !asignaciones.isEmpty()) {
                        mostrarMedicamentosAsignados(asignaciones);
                    } else {
                        showEmptyState("No hay medicamentos asignados para " + selectedPatientName);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                "Error: " + response.errorBody().string() :
                                "Error: Código " + response.code();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", errorBody);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", "Error al procesar error", e);
                    }
                    showEmptyState("Error al cargar medicamentos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AsignacionMedicamentoResponse>> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Error al obtener medicamentos", t);
                showEmptyState("Error de conexión");
            }
        });
    }

    private void mostrarMedicamentosAsignados(List<AsignacionMedicamentoResponse> asignaciones) {
        medicamentosContainer.removeAllViews();

        if (asignaciones == null || asignaciones.isEmpty()) {
            showEmptyState("No hay medicamentos asignados para " + selectedPatientName);
            return;
        }

        TextView title = new TextView(getContext());
        title.setText("Medicamentos asignados a " + selectedPatientName);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 16, 0, 16);
        medicamentosContainer.addView(title);

        for (AsignacionMedicamentoResponse asignacion : asignaciones) {
            agregarItemMedicamento(asignacion);
        }
    }

    private void mostrarDialogoEditarMedicamento(AsignacionMedicamentoResponse asignacion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_agregar_medicamento_usuario, null);
        builder.setView(viewInflated);

        Spinner spinnerPatients = viewInflated.findViewById(R.id.spinner_patients);
        Spinner spinnerMedications = viewInflated.findViewById(R.id.spinner_medications);
        Spinner spinnerFrequencyUnit = viewInflated.findViewById(R.id.spinner_frequency_unit);
        TextView tvStartDate = viewInflated.findViewById(R.id.tv_start_date);
        TextView tvEndDate = viewInflated.findViewById(R.id.tv_end_date);
        tvStartTime = viewInflated.findViewById(R.id.tv_start_time);
        EditText etDosage = viewInflated.findViewById(R.id.et_dosage);
        EditText etFrequencyNumber = viewInflated.findViewById(R.id.et_frequency_number);
        Button btnSave = viewInflated.findViewById(R.id.btn_save);
        LinearLayout layoutStartTime = viewInflated.findViewById(R.id.layout_start_time);

        btnSave.setText("Actualizar");

        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.frequency_units,
                android.R.layout.simple_spinner_item
        );
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencyUnit.setAdapter(frequencyAdapter);

        tvStartDate.setText(asignacion.getFechaInicio());
        tvEndDate.setText(asignacion.getFechaFin());
        etDosage.setText(asignacion.getDosis());

        if (asignacion.getHoraInicio() != null) {
            tvStartTime.setText(asignacion.getHoraInicio());
        }

        layoutStartTime.setOnClickListener(v -> showTimePickerDialog());

        if (asignacion.getFrecuencia() != null && !asignacion.getFrecuencia().isEmpty()) {
            String[] frecuenciaParts = asignacion.getFrecuencia().split(" ");
            if (frecuenciaParts.length >= 2) {
                etFrequencyNumber.setText(frecuenciaParts[0]);
                for (int i = 0; i < spinnerFrequencyUnit.getCount(); i++) {
                    if (spinnerFrequencyUnit.getItemAtPosition(i).toString().equalsIgnoreCase(frecuenciaParts[1])) {
                        spinnerFrequencyUnit.setSelection(i);
                        break;
                    }
                }
            }
        }

        setupSpinners(spinnerPatients, spinnerMedications);
        spinnerPatients.setEnabled(false);
        spinnerMedications.setEnabled(false);

        LinearLayout layoutStartDate = viewInflated.findViewById(R.id.layout_start_date);
        LinearLayout layoutEndDate = viewInflated.findViewById(R.id.layout_end_date);

        final Calendar dialogStartDate = Calendar.getInstance();
        final Calendar dialogEndDate = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dialogStartDate.setTime(sdf.parse(asignacion.getFechaInicio()));
            dialogEndDate.setTime(sdf.parse(asignacion.getFechaFin()));
        } catch (Exception e) {
            Log.e("DateError", "Error al parsear fechas", e);
        }

        layoutStartDate.setOnClickListener(v -> showDatePickerDialog(dialogStartDate, tvStartDate, true, dialogEndDate, tvEndDate));
        layoutEndDate.setOnClickListener(v -> showDatePickerDialog(dialogEndDate, tvEndDate, false, dialogStartDate, null));

        AlertDialog dialog = builder.create();

        spinnerPatients.post(() -> {
            if (asignacion.getPaciente() != null && pacientesMap.containsKey(asignacion.getPaciente().getId())) {
                String pacienteNombre = pacientesMap.get(asignacion.getPaciente().getId());
                int position = ((ArrayAdapter<String>) spinnerPatients.getAdapter()).getPosition(pacienteNombre);
                if (position >= 0) {
                    spinnerPatients.setSelection(position);
                }
            }
        });

        spinnerMedications.post(() -> {
            if (asignacion.getMedicamento() != null && medicamentosMap.containsKey(asignacion.getMedicamento().getId_medicamento())) {
                String medicamentoNombre = medicamentosMap.get(asignacion.getMedicamento().getId_medicamento());
                int position = ((ArrayAdapter<String>) spinnerMedications.getAdapter()).getPosition(medicamentoNombre);
                if (position >= 0) {
                    spinnerMedications.setSelection(position);
                }
            }
        });

        dialog.show();

        btnSave.setOnClickListener(v -> {
            if (validarCampos(spinnerPatients, spinnerMedications, etDosage, etFrequencyNumber, tvStartDate, tvEndDate, tvStartTime)) {
                String frecuenciaNumero = etFrequencyNumber.getText().toString();
                String frecuenciaUnidad = spinnerFrequencyUnit.getSelectedItem().toString();
                String frecuenciaCompleta = frecuenciaNumero + " " + frecuenciaUnidad;
                String horaInicio = tvStartTime.getText().toString();

                AsignarMedicamentoRequest request = new AsignarMedicamentoRequest(
                        asignacion.getPaciente().getId(),
                        asignacion.getMedicamento().getId_medicamento(),
                        tvStartDate.getText().toString(),
                        tvEndDate.getText().toString(),
                        horaInicio,
                        etDosage.getText().toString(),
                        frecuenciaCompleta
                );

                actualizarMedicamento(asignacion.getIdAsignacion(), request, dialog);
            }
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, which) -> dialogInterface.dismiss());
    }

    private void mostrarDialogoConfirmarEliminacion(AsignacionMedicamentoResponse asignacion) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este medicamento?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarMedicamento(asignacion))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoAgregarMedicamento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_agregar_medicamento_usuario, null);
        builder.setView(viewInflated);

        Spinner spinnerPatients = viewInflated.findViewById(R.id.spinner_patients);
        Spinner spinnerMedications = viewInflated.findViewById(R.id.spinner_medications);
        Spinner spinnerFrequencyUnit = viewInflated.findViewById(R.id.spinner_frequency_unit);
        TextView tvStartDate = viewInflated.findViewById(R.id.tv_start_date);
        TextView tvEndDate = viewInflated.findViewById(R.id.tv_end_date);
        tvStartTime = viewInflated.findViewById(R.id.tv_start_time);
        EditText etDosage = viewInflated.findViewById(R.id.et_dosage);
        EditText etFrequencyNumber = viewInflated.findViewById(R.id.et_frequency_number);
        Button btnSave = viewInflated.findViewById(R.id.btn_save);
        LinearLayout layoutStartTime = viewInflated.findViewById(R.id.layout_start_time);

        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.frequency_units,
                android.R.layout.simple_spinner_item
        );
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencyUnit.setAdapter(frequencyAdapter);

        LinearLayout layoutStartDate = viewInflated.findViewById(R.id.layout_start_date);
        LinearLayout layoutEndDate = viewInflated.findViewById(R.id.layout_end_date);

        final Calendar dialogStartDate = Calendar.getInstance();
        final Calendar dialogEndDate = Calendar.getInstance();
        dialogEndDate.add(Calendar.DAY_OF_MONTH, 0);

        tvStartDate.setText(dateFormatter.format(dialogStartDate.getTime()));
        tvEndDate.setText(dateFormatter.format(dialogEndDate.getTime()));
        tvStartTime.setText("08:00"); // Hora por defecto

        layoutStartDate.setOnClickListener(v -> showDatePickerDialog(dialogStartDate, tvStartDate, true, dialogEndDate, tvEndDate));
        layoutEndDate.setOnClickListener(v -> showDatePickerDialog(dialogEndDate, tvEndDate, false, dialogStartDate, null));
        layoutStartTime.setOnClickListener(v -> showTimePickerDialog());

        setupSpinners(spinnerPatients, spinnerMedications);

        if (selectedPatientId != -1 && pacientesMap.containsKey(selectedPatientId)) {
            String selectedName = pacientesMap.get(selectedPatientId);
            int position = ((ArrayAdapter<String>) spinnerPatients.getAdapter()).getPosition(selectedName);
            if (position >= 0) {
                spinnerPatients.setSelection(position);
            }
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            if (validarCampos(spinnerPatients, spinnerMedications, etDosage, etFrequencyNumber, tvStartDate, tvEndDate, tvStartTime)) {
                String pacienteNombre = spinnerPatients.getSelectedItem().toString();
                String medicamentoNombre = spinnerMedications.getSelectedItem().toString();
                String dosis = etDosage.getText().toString();
                String frecuenciaNumero = etFrequencyNumber.getText().toString();
                String frecuenciaUnidad = spinnerFrequencyUnit.getSelectedItem().toString();
                String fechaInicio = tvStartDate.getText().toString();
                String fechaFin = tvEndDate.getText().toString();
                String horaInicio = tvStartTime.getText().toString();

                String frecuenciaCompleta = frecuenciaNumero + " " + frecuenciaUnidad;

                int idPaciente = obtenerIdPorNombre(pacientesMap, pacienteNombre);
                int idMedicamento = obtenerIdPorNombre(medicamentosMap, medicamentoNombre);

                if (idPaciente == -1 || idMedicamento == -1) {
                    Toast.makeText(getContext(), "Error al obtener datos del paciente o medicamento", Toast.LENGTH_SHORT).show();
                    return;
                }

                AsignarMedicamentoRequest request = new AsignarMedicamentoRequest(
                        idPaciente,
                        idMedicamento,
                        fechaInicio,
                        fechaFin,
                        horaInicio,
                        dosis,
                        frecuenciaCompleta
                );

                asignarMedicamento(request,dialog);
            }
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, which) -> dialogInterface.dismiss());
    }

    private void showTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Si hay una hora ya seleccionada, usarla como valor inicial
        if (tvStartTime.getText() != null && !tvStartTime.getText().toString().isEmpty()) {
            try {
                String[] timeParts = tvStartTime.getText().toString().split(":");
                if (timeParts.length == 2) {
                    hour = Integer.parseInt(timeParts[0]);
                    minute = Integer.parseInt(timeParts[1]);
                }
            } catch (Exception e) {
                Log.e("TimePicker", "Error al parsear hora existente", e);
            }
        }

        timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute1) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute1);
                    // Formatear siempre a HH:mm
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    tvStartTime.setText(formattedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showDatePickerDialog(final Calendar dateToSet, final TextView textView,
                                      final boolean isStartDate,
                                      final Calendar minDate, final TextView dependentTextView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    dateToSet.set(year, month, dayOfMonth);
                    textView.setText(dateFormatter.format(dateToSet.getTime()));

                    if (isStartDate && dependentTextView != null && dateToSet.after(minDate)) {
                        minDate.setTime(dateToSet.getTime());
                        minDate.add(Calendar.DAY_OF_MONTH, 1);
                        dependentTextView.setText(dateFormatter.format(minDate.getTime()));
                    }
                },
                dateToSet.get(Calendar.YEAR),
                dateToSet.get(Calendar.MONTH),
                dateToSet.get(Calendar.DAY_OF_MONTH));

        if (!isStartDate && minDate != null) {
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void setupSpinners(Spinner spinnerPatients, Spinner spinnerMedications) {
        List<String> pacientesNombres = new ArrayList<>(pacientesMap.values());
        ArrayAdapter<String> patientsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                pacientesNombres
        );
        patientsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatients.setAdapter(patientsAdapter);

        ArrayAdapter<String> medicationsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        medicationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedications.setAdapter(medicationsAdapter);

        loadMedications(spinnerMedications);
    }

    private void loadMedications(Spinner spinnerMedications) {
        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = ApiClient.getClient();
        AuthService authService = retrofit.create(AuthService.class);
        listarMedicamentosCall = authService.listarMedicamentos("Bearer " + token);

        listarMedicamentosCall.enqueue(new Callback<List<MedicamentoResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MedicamentoResponse>> call,
                                   @NonNull Response<List<MedicamentoResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<MedicamentoResponse> medicamentos = response.body();
                    medicamentosMap.clear();
                    List<String> nombresMedicamentos = new ArrayList<>();

                    for (MedicamentoResponse medicamento : medicamentos) {
                        medicamentosMap.put(medicamento.getId_medicamento(), medicamento.getNombre());
                        nombresMedicamentos.add(medicamento.getNombre());
                    }

                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerMedications.getAdapter();
                    adapter.clear();
                    adapter.addAll(nombresMedicamentos);
                    adapter.notifyDataSetChanged();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                "Error: " + response.errorBody().string() :
                                "Error: Código " + response.code();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al obtener medicamentos", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MedicamentoResponse>> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validarCampos(Spinner spinnerPatients, Spinner spinnerMedications,
                                  EditText etDosage, EditText etFrequency,
                                  TextView tvStartDate, TextView tvEndDate, TextView tvStartTime) {
        if (spinnerPatients.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Selecciona un paciente", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerMedications.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Selecciona un medicamento", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDosage.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Ingresa la dosis", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etFrequency.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Ingresa la frecuencia", Toast.LENGTH_SHORT).show();
            return false;
        }
        String fechaInicio = tvStartDate.getText().toString().trim();
        String fechaFin = tvEndDate.getText().toString().trim();
        if (fechaInicio.isEmpty() || fechaFin.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona fechas válidas", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvStartTime.getText().toString().trim().isEmpty() ||
                tvStartTime.getText().toString().equals("Seleccionar hora")) {
            Toast.makeText(getContext(), "Selecciona una hora de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int obtenerIdPorNombre(Map<Integer, String> map, String nombre) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(nombre)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private void updatePatientSelection() {
        if (selectedPatientId != -1 && pacientesMap.containsKey(selectedPatientId)) {
            String selectedName = pacientesMap.get(selectedPatientId);
            int position = ((ArrayAdapter<String>) spinnerPatientsMain.getAdapter())
                    .getPosition(selectedName);
            if (position >= 0) {
                spinnerPatientsMain.setSelection(position);
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        medicamentosContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(String message) {
        medicamentosContainer.removeAllViews();
        TextView tvEmpty = new TextView(getContext());
        tvEmpty.setText(message);
        tvEmpty.setTextSize(16);
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setPadding(0, 32, 0, 32);
        medicamentosContainer.addView(tvEmpty);
    }
}