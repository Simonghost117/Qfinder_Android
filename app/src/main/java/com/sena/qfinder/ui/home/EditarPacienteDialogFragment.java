package com.sena.qfinder.ui.home;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.sena.qfinder.R;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.PacienteRequest;
import com.sena.qfinder.models.PacienteResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditarPacienteDialogFragment extends DialogFragment {

    private EditText nombreEditText, apellidoEditText, identificacionEditText, fechaNacimientoEditText, diagnosticoEditText;
    private Spinner sexoSpinner;
    private Button btnGuardar;
    private int pacienteId;
    private PacienteResponse paciente;
    private AuthService authService;

    private OnPacienteActualizadoListener listener;

    public interface OnPacienteActualizadoListener {
        void onPacienteActualizado();
    }

    public EditarPacienteDialogFragment(PacienteResponse paciente) {
        this.paciente = paciente;
        this.pacienteId = paciente.getId();
    }

    // MÉTODO PÚBLICO PARA SETEAR EL LISTENER DESDE AFUERA
    public void setOnPacienteActualizadoListener(OnPacienteActualizadoListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (listener == null) { // Solo asignar si no se asignó por el setter
            if (context instanceof OnPacienteActualizadoListener) {
                listener = (OnPacienteActualizadoListener) context;
            } else if (getParentFragment() instanceof OnPacienteActualizadoListener) {
                listener = (OnPacienteActualizadoListener) getParentFragment();
            } else {
                throw new ClassCastException("Debe implementar OnPacienteActualizadoListener");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_editar_paciente, container, false);

        nombreEditText = view.findViewById(R.id.etNombre);
        apellidoEditText = view.findViewById(R.id.etApellido);
        identificacionEditText = view.findViewById(R.id.edtIdentificacion);
        sexoSpinner = view.findViewById(R.id.spinnerSexo);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        fechaNacimientoEditText = view.findViewById(R.id.etFechaNacimiento);
        diagnosticoEditText = view.findViewById(R.id.etDiagnostico);

        setupRetrofit();

        nombreEditText.setText(paciente.getNombre());
        apellidoEditText.setText(paciente.getApellido());
        identificacionEditText.setText(paciente.getIdentificacion());
        fechaNacimientoEditText.setText(convertirFechaParaMostrar(paciente.getFecha_nacimiento()));
        diagnosticoEditText.setText(paciente.getDiagnostico_principal());

        fechaNacimientoEditText.setOnClickListener(v -> mostrarDatePicker());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sexo_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexoSpinner.setAdapter(adapter);

        if (paciente.getSexo() != null && paciente.getSexo().equalsIgnoreCase("masculino")) {
            sexoSpinner.setSelection(0);
        } else {
            sexoSpinner.setSelection(1);
        }

        btnGuardar.setOnClickListener(v -> guardarCambios());

        return view;
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String fechaFormateada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    fechaNacimientoEditText.setText(fechaFormateada);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);
    }


    private void guardarCambios() {
        String nombre = nombreEditText.getText().toString().trim();
        String apellido = apellidoEditText.getText().toString().trim();
        String identificacion = identificacionEditText.getText().toString().trim();
        String sexo = sexoSpinner.getSelectedItem().toString();
        String fechaNacimientoFormateada = convertirFechaParaEnviar(fechaNacimientoEditText.getText().toString().trim());
        String diagnostico = diagnosticoEditText.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        PacienteRequest request = new PacienteRequest(
                nombre,
                apellido,
                fechaNacimientoFormateada,
                sexo,
                diagnostico,
                identificacion
        );

        String token = getContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
                .getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "Token no disponible. Por favor inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("EDITAR_PACIENTE", "Request JSON: " + new Gson().toJson(request));
        Log.d("EDITAR_PACIENTE", "Token: Bearer " + token);

        Call<Void> call = authService.actualizarPaciente("Bearer " + token, pacienteId, request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Paciente actualizado correctamente", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onPacienteActualizado();
                    }
                    dismiss();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("EDITAR_PACIENTE", "Error al actualizar: " + response.code());
                        Log.e("EDITAR_PACIENTE", "Cuerpo del error: " + errorBody);
                        Toast.makeText(getContext(), "Error " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error desconocido al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EDITAR_PACIENTE", "Fallo de red", t);
            }
        });
    }

    // Convierte de yyyy-MM-dd a dd/MM/yyyy para mostrar en el EditText
    private String convertirFechaParaMostrar(String fecha) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatoEntrada.parse(fecha);
            return formatoSalida.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return fecha;
        }
    }

    // Convierte de dd/MM/yyyy a yyyy-MM-dd para enviar al backend
    private String convertirFechaParaEnviar(String fechaUsuario) {
        try {
            SimpleDateFormat formatoUsuario = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatoBackend = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatoUsuario.parse(fechaUsuario);
            return formatoBackend.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaUsuario;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95); // Cambia el porcentaje si quieres más o menos ancho
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
