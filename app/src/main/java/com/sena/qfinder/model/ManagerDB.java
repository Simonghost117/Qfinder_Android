package com.sena.qfinder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

public class ManagerDB {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    public ManagerDB(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }

    // Métodos para apertura/cierre de conexión
    public void openWritable() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
    }

    public void openReadable() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
    public HashMap<String, String> obtenerUsuarioPorEmail(String email) {
        openReadable();
        HashMap<String, String> usuario = new HashMap<>();

        Cursor cursor = db.rawQuery(
                "SELECT nombre_usuario, apellido_usuario FROM usuario WHERE correo_usuario = ?",
                new String[]{email}
        );

        if (cursor.moveToFirst()) {
            usuario.put("nombre", cursor.getString(0));
            usuario.put("apellido", cursor.getString(1));
        }

        cursor.close();
        close();
        return usuario;
    }
    // ==================== OPERACIONES PARA USUARIO ====================
    public long crearUsuario(String nombres, String apellidos, String identificacion,
                             String direccion, String telefono, String email, String password) {
        openWritable();

        if (correoExiste(email)) {
            Toast.makeText(context, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
            return -1;
        }

        ContentValues valores = new ContentValues();
        valores.put("nombre_usuario", nombres);
        valores.put("apellido_usuario", apellidos);
        valores.put("identificacion_usuario", identificacion);
        valores.put("direccion_usuario", direccion);
        valores.put("telefono_usuario", telefono);
        valores.put("correo_usuario", email);
        valores.put("contraseña_usuario", password);

        long result = db.insert("usuario", null, valores);
        close();
        return result;
    }

    public boolean correoExiste(String correo) {
        openReadable();
        Cursor cursor = db.rawQuery(
                "SELECT correo_usuario FROM usuario WHERE correo_usuario = ?",
                new String[]{correo}
        );

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    // ==================== OPERACIONES PARA PACIENTE ====================
    public long insertarPaciente(String nombres, String apellidos, String fechaNacimiento,
                                 String sexo, String diagnostico, int identificacion) {
        openWritable();

        // Generar ID autoincremental
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM Paciente", null);
        int id = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            id = cursor.getInt(0) + 1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("nombres", nombres);
        values.put("apellidos", apellidos);
        values.put("fechaNacimiento", fechaNacimiento);
        values.put("sexo", sexo);
        values.put("diagnostico", diagnostico);
        values.put("identificacion", identificacion);

        long resultado = db.insert("Paciente", null, values);
        close();
        return resultado;
    }


    public ArrayList<HashMap<String, String>> obtenerPacientes() {
        openReadable();
        ArrayList<HashMap<String, String>> pacientesLista = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM Paciente", null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> paciente = new HashMap<>();
                paciente.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                paciente.put("nombres", cursor.getString(cursor.getColumnIndexOrThrow("nombres")));
                paciente.put("apellidos", cursor.getString(cursor.getColumnIndexOrThrow("apellidos")));
                paciente.put("fechaNacimiento", cursor.getString(cursor.getColumnIndexOrThrow("fechaNacimiento")));
                paciente.put("sexo", cursor.getString(cursor.getColumnIndexOrThrow("sexo")));
                paciente.put("diagnostico", cursor.getString(cursor.getColumnIndexOrThrow("diagnostico")));
                paciente.put("identificacion", cursor.getString(cursor.getColumnIndexOrThrow("identificacion")));
                pacientesLista.add(paciente);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return pacientesLista;
    }
    // Dentro de tu clase ManagerDB
    public boolean validarUsuario(String email, String password) {
        openReadable();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM usuario WHERE correo_usuario = ? AND contraseña_usuario = ?",
                new String[]{email, password}
        );

        boolean credencialesValidas = cursor.getCount() > 0;
        cursor.close();
        close();

        return credencialesValidas;
    }
    public HashMap<String, String> obtenerPaciente(int id) {
        openReadable();
        HashMap<String, String> paciente = null;
        Cursor cursor = db.query(
                "Paciente",
                new String[]{"id", "nombres", "apellidos", "fechaNacimiento", "sexo", "diagnostico", "identificacion"},
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            paciente = new HashMap<>();
            paciente.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
            paciente.put("nombres", cursor.getString(cursor.getColumnIndexOrThrow("nombres")));
            paciente.put("apellidos", cursor.getString(cursor.getColumnIndexOrThrow("apellidos")));
            paciente.put("fechaNacimiento", cursor.getString(cursor.getColumnIndexOrThrow("fechaNacimiento")));
            paciente.put("sexo", cursor.getString(cursor.getColumnIndexOrThrow("sexo")));
            paciente.put("diagnostico", cursor.getString(cursor.getColumnIndexOrThrow("diagnostico")));
            paciente.put("identificacion", cursor.getString(cursor.getColumnIndexOrThrow("identificacion")));
        }
        if (cursor != null) cursor.close();
        close();
        return paciente;
    }
}