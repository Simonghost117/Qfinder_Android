package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.sena.qfinder.RegistroUsuario;
import com.sena.qfinder.controller.MainActivityDash;
import com.sena.qfinder.model.ManagerDB;

public class Login extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private ManagerDB managerDB;
    private SharedPreferences sharedPreferences;

    public Login() {}

    public static Login newInstance() {
        return new Login();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        managerDB = new ManagerDB(requireContext());
        sharedPreferences = requireContext().getSharedPreferences("prefs_qfinder", Context.MODE_PRIVATE);

        initializeViews(view);
        setupClickListeners(view); // Se pasa la vista como parámetro
    }

    private void initializeViews(View view) {
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        Button loginButton = view.findViewById(R.id.loginButton);
        TextView registerLink = view.findViewById(R.id.registerLink);
        TextView forgotPassword = view.findViewById(R.id.forgotPassword);
    }

    // Método modificado para recibir la vista como parámetro
    private void setupClickListeners(View view) {
        view.findViewById(R.id.loginButton).setOnClickListener(v -> validarYLogin());
        view.findViewById(R.id.registerLink).setOnClickListener(v -> navegarARegistro());
        view.findViewById(R.id.forgotPassword).setOnClickListener(v -> mostrarToast("Función en desarrollo"));
    }

    private void validarYLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validarCampos(email, password)) {
            if (validarCredenciales(email, password)) {
                guardarEmailUsuario(email);
                iniciarSesionExitoso();
            } else {
                mostrarToast("Credenciales incorrectas");
            }
        }
    }

    private boolean validarCampos(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            mostrarToast("Todos los campos son obligatorios");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarToast("Formato de email inválido");
            return false;
        }

        return true;
    }

    private boolean validarCredenciales(String email, String password) {
        managerDB.openReadable();
        boolean credencialesValidas = managerDB.validarUsuario(email, password);
        managerDB.close();
        return credencialesValidas;
    }

    private void guardarEmailUsuario(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email_usuario", email);
        editor.apply();
    }

    private void iniciarSesionExitoso() {
        mostrarToast("Bienvenido!");
        Intent intent = new Intent(requireContext(), MainActivityDash.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void navegarARegistro() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new RegistroUsuario());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}