package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.card.MaterialCardView;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PacienteListResponse;
import com.sena.qfinder.models.PacienteResponse;
import com.sena.qfinder.ui.home.DashboardFragment;
import com.sena.qfinder.ui.home.EditarPacienteDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilPaciente extends Fragment implements EditarPacienteDialogFragment.OnPacienteActualizadoListener {

    private static final String ARG_PACIENTE_ID = "paciente_id";
    private static final String BASE_URL = "https://qfinder-production.up.railway.app/";

    private int pacienteId;
    private SharedPreferences sharedPreferences;

    private TextView tvNombreApellido, tvFechaNacimiento, tvSexo, tvDiagnostico, tvIdentificacion;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private PacienteResponse pacienteActual;

    public PerfilPaciente() {}

    public static PerfilPaciente newInstance(int pacienteId) {
        PerfilPaciente fragment = new PerfilPaciente();
        Bundle args = new Bundle();
        args.putInt(ARG_PACIENTE_ID, pacienteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pacienteId = getArguments().getInt(ARG_PACIENTE_ID);
        }
        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_paciente, container, false);

        MaterialCardView btnEditar = view.findViewById(R.id.boton);
        btnEditar.setOnClickListener(v -> abrirDialogoEditar());

        initViews(view);
        setupBackButton();
        loadPacienteData();

        return view;
    }

    private void initViews(View view) {
        tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento);
        tvSexo = view.findViewById(R.id.tvSexo);
        tvDiagnostico = view.findViewById(R.id.tvDiagnostico);
        tvIdentificacion = view.findViewById(R.id.tvIdentificacion);
        btnBack = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateBack());
        }
    }

    private void loadPacienteData() {
        String token = sharedPreferences.getString("token", null);
        if (token == null) {
            showError("Sesión no válida");
            return;
        }

        showLoading(true);

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

        call.enqueue(new Callback<PacienteListResponse>() {
            @Override
            public void onResponse(Call<PacienteListResponse> call, Response<PacienteListResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PacienteResponse> pacientes = response.body().getData();
                    for (PacienteResponse paciente : pacientes) {
                        if (paciente.getId() == pacienteId) {
                            displayPacienteData(paciente);
                            return;
                        }
                    }
                    showError("Paciente no encontrado");
                } else {
                    showError("Error al cargar datos del paciente");
                }
            }

            @Override
            public void onFailure(Call<PacienteListResponse> call, Throwable t) {
                showLoading(false);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void displayPacienteData(PacienteResponse paciente) {
        if (!isAdded() || getActivity() == null || paciente == null) return;

        pacienteActual = paciente;

        getActivity().runOnUiThread(() -> {
            String nombreCompleto = (paciente.getNombre() + " " + paciente.getApellido()).trim();
            tvNombreApellido.setText(nombreCompleto);

            String fecha = paciente.getFecha_nacimiento() != null ?
                    formatFecha(paciente.getFecha_nacimiento()) : "No especificada";
            tvFechaNacimiento.setText("FECHA DE NACIMIENTO: " + fecha);

            String sexo = paciente.getSexo() != null ? capitalizeFirstLetter(paciente.getSexo()) : "No especificado";
            tvSexo.setText("SEXO: " + sexo);

            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";
            tvDiagnostico.setText("DIAGNÓSTICO: " + diagnostico);

            String identificacion = paciente.getIdentificacion() != null ?
                    paciente.getIdentificacion() : "No especificada";
            tvIdentificacion.setText("IDENTIFICACIÓN: " + identificacion);
        });
    }

    private void abrirDialogoEditar() {
        if (pacienteActual == null) {
            showToast("No hay paciente cargado para editar");
            return;
        }

        EditarPacienteDialogFragment dialog = new EditarPacienteDialogFragment(pacienteActual);
        dialog.setOnPacienteActualizadoListener(this); // Setear el listener
        dialog.show(getParentFragmentManager(), "editarPaciente");
    }

    @Override
    public void onPacienteActualizado() {
        loadPacienteData(); // Recargar datos del paciente cuando se edite
    }

    private String formatFecha(String fechaOriginal) {
        try {
            SimpleDateFormat original = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fecha = original.parse(fechaOriginal);
            return formatoDeseado.format(fecha);
        } catch (Exception e) {
            return fechaOriginal;
        }
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void showToast(String message) {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void navigateBack() {
        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new DashboardFragment());
            transaction.commit();
        }
    }
}
