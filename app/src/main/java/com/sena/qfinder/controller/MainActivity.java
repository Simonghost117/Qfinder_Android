package com.sena.qfinder.controller;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sena.qfinder.R;
import com.sena.qfinder.RegistroUsuario;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegúrate de que activity_main tenga un contenedor (FrameLayout)

        // Carga el Fragment RegistroUsuario directamente
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor_fragmentos, new RegistroUsuario()) // Usa tu fragment aquí
                .commit();
    }
}