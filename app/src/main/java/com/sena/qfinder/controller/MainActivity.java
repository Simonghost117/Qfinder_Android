package com.sena.qfinder.controller;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sena.qfinder.Inicio;
import com.sena.qfinder.R;

public class MainActivity extends AppCompatActivity {

    private boolean isInicioFragmentShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Si es la primera vez que se crea la actividad, se carga el SplashFragment
        if (savedInstanceState == null) {
            loadFragment(new Inicio());
            isInicioFragmentShown = true; // Marcamos que el fragmento de inicio ya se ha mostrado
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Solo cargamos el fragmento de Inicio si no ha sido mostrado anteriormente
        if (!isInicioFragmentShown) {
            loadFragment(new Inicio());
            isInicioFragmentShown = true;
        }
    }

    private void loadFragment(Fragment fragment) {
        // Reemplaza el fragmento en el contenedor del layout
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)  // ID del contenedor de fragmentos
                .commit();
    }
}
