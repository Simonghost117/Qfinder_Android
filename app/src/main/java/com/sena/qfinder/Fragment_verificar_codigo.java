package com.sena.qfinder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sena.qfinder.R;

public class Fragment_verificar_codigo extends Fragment {

    public Fragment_verificar_codigo () {
        super(R.layout.fragment_verificar_codigo);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        Button confirmButton = view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            String code = String.format("%s%s%s%s",
                    ((EditText) view.findViewById(R.id.digit1)).getText().toString(),
                    ((EditText) view.findViewById(R.id.digit2)).getText().toString(),
                    ((EditText) view.findViewById(R.id.digit3)).getText().toString(),
                    ((EditText) view.findViewById(R.id.digit4)).getText().toString()
            );

            if (code.length() == 4) {
                // Code validation logic
                Toast.makeText(requireContext(), "Código ingresado: " + code, Toast.LENGTH_SHORT).show();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new Fragment_new_password());
                transaction.commit();
            } else {
                Toast.makeText(requireContext(), "Completa los 4 dígitos", Toast.LENGTH_SHORT).show();
            }
        });

        TextView resendCode = view.findViewById(R.id.resendCodeText);
        resendCode.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
        });
    }
}