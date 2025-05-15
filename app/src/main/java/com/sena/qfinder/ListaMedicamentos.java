package com.sena.qfinder;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListaMedicamentos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListaMedicamentos extends Fragment {
    private TableLayout tablaMedicamentos;
    private Button btnAgregar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListaMedicamentos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListaMedicamentos.
     */
    // TODO: Rename and change types and number of parameters
    public static ListaMedicamentos newInstance(String param1, String param2) {
        ListaMedicamentos fragment = new ListaMedicamentos();
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
        View view = inflater.inflate(R.layout.fragment_lista_medicamentos, container, false);

        tablaMedicamentos = view.findViewById(R.id.tablaMedicamentos);
        btnAgregar = view.findViewById(R.id.btnAgregar);

        // Datos de prueba
        agregarFila("Silazina", "200mg", "Tomar una pastilla al día");
        agregarFila("Acetaminofén", "100mg", "Una pastilla cada 8 horas");
        agregarFila("Ibuprofeno", "150mg", "Una pastilla cada 8 horas");

        btnAgregar.setOnClickListener(v -> {
            // Aquí puedes abrir un diálogo o nueva pantalla para ingresar datos
            agregarFila("Nuevo", "0mg", "Descripción");
        });
        // Inflate the layout for this fragment
        return view;
    }

    private void agregarFila(String nombre, String dosis, String descripcion) {
        TableRow fila = new TableRow(getContext());

        TextView tvNombre = new TextView(getContext());
        tvNombre.setText(nombre);

        TextView tvDosis = new TextView(getContext());
        tvDosis.setText(dosis);

        TextView tvDescripcion = new TextView(getContext());
        tvDescripcion.setText(descripcion);

        ImageButton btnEliminar = new ImageButton(getContext());
        btnEliminar.setImageResource(android.R.drawable.ic_menu_delete);
        btnEliminar.setBackgroundColor(Color.rgb(255,117,177));
        btnEliminar.setOnClickListener(v -> tablaMedicamentos.removeView(fila));

        fila.addView(tvNombre);
        fila.addView(tvDosis);
        fila.addView(tvDescripcion);
        fila.addView(btnEliminar);

        tablaMedicamentos.addView(fila);
    }
}