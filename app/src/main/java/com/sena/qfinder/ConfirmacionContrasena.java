package com.sena.qfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.sena.qfinder.controller.MainActivity;
import com.sena.qfinder.model.ManagerDB;

public class ConfirmacionContrasena extends Fragment {
    private TextInputEditText edtContrasena, edtConfirmarContrasena;
    private Button btnFinalizarRegistro;
    private ManagerDB managerDB;
    private CheckBox checkTerminos;
    private TextView txtTerminos;
    private CardView btnVolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmacion_contrasena, container, false);

        managerDB = new ManagerDB(requireContext());

        edtContrasena = view.findViewById(R.id.edtContrasena);
        edtConfirmarContrasena = view.findViewById(R.id.edtConfContrasena);
        checkTerminos = view.findViewById(R.id.checkTerminos);
        txtTerminos = view.findViewById(R.id.txtTerminos);
        btnFinalizarRegistro = view.findViewById(R.id.btnEnviar);
        btnVolver = view.findViewById(R.id.btnVolver);

        configurarTerminosClickable();

        btnFinalizarRegistro.setOnClickListener(v -> finalizarRegistro());
        btnVolver.setOnClickListener(v -> volverARegistroUsuario());
        return view;
    }

    private void volverARegistroUsuario() {
        // Regresar al fragmento anterior (RegistroUsuario)
        getParentFragmentManager().popBackStack();
    }
    private void configurarTerminosClickable() {
        String textoTerminos = "Acepto los Términos y Condiciones";
        SpannableString spannableString = new SpannableString(textoTerminos);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                mostrarTerminosCompletos();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(requireContext(), R.color.azul_link));
                ds.setUnderlineText(true);
            }
        };

        int inicio = textoTerminos.indexOf("Términos y Condiciones");
        int fin = inicio + "Términos y Condiciones".length();
        spannableString.setSpan(clickableSpan, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtTerminos.setText(spannableString);
        txtTerminos.setMovementMethod(LinkMovementMethod.getInstance());
        txtTerminos.setHighlightColor(Color.TRANSPARENT);
    }

    private void mostrarTerminosCompletos() {
        Fragment fragment = new TerminosCondiciones();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.contenedor_fragmentos, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void finalizarRegistro() {
        String contrasena = edtContrasena.getText().toString();
        String confirmacion = edtConfirmarContrasena.getText().toString();

        if (!contrasena.equals(confirmacion)) {
            Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasena.length() < 6) {
            Toast.makeText(getContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkTerminos.isChecked()) {
            Toast.makeText(getContext(), "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = getArguments();
        if (args != null) {
            long result = managerDB.crearUsuario(
                    args.getString("nombre"),
                    args.getString("apellido"),
                    args.getString("identificacion"),
                    args.getString("direccion"),
                    args.getString("telefono"),
                    args.getString("correo"),
                    contrasena
            );

            if (result > 0) {
                guardarEnSharedPreferences(args, contrasena);
                Toast.makeText(getContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                redirigirALogin();
            }
        }
    }

    private void guardarEnSharedPreferences(Bundle args, String contrasena) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nombre", args.getString("nombre"));
        editor.putString("apellido", args.getString("apellido"));
        editor.putString("correo", args.getString("correo"));
        editor.putString("identificacion", args.getString("identificacion"));
        editor.putString("direccion", args.getString("direccion"));
        editor.putString("telefono", args.getString("telefono"));
        editor.putString("contrasena", contrasena);
        editor.apply();
    }

    private void redirigirALogin() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        requireActivity().finish();
    }
}