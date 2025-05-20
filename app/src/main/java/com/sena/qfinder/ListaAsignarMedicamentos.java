package com.sena.qfinder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.MedicamentoRequest;
import com.sena.qfinder.models.MedicamentoResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListaAsignarMedicamentos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListaAsignarMedicamentos extends Fragment {

    private Button btnOpenModalAsignar;
    private Calendar startDate, endDate;
    private boolean isSelectingStartDate = true;
    private SimpleDateFormat dateFormatter;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListaAsignarMedicamentos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListaAsignarMedicamentos.
     */
    // TODO: Rename and change types and number of parameters
    public static ListaAsignarMedicamentos newInstance(String param1, String param2) {
        ListaAsignarMedicamentos fragment = new ListaAsignarMedicamentos();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_asignar_medicamentos, container, false);
        btnOpenModalAsignar = view.findViewById(R.id.btnOpenModalAsignar);
        // Inflate the layout for this fragment

        btnOpenModalAsignar.setOnClickListener(view1 -> {
            mostrarDialogoAgregarMedicamento();
        });

        return view;
    }

    private void mostrarDialogoAgregarMedicamento() {
        // Inicializar el formateador de fecha si no está inicializado
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_agregar_medicamento_usuario, null);
        builder.setView(viewInflated);

        // Inicializar componentes
        Spinner spinnerPatients = viewInflated.findViewById(R.id.spinner_patients);
        Spinner spinnerMedications = viewInflated.findViewById(R.id.spinner_medications);
        LinearLayout layoutDatePicker = viewInflated.findViewById(R.id.layout_date_picker);
        TextView tvSelectedDates = viewInflated.findViewById(R.id.tv_selected_dates);
        EditText etDosage = viewInflated.findViewById(R.id.et_dosage);
        EditText etDescription = viewInflated.findViewById(R.id.et_description);
        Button btnSave = viewInflated.findViewById(R.id.btn_save);

        // Configurar Spinners
        setupSpinners(spinnerPatients, spinnerMedications);

        // Configurar DatePicker
        setupDatePicker(layoutDatePicker, tvSelectedDates);

        // Configurar botón Guardar
        btnSave.setOnClickListener(v -> {
            if (validarCampos(spinnerPatients, spinnerMedications, etDosage, tvSelectedDates)) {
                Log.d("misDatos",
                        spinnerPatients.getSelectedItem().toString()+
                        spinnerMedications.getSelectedItem().toString()+
                        etDosage.getText().toString()+
                        etDescription.getText().toString()+
                        tvSelectedDates
                );

                /*
                {
                        "id_paciente": 1,
                        "id_medicamento": 1,
                        "fecha_inicio": "2025-05-20",
                        "fecha_fin": "2025-05-30",
                        "dosis": "5 dosis",
                        "frecuencia": "1440hz"
                    }
                * */
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para configurar los Spinners
    private void setupSpinners(Spinner spinnerPatients, Spinner spinnerMedications) {
        // Datos para pacientes
        String[] patients = {"Jose Carlos", "Luis Guillermo"};
        ArrayAdapter<String> patientsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                patients
        );
        patientsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatients.setAdapter(patientsAdapter);

        // Datos para medicamentos
        String[] medications = {"Silazina 200mg", "Acetaminofen 100mg", "Ibuprofeno 150mg"};
        ArrayAdapter<String> medicationsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                medications
        );
        medicationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedications.setAdapter(medicationsAdapter);
    }

    // Método para configurar el DatePicker
    private void setupDatePicker(LinearLayout layoutDatePicker, TextView tvSelectedDates) {
        layoutDatePicker.setOnClickListener(v -> mostrarSelectorFecha(tvSelectedDates));
    }

    // Método para mostrar el selector de fecha
    private void mostrarSelectorFecha(TextView tvSelectedDates) {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    if (isSelectingStartDate) {
                        startDate = selectedDate;
                        isSelectingStartDate = false;
                        mostrarSelectorFecha(tvSelectedDates); // Mostrar selector para fecha fin
                    } else {
                        endDate = selectedDate;
                        isSelectingStartDate = true;
                        actualizarTextoFechas(tvSelectedDates);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // Método para actualizar el texto de las fechas seleccionadas
    private void actualizarTextoFechas(TextView tvSelectedDates) {
        if (startDate != null && endDate != null) {
            String startDateStr = dateFormatter.format(startDate.getTime());
            String endDateStr = dateFormatter.format(endDate.getTime());
            tvSelectedDates.setText(startDateStr + " - " + endDateStr);
        } else if (startDate != null) {
            String startDateStr = dateFormatter.format(startDate.getTime());
            tvSelectedDates.setText(startDateStr + " - Seleccionar fecha fin");
        }
    }

    // Método para validar los campos
    private boolean validarCampos(Spinner spinnerPatients, Spinner spinnerMedications,
                                  EditText etDosage, TextView tvSelectedDates) {
        if (spinnerPatients.getSelectedItem() == null) {
            showToast("Selecciona un paciente");
            return false;
        }

        if (spinnerMedications.getSelectedItem() == null) {
            showToast("Selecciona un medicamento");
            return false;
        }

        if (startDate == null || endDate == null) {
            showToast("Selecciona las fechas de inicio y fin");
            return false;
        }

        if (etDosage.getText().toString().trim().isEmpty()) {
            showToast("Ingresa la dosis");
            return false;
        }

        return true;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}