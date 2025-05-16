package com.sena.qfinder;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sena.qfinder.api.ApiClient;
import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.CambiarPasswordRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_new_password extends Fragment {

    private ImageView backButton;
    private String email;
    private static final String TAG = "PASSWORD_RESET";

    public Fragment_new_password() {
        super(R.layout.fragment_new_password);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            email = getArguments().getString("email");
            Log.d(TAG, "Email recibido: " + email);

            String cookies = ApiClient.getCookieJar().getCookies("qfinder-production.up.railway.app");
            Log.d(TAG, "Cookies encontradas: " + cookies);

            String[] parts = cookies.split("; ");
            for (String part : parts) {
                if (part.startsWith("resetToken=")) {
                    Log.d(TAG, "Parte de cookie: " + part);
                    String token = part.substring("resetToken=".length());
                    ApiClient.setAuthToken(token);
                    Log.d(TAG, "Token JWT extraído y establecido: " + token);
                    break;
                }
            }
        }

        EditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        Button confirmButton = view.findViewById(R.id.confirmButton);
        backButton = view.findViewById(R.id.backButton);

        confirmButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón confirmar presionado");
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (validatePasswords(newPassword, confirmPassword)) {
                cambiarContrasena(newPassword);
            }
        });

        backButton.setOnClickListener(v -> navigateBackToCodeVerification());
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

    private void cambiarContrasena(String nuevaContrasena) {
        Log.d(TAG, "Llamando a cambiarPassword con email: " + email + " y nueva contraseña");

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Cambiando contraseña...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        AuthService authService = ApiClient.getClient().create(AuthService.class);
        CambiarPasswordRequest request = new CambiarPasswordRequest(email, nuevaContrasena);

        Call<Void> call = authService.cambiarPassword(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();
                Log.d(TAG, "Respuesta HTTP recibida: " + response.code());
                if (response.isSuccessful()) {
                    handlePasswordChangeSuccess();
                } else {
                    handlePasswordChangeError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error en la llamada", t);
            }
        });
    }

    private void handlePasswordChangeSuccess() {
        Toast.makeText(getContext(), "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new Login());
        transaction.commit();
    }

    private void handlePasswordChangeError(Response<Void> response) {
        String errorMessage = "Error al cambiar la contraseña";
        Log.e(TAG, "Error al cambiar contraseña. Código HTTP: " + response.code());

        try {
            if (response.code() == 401) {
                errorMessage = "Sesión expirada. Por favor solicita un nuevo código de verificación.";
                Log.w(TAG, "Sesión expirada, redirigiendo");
                navigateBackToCodeVerification();
            } else if (response.errorBody() != null) {
                errorMessage = response.errorBody().string();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al leer mensaje de error", e);
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void navigateBackToCodeVerification() {
        Log.d(TAG, "Navegando a Fragment_verificar_codigo");

        Bundle bundle = new Bundle();
        bundle.putString("email", email);

        Fragment_verificar_codigo fragment = new Fragment_verificar_codigo();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
