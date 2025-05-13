package com.sena.qfinder.controller;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sena.qfinder.Comunidad;
import com.sena.qfinder.Fragment_Serivicios;
import com.sena.qfinder.R;
import com.sena.qfinder.perfil_usuario;
import com.sena.qfinder.ui.home.DashboardFragment;

public class MainActivityDash extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(navListener);

        // Fragmento inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new DashboardFragment();
                }
                else if (itemId == R.id.nav_services) {
                    selectedFragment = new Fragment_Serivicios();
                }
                else if (itemId == R.id.nav_comunidad) {
                    selectedFragment = new Comunidad();
                }
                else if (itemId == R.id.nav_profile) {
                    selectedFragment = new perfil_usuario();
                }

                // } else if (itemId == R.id.nav_community) {
                //     selectedFragment = new CommunityFragment();
                //

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            };
}