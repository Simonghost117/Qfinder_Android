package com.sena.qfinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.sena.qfinder.api.AuthService;
import com.sena.qfinder.models.CodeVerificationRequest;
import com.sena.qfinder.models.CodeVerificationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VerficacionCorreo extends Fragment {

    private static final String ARG_EMAIL = "correo_usuario";
    private String correoUsuario;

    public VerficacionCorreo() {
    }

    public static VerficacionCorreo newInstance(String correo) {
        VerficacionCorreo fragment = new VerficacionCorreo();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, correo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            correoUsuario = getArguments().getString(ARG_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.verificacion_correo, container, false);

        EditText[] digits = {
                view.findViewById(R.id.digit1),
                view.findViewById(R.id.digit2),
                view.findViewById(R.id.digit3),
                view.findViewById(R.id.digit4),
                view.findViewById(R.id.digit5)
        };

        Button confirmButton = view.findViewById(R.id.confirmButton);

        // Setup auto-focus for each digit input
        for (int i = 0; i < digits.length - 1; i++) {
            setupAutoFocus(digits[i], digits[i + 1]);
        }

        confirmButton.setOnClickListener(v -> {
            StringBuilder codeBuilder = new StringBuilder();
            for (EditText digit : digits) {
                codeBuilder.append(digit.getText().toString().trim());
            }

            String codeEntered = codeBuilder.toString();
            if (codeEntered.length() < 5) {
                Toast.makeText(getContext(), "Por favor completa los 5 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            verificarCodigoConBackend(correoUsuario, codeEntered);
        });

        return view;
    }

    private void setupAutoFocus(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    next.requestFocus();
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void verificarCodigoConBackend(String correo, String codigo) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://qfinder-production.up.railway.app/")  // Asegúrate de que la URL esté correcta
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthService service = retrofit.create(AuthService.class);
        CodeVerificationRequest request = new CodeVerificationRequest(correo, codigo);

        service.verificarCodigo(request).enqueue(new Callback<CodeVerificationResponse>() {
            @Override
            public void onResponse(Call<CodeVerificationResponse> call, Response<CodeVerificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();

                    // Aquí, puedes manejar los diferentes mensajes del servidor
                    if (message.contains("Código incorrecto")) {
                        // Mostrar mensaje si el código es incorrecto
                        Toast.makeText(getContext(), "Código incorrecto, por favor verifica e intenta de nuevo.", Toast.LENGTH_SHORT).show();
                    } else if (message.contains("El código ha expirado")) {
                        // Mostrar mensaje si el código ha expirado
                        Toast.makeText(getContext(), "El código ha expirado. Solicita uno nuevo.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Si todo está bien
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        // Aquí puedes navegar a otro fragmento o actividad si la verificación fue exitosa
                    }
                } else {
                    // Si la respuesta del servidor no fue exitosa
                    Toast.makeText(getContext(), "Código incorrecto o error en la verificación", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CodeVerificationResponse> call, Throwable t) {
                // Si falla la conexión con el servidor
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
