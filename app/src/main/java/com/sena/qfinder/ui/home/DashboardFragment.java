package com.sena.qfinder.ui.home;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sena.qfinder.R;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla el layout
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
}
