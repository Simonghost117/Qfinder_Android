package com.sena.qfinder.ui.home;

import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.sena.qfinder.R;
import com.sena.qfinder.adapters.CitaAdapter;
import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.CitaMedica;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CitasFragment extends Fragment {

    private RecyclerView recyclerViewCitas;
    private CitaAdapter citaAdapter;
    private List<CitaMedica> listaCitas = new ArrayList<>();

    private AuthService authService;
    private String token;
    private int idPaciente;

    public CitasFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas, container, false);

        // Inicializar RecyclerView
        recyclerViewCitas = view.findViewById(R.id.recyclerCitas);
        recyclerViewCitas.setLayoutManager(new LinearLayoutManager(getContext()));
        citaAdapter = new CitaAdapter(listaCitas);
        recyclerViewCitas.setAdapter(citaAdapter);

        // Inicializar calendario
        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setSelectedDate(CalendarDay.today());

        // Botón para agregar recordatorio
        Button btnAgregar = view.findViewById(R.id.btnAgregarRecordatorio);
        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        // Inicializar API
        authService = ApiClient.getClient().create(AuthService.class);

        // Obtener token e ID del paciente desde SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);
        idPaciente = prefs.getInt("id_paciente", -1);

        // Validar datos y obtener citas
        if (token != null && idPaciente != -1) {
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
        CheckBox check1 = dialogView.findViewById(R.id.checkUnDiaAntes);
        CheckBox check2 = dialogView.findViewById(R.id.checkMismoDia);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnGuardar.setOnClickListener(v -> {
            // Aquí puedes guardar los datos del formulario si deseas
            Toast.makeText(getContext(), "Cita guardada (lógica pendiente)", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private void obtenerCitasDesdeApi() {
        Call<List<CitaMedica>> call = authService.listarCitasMedicas("Bearer " + token, idPaciente);

        call.enqueue(new Callback<List<CitaMedica>>() {
            @Override
            public void onResponse(@NonNull Call<List<CitaMedica>> call, @NonNull Response<List<CitaMedica>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCitas.clear();
                    listaCitas.addAll(response.body());
                    citaAdapter.notifyDataSetChanged();
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
}
