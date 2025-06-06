package com.sena.qfinder.ui.medicamento;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sena.qfinder.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Medicamentos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Medicamentos extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Medicamentos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Medicamentos.
     */
    // TODO: Rename and change types and number of parameters
    public static Medicamentos newInstance(String param1, String param2) {
        Medicamentos fragment = new Medicamentos();
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
        View view = inflater.inflate(R.layout.fragment_medicamentos, container, false);

        //
        LinearLayout btnListaMedicamentos = view.findViewById(R.id.btnListaMedicamentos);
        btnListaMedicamentos.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ListaMedicamentos());
            transaction.addToBackStack(null); // Para poder volver atrás
            transaction.commit();
        });

        LinearLayout btnAsignarMedicamentos = view.findViewById(R.id.btnAsignarMedicamentos);
        btnAsignarMedicamentos.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ListaAsignarMedicamentos());
            transaction.addToBackStack(null); // Para poder volver atrás
            transaction.commit();
        });

        // Inflate the layout for this fragment
        return view;
    }

}