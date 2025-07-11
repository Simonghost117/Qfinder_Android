package com.sena.qfinder.ui.paciente;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.sena.qfinder.R;
import com.sena.qfinder.data.api.AuthService;
import com.sena.qfinder.data.models.PacienteListResponse;
import com.sena.qfinder.data.models.PacienteResponse;
import com.sena.qfinder.ui.home.Fragment_Serivicios;
import com.sena.qfinder.ui.home.PerfilPaciente;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GestionPacienteFragment extends Fragment {

    private LinearLayout patientsContainer;
    private ImageView addButton;
    private ImageView btnBack;
    private final String BASE_URL = "https://qfinder-production.up.railway.app/";

    public GestionPacienteFragment() {
        // Constructor vacío requerido
    }

    public static GestionPacienteFragment newInstance(String param1, String param2) {
        GestionPacienteFragment fragment = new GestionPacienteFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gestion_paciente, container, false);

        setupPatientsSection(inflater, root);
        setupAddButton(root);

        // Botón de retroceso
        ImageView btnBack = root.findViewById(R.id.btnBack); // O Button si usas otro tipo de vista
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment_Serivicios serviciosFragment = new Fragment_Serivicios(); // Asegúrate de tener esta clase creada
                fragmentTransaction.replace(R.id.fragment_container, serviciosFragment); // Usa el ID correcto del contenedor de fragments
                fragmentTransaction.addToBackStack(null); // Opcional para volver hacia adelante luego
                fragmentTransaction.commit();
            }
        });

        return root;
    }


    private void setupPatientsSection(LayoutInflater inflater, View root) {
        patientsContainer = root.findViewById(R.id.containerPacientes);
        patientsContainer.removeAllViews();

        // Obtener token de SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurar Retrofit con interceptor para logs
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
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<PacienteResponse> pacientes = response.body().getData();
                    mostrarPacientes(inflater, pacientes != null ? pacientes : new ArrayList<>());
                } else {
                    Toast.makeText(getContext(), "Error al obtener pacientes", Toast.LENGTH_SHORT).show();
                    mostrarPacientes(inflater, new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<PacienteListResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Error de conexión", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                mostrarPacientes(inflater, new ArrayList<>());
            }
        });
    }

    private void mostrarPacientes(LayoutInflater inflater, List<PacienteResponse> pacientes) {
        patientsContainer.removeAllViews();

        for (PacienteResponse paciente : pacientes) {
            String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();
            String diagnostico = paciente.getDiagnostico_principal() != null ?
                    paciente.getDiagnostico_principal() : "Sin diagnóstico";
            String imagenUrl = paciente.getImagen_paciente(); // Obtener URL de la imagen

            View card = inflater.inflate(R.layout.item_paciente, patientsContainer, false);

            TextView nombreTextView = card.findViewById(R.id.nombrePaciente);
            TextView enfermedadTextView = card.findViewById(R.id.enfermedadPaciente);
            ImageView fotoPaciente = card.findViewById(R.id.imagenPaciente);

            nombreTextView.setText(nombreCompleto);
            enfermedadTextView.setText(diagnostico);

            // Cargar imagen con Glide
            if (imagenUrl != null && !imagenUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imagenUrl)
                        .placeholder(R.drawable.perfil_paciente) // Imagen por defecto
                        .error(R.drawable.perfil_paciente) // Imagen si hay error
                        .circleCrop() // Para hacerla circular
                        .into(fotoPaciente);
            } else {
                fotoPaciente.setImageResource(R.drawable.perfil_paciente);
            }

            card.setOnClickListener(v -> {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, PerfilPaciente.newInstance(paciente.getId()));
                transaction.addToBackStack(null);
                transaction.commit();
            });

            patientsContainer.addView(card);
        }
    }

    private void setupAddButton(View root) {
        addButton = root.findViewById(R.id.PatientAdd);
        if (addButton != null) {
            addButton.setOnClickListener(v -> navigateToRegisterPatient());
        } else {
            Log.e("GestionPacienteFragment", "No se encontró el botón de agregar paciente");
        }
    }

    private void navigateToRegisterPatient() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new RegistrarPaciente());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}