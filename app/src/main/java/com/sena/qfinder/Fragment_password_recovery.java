package com.sena.qfinder;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Fragment_password_recovery extends Fragment {

    private EditText edtEmail;
    private Button btnSend;

    public void PasswordRecoveryFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla el layout de este fragment
        return inflater.inflate(R.layout.fragment_password_recovery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        edtEmail = view.findViewById(R.id.emailEditText);
        btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(getContext(), "Ingresa un coreo electronico valido", Toast.LENGTH_SHORT).show();

                } else {
                    // Aquí puedes agregar tu lógica para enviar el código
                    Toast.makeText(getContext(), "Código enviado a " + email, Toast.LENGTH_SHORT).show();

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new Fragment_verificar_codigo());
                    transaction.addToBackStack(null); // Opcional: permite volver atrás con el botón "Back"
                    transaction.commit();
                }
            }
        });
    }
}
