package com.sena.qfinder.ui.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sena.qfinder.R;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordatoriosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordatoriosFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RecordatoriosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordatoriosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordatoriosFragment newInstance(String param1, String param2) {
        RecordatoriosFragment fragment = new RecordatoriosFragment();
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
        // 1. Inflar el layout XML
        View view = inflater.inflate(R.layout.fragment_recordatorios, container, false);

        // 2. Obtener referencias a los elementos
        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);

        // Puedes configurar el calendario aquí si quieres (opcional)
        calendarView.setSelectedDate(CalendarDay.today());

        // 3. Aquí puedes hacer lo mismo con otros elementos como:
        // TextView citasView = view.findViewById(R.id.textCitas);
        // Button btnAgregar = view.findViewById(R.id.btnAgregar);

        return view;
    }}
