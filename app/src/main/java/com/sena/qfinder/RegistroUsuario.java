package com.sena.qfinder;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
    private Button btnEnviar;

    public RegistroUsuario() {
        // Constructor público vacío requerido
    }

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
        edtContrasena = view.findViewById(R.id.edtContrasena);

        btnEnviar = view.findViewById(R.id.btnEnviar);


        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarUsuario();
            }
        });

        return view;
    }

    private void registrarUsuario() {
        // Validación de campos obligatorios
        if (edtNombre.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            edtNombre.requestFocus();
            return;
        }

        if (edtApellido.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El apellido es obligatorio", Toast.LENGTH_SHORT).show();
            edtApellido.requestFocus();
            return;
        }

        if (edtCorreo.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El correo es obligatorio", Toast.LENGTH_SHORT).show();
            edtCorreo.requestFocus();
            return;
        }

// Verificar si el correo ya está registrado
        if (managerDB.correoExiste(edtCorreo.getText().toString())) {
            Toast.makeText(getContext(), "Este correo ya está registrado, intenta con otro.", Toast.LENGTH_SHORT).show();
            edtCorreo.requestFocus();
            return;
        }



        if (edtIdentificacion.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "La identificacion es obligatoria", Toast.LENGTH_SHORT).show();
            edtIdentificacion.requestFocus();
            return;
        }
        if (edtDirrecion.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "La direccion es obligatoria", Toast.LENGTH_SHORT).show();
            edtDirrecion.requestFocus();
            return;
        }

        if (edtTelefono.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            edtTelefono.requestFocus();
            return;
        }


        if (edtContrasena.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "La contraseña es obligatoria", Toast.LENGTH_SHORT).show();
            edtContrasena.requestFocus();
            return;
        }

        // Insertar datos en la base de datos
        long result = managerDB.crearUsuario(
                edtNombre.getText().toString(),
                edtApellido.getText().toString(),
                edtCorreo.getText().toString(),
                edtIdentificacion.getText().toString(),
                edtDirrecion.getText().toString(),
                edtTelefono.getText().toString(),
                edtContrasena.getText().toString()
        );

        if (result < 0) {
            Toast.makeText(getContext(), "Hubieron fallos en el registro del usuario", Toast.LENGTH_SHORT).show();
        } else {
            // Guardar los datos en SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserData", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("nombre", edtNombre.getText().toString());
            editor.putString("apellido", edtApellido.getText().toString());
            editor.putString("correo", edtCorreo.getText().toString());
            editor.putString("identificacion", edtIdentificacion.getText().toString());
            editor.putString("direccion", edtDirrecion.getText().toString());
            editor.putString("telefono", edtTelefono.getText().toString());
            editor.putString("contrasena", edtContrasena.getText().toString());
            editor.apply();

            Toast.makeText(getContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();


        }
    }}