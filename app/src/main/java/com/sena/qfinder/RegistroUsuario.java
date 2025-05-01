package com.sena.qfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.sena.qfinder.model.ManagerDB;


public class RegistroUsuario extends Fragment {
    private ManagerDB managerDB;
    private TextInputEditText edtNombre, edtApellido, edtCorreo, edtIdentificacion, edtDirrecion, edtTelefono, edtContrasena;
    private Button btnContinuar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_registro_usuario, container, false);


        // Inicializar la base de datos
        managerDB = new ManagerDB(requireContext());

        // Asignación de elementos UI
        edtNombre = view.findViewById(R.id.edtNombre);
        edtApellido = view.findViewById(R.id.edtApellido);
        edtCorreo = view.findViewById(R.id.edtCorreo);
        edtIdentificacion = view.findViewById(R.id.edtIdentificacion);
        edtDirrecion = view.findViewById(R.id.edtDireccion);
        edtTelefono = view.findViewById(R.id.edtTelefono);

        //edtContrasena = view.findViewById(R.id.edtContrasena);

        btnContinuar = view.findViewById(R.id.btnContinuar);
        //btnContinuar = view.findViewById(R.id.btnContinuar);

        btnContinuar.setOnClickListener(v -> {
            if (validarCampos()) {
                // Guardar datos en Bundle para pasarlos al siguiente Fragment
                Bundle args = new Bundle();
                args.putString("nombre", edtNombre.getText().toString());
                args.putString("apellido", edtApellido.getText().toString());
                args.putString("identificacion", edtIdentificacion.getText().toString());
                args.putString("direccion", edtDirrecion.getText().toString());
                args.putString("telefono", edtTelefono.getText().toString());
                args.putString("correo", edtCorreo.getText().toString());


                // Navegar al Fragment de confirmación de contraseña
                Fragment fragment = new ConfirmacionContrasena();
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment) // Usa el mismo ID que en activity_main
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private boolean validarCampos() {
        // Validación de campos obligatorios
        if (edtNombre.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            edtNombre.requestFocus();
            return false;
        }

        if (edtApellido.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El apellido es obligatorio", Toast.LENGTH_SHORT).show();
            edtApellido.requestFocus();
            return false;
        }

        if (edtCorreo.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El correo es obligatorio", Toast.LENGTH_SHORT).show();
            edtCorreo.requestFocus();
            return false;
        }
// Verificar si el correo ya está registrado
        if (managerDB.correoExiste(edtCorreo.getText().toString())) {
            Toast.makeText(getContext(), "Este correo ya está registrado, intenta con otro.", Toast.LENGTH_SHORT).show();
            edtCorreo.requestFocus();
            return false;
        }

        if (edtIdentificacion.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "La identificacion es obligatoria", Toast.LENGTH_SHORT).show();
            edtIdentificacion.requestFocus();
            return false;
        }
        if (edtDirrecion.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "La direccion es obligatoria", Toast.LENGTH_SHORT).show();
            edtDirrecion.requestFocus();
            return false;
        }

        if (edtTelefono.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            edtTelefono.requestFocus();
            return false;
        }
        return true;
    }
}
