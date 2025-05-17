package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PacienteResponse;
import com.sena.qfinder.ui.home.DashboardFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilPaciente extends Fragment {

    private static final String ARG_PACIENTE_ID = "id";
    private static final String BASE_URL = "https://qfinder-production.up.railway.app/";

    private int pacienteId = -1;
    private SharedPreferences sharedPreferences;

    private TextView tvNombreApellido, tvFechaNacimiento, tvSexo, tvDiagnostico, tvIdentificacion;
    private ImageView btnBack;

    public PerfilPaciente() {
        // Required empty public constructor
    }

    public static PerfilPaciente newInstance(int pacienteId) {
        PerfilPaciente fragment = new PerfilPaciente();
        Bundle args = new Bundle();
        args.putInt(ARG_PACIENTE_ID, pacienteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pacienteId = getArguments().getInt(ARG_PACIENTE_ID, -1);
            Log.d("PerfilPaciente", "ID del paciente recibido: " + pacienteId);
        }

        sharedPreferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_paciente, container, false);

        // Referencias UI
        tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento);
        tvSexo = view.findViewById(R.id.tvSexo);
        tvDiagnostico = view.findViewById(R.id.tvDiagnostico);
        tvIdentificacion = view.findViewById(R.id.tvIdentificacion);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> volverADashboard());

        obtenerInformacionPaciente();

        return view;
    }

    private void obtenerInformacionPaciente() {
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pacienteId == -1) {
            tvNombreApellido.setText("ID de paciente no proporcionado");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService authService = retrofit.create(AuthService.class);
        Call<List<PacienteResponse>> call = authService.listarPacientes1("Bearer " + token);

        call.enqueue(new Callback<List<PacienteResponse>>() {
            @Override
            public void onResponse(Call<List<PacienteResponse>> call, Response<List<PacienteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PacienteResponse> pacientes = response.body();
                    mostrarInformacionPaciente(pacientes);
                } else {
                    Toast.makeText(getContext(), "Error al obtener información del paciente", Toast.LENGTH_SHORT).show();
                    Log.e("PerfilPaciente", "Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<PacienteResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                Log.e("PerfilPaciente", "Error al obtener paciente", t);
            }
        });
    }

    private void mostrarInformacionPaciente(List<PacienteResponse> pacientes) {
        for (PacienteResponse paciente : pacientes) {
            if (paciente.getId() == pacienteId) {
                tvNombreApellido.setText(paciente.getNombre() + " " + paciente.getApellido());
                tvFechaNacimiento.setText(paciente.getFecha_nacimiento());
                tvSexo.setText(paciente.getSexo());
                tvDiagnostico.setText(paciente.getDiagnostico_principal());
                tvIdentificacion.setText(paciente.getIdentificacion());
                return;
            }
        }
        tvNombreApellido.setText("Paciente no encontrado");
    }

    private void volverADashboard() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new DashboardFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}