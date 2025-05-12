package com.sena.qfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Fragment_new_password extends Fragment {

    public Fragment_new_password() {
        super(R.layout.fragment_new_password);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        EditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        Button confirmButton = view.findViewById(R.id.confirmButton);
        ImageView backButton = view.findViewById(R.id.backButton);

        // Configurar listeners
        confirmButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (validatePasswords(newPassword, confirmPassword)) {
                // Lógica para cambiar la contraseña
                Toast.makeText(requireContext(), "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();

                // Navegar al fragment de inicio de sesión o donde corresponda
                navigateToLogin();
            }
        });

        backButton.setOnClickListener(v -> {
            // Regresar al fragment anterior
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, complete ambos campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void navigateToLogin() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new Login()); // Asegúrate de tener este Fragment
        transaction.addToBackStack(null); // Opcional: permite volver atrás con el botón "Back"
        transaction.commit();
    }
}