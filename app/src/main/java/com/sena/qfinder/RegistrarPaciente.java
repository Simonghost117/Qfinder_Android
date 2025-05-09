package com.sena.qfinder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sena.qfinder.model.ManagerDB;
import com.sena.qfinder.ui.home.DashboardFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RegistrarPaciente extends Fragment {

    ManagerDB managerDB;

    ImageView btnBack;

    private EditText editNombreApellido, editFechaNacimiento, editSexo, editDiagnostico, editIdentificacion;
    private Button btnRegistrar;

    public RegistrarPaciente() {
    }

    public static RegistrarPaciente newInstance(String param1, String param2) {
        RegistrarPaciente fragment = new RegistrarPaciente();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registrar_paciente, container, false);

        editNombreApellido = view.findViewById(R.id.etNombresApellidos);
        editFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        editSexo = view.findViewById(R.id.etSexo);
        editDiagnostico = view.findViewById(R.id.etDiagnostico);
        editIdentificacion = view.findViewById(R.id.etIdentificacion);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);

        // Mostrar el selector de fecha con clic o foco
        editFechaNacimiento.setOnClickListener(v -> showDatePickerDialog());
        editFechaNacimiento.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePickerDialog();
            }
        });

        // Bloquear escritura directa en el campo
        editFechaNacimiento.setKeyListener(null);

        btnRegistrar.setOnClickListener(v -> registrarPaciente());
        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new DashboardFragment()); // o el fragmento que represente tu dashboard
            transaction.addToBackStack(null);
            transaction.commit();
        });


        return view;
    }

    // Mostrar DatePickerDialog para seleccionar la fecha
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    editFechaNacimiento.setText(sdf.format(selectedDate.getTime()));
                    editFechaNacimiento.clearFocus();
                }, year, month, day);

        datePickerDialog.show();
    }

    private void registrarPaciente() {
        String nombreApellido = editNombreApellido.getText().toString().trim();
        String fechaNacimiento = editFechaNacimiento.getText().toString().trim();
        String sexo = editSexo.getText().toString().trim();
        String diagnostico = editDiagnostico.getText().toString().trim();
        String identificacionStr = editIdentificacion.getText().toString().trim();

        if (TextUtils.isEmpty(nombreApellido) || TextUtils.isEmpty(fechaNacimiento) ||
                TextUtils.isEmpty(sexo) || TextUtils.isEmpty(diagnostico) || TextUtils.isEmpty(identificacionStr)) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int identificacion;
        try {
            identificacion = Integer.parseInt(identificacionStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Identificación no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombresApellidos = nombreApellido.split(" ", 2);
        String nombres = nombresApellidos[0];
        String apellidos = nombresApellidos.length > 1 ? nombresApellidos[1] : "";

        managerDB = new ManagerDB(getContext());
        long resultado = managerDB.insertarPaciente(nombres, apellidos, fechaNacimiento, sexo, diagnostico, identificacion);
        Log.d("debugger", "resultadocreacionuser: " + resultado);
        if (resultado != -1) {
            Toast.makeText(getContext(), "Paciente registrado correctamente", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            Integer resultadoConvert = Integer.parseInt(resultado + "");
            mostrarPerfilPaciente(resultadoConvert);
        } else {
            Toast.makeText(getContext(), "Error al registrar paciente", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarPerfilPaciente(int pacienteId) {
        Log.d("debugger", "ID: " + pacienteId);
        PerfilPaciente perfilFragment = PerfilPaciente.newInstance(pacienteId);
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, perfilFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void limpiarCampos() {
        editNombreApellido.setText("");
        editFechaNacimiento.setText("");
        editSexo.setText("");
        editDiagnostico.setText("");
        editIdentificacion.setText("");
    }
}
