package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.sena.qfinder.model.MainActivity;

public class perfil_usuario extends Fragment {

    private SharedPreferences sharedPreferences;

    public perfil_usuario() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false);

        sharedPreferences = requireContext().getSharedPreferences("prefs_qfinder", Context.MODE_PRIVATE);

        LinearLayout ivLogout = view.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(v -> mostrarConfirmacionCerrarSesion());

        return view;
    }

    private void mostrarConfirmacionCerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    cerrarSesion();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void cerrarSesion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
