package com.sena.qfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.MedicamentoRequest;
import com.sena.qfinder.models.MedicamentoResponse;
import com.sena.qfinder.models.RegisterRequest;
import com.sena.qfinder.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_agregar_medicamento#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_agregar_medicamento extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btnGuardarMedicamento;
    TextInputEditText edtNombreMedicamento;
    TextInputEditText edtDosisMedicamento;
    TextInputEditText edtDescripcion;

    private ProgressDialog progressDialog;

    public fragment_agregar_medicamento() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_agregar_medicamento.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_agregar_medicamento newInstance(String param1, String param2) {
        fragment_agregar_medicamento fragment = new fragment_agregar_medicamento();
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
        View view = inflater.inflate(R.layout.fragment_agregar_medicamento, container, false);

        initViews(view);

        btnGuardarMedicamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = edtNombreMedicamento.getText().toString().trim();
                String tipo = edtDosisMedicamento.getText().toString().trim();
                String descripcion = edtDescripcion.getText().toString().trim();

                Log.d("AgregarMedicamento", "Nombre: " + nombre);
                Log.d("AgregarMedicamento", "Tipo: " + tipo);
                Log.d("AgregarMedicamento", "Descripción: " + descripcion);

                // Crear objeto de solicitud
                MedicamentoRequest request = new MedicamentoRequest(
                        nombre,
                        tipo,
                        descripcion
                );

                Log.d("AgregarMedicamento", "Request Nombre: " + request.getNombre());
                Log.d("AgregarMedicamento", "Request Tipo: " + request.getTipo());
                Log.d("AgregarMedicamento", "Request Descripción: " + request.getDescripcion());

                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", null);
                Toast.makeText(getContext(), "aaaa" + token, Toast.LENGTH_SHORT).show();


                if (token == null || token.isEmpty()) {
                    Toast.makeText(getContext(), "Token no encontrado. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Configurar Retrofit
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://qfinder-production.up.railway.app/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                AuthService authService = retrofit.create(AuthService.class);

                // Realizar la llamada al servidor
                Call<MedicamentoResponse> call = authService.agregarMedicamento("Bearer " + token, request);
                call.enqueue(new Callback<MedicamentoResponse>() {
                    @Override
                    public void onResponse(Call<MedicamentoResponse> call, Response<MedicamentoResponse> response) {

                        Log.d("AgregarMedicamento", "Código de respuesta: " + response.code());

                        if (response.isSuccessful()) {
                            Log.d("AgregarMedicamento", "Respuesta exitosa del servidor: " + response.body().getMessage());
                            Toast.makeText(getContext(), "Se registro el medicamento correctamente", Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("AgregarMedicamento", "Error en la respuesta del servidor: " + response.code());
                            try {
                                Log.d("AgregarMedicamento", "Cuerpo del error: " + response.errorBody().string());
                            } catch (Exception e) {
                                Log.d("AgregarMedicamento", "Error al leer el cuerpo del error: " + e.getMessage());
                            }
                            Toast.makeText(getContext(), "No se registro el medicamento correctamente", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MedicamentoResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("AgregarMedicamento", "Error de conexión: " + t.getMessage());
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void initViews(View view) {
        edtNombreMedicamento = view.findViewById(R.id.edtNombreMedicamento);
        edtDosisMedicamento = view.findViewById(R.id.edtDosisMedicamento);
        edtDescripcion = view.findViewById(R.id.edtDescripcion);
        btnGuardarMedicamento = view.findViewById(R.id.btnGuardarMedicamento);

        // Configurar ProgressDialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Registrando");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);
    }
}