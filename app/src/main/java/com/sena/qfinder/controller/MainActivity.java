package com.sena.qfinder.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sena.qfinder.R;
import com.sena.qfinder.ui.home.DashboardFragment;
// import com.sena.qfinder.ui.home.HomeFragment;
// import com.sena.qfinder.ui.home.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private Button btnHome, btnDashboard, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnHome = findViewById(R.id.btn_home);
        btnDashboard = findViewById(R.id.btn_dashboard);
        btnProfile = findViewById(R.id.btn_profile);

        // Fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        btnHome.setOnClickListener(view -> {
            // loadFragment(new HomeFragment());
        });

        btnDashboard.setOnClickListener(view -> loadFragment(new DashboardFragment()));

        btnProfile.setOnClickListener(view -> {
            // loadFragment(new ProfileFragment());
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
