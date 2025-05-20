package com.sena.qfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class episodios_salud_nota extends AppCompatActivity {

    private String nivelGravedadActual = "Baja"; // Estado inicial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_episodios_salud_nota);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón gravedad
        Button btnGravedad = findViewById(R.id.gravedad);
        final int[] estadoGravedad = {0}; // 0: baja, 1: media, 2: alta

        btnGravedad.setText("Baja");
        btnGravedad.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.baja));
        nivelGravedadActual = "Baja";

        btnGravedad.setOnClickListener(v -> {
            switch (estadoGravedad[0]) {
                case 0:
                    btnGravedad.setText("Media");
                    btnGravedad.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.media));
                    nivelGravedadActual = "Media";
                    estadoGravedad[0] = 1;
                    break;
                case 1:
                    btnGravedad.setText("Alta");
                    btnGravedad.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.alta));
                    nivelGravedadActual = "Alta";
                    estadoGravedad[0] = 2;
                    break;
                case 2:
                    btnGravedad.setText("Baja");
                    btnGravedad.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.baja));
                    nivelGravedadActual = "Baja";
                    estadoGravedad[0] = 0;
                    break;
            }
        });

        // Botón volver
        ImageView btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(episodios_salud_nota.this, episodios_salud_menu.class);
            startActivity(intent);
        });

        // Spinner para tamaño de letra
        Spinner spinnerTamano = findViewById(R.id.spinner_tamano_letra);
        EditText editTextNota = findViewById(R.id.editTextNota);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.Tamaño_de_letra,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTamano.setAdapter(adapter);

        spinnerTamano.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tamanoStr = parent.getItemAtPosition(position).toString();
                float tamano = Float.parseFloat(tamanoStr);
                editTextNota.setTextSize(tamano);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    public String getNivelGravedadActual() {
        return nivelGravedadActual;
    }
}
