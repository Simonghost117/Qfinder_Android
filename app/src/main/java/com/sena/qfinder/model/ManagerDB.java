package com.sena.qfinder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ManagerDB {

    public DbHelper dbHelper;
    public SQLiteDatabase db;

    public ManagerDB(Context context) {
        this.dbHelper = new DbHelper(context);
    }

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

    // Inserta un paciente en la base de datos
    public long insertarPaciente(String nombres, String apellidos, String fechaNacimiento, String sexo, String diagnostico, int identificacion) {
        openWritable();

        // Recupera el valor máximo de 'id' para incrementar el próximo
        String query = "SELECT MAX(id) FROM Paciente";
        Cursor cursor = db.rawQuery(query, null);

        int id = 1; // Valor por defecto en caso de que no haya pacientes en la base de datos
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0) + 1;  // Incrementa el valor del id
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("id", id);  // Asigna manualmente el id
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
        ArrayList<HashMap<String, String>> pacientaesLista = new ArrayList<>();
        String selectQuery = "SELECT * FROM Paciente";
        Cursor cursor = db.rawQuery(selectQuery, null);

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
                pacientaesLista.add(paciente);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return pacientaesLista;
    }

    // Obtener un paciente por su ID
    public HashMap<String, String> obtenerPaciente(int id) {
        openReadable();
        HashMap<String, String> paciente = null; // Inicializa paciente como null
        Cursor cursor = db.query("Paciente", new String[]{"id", "nombres", "apellidos", "fechaNacimiento", "sexo", "diagnostico", "identificacion"}, "id" + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            paciente = new HashMap<>();
            paciente.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
            paciente.put("nombres", cursor.getString(cursor.getColumnIndexOrThrow("nombres")));
            paciente.put("apellidos", cursor.getString(cursor.getColumnIndexOrThrow("apellidos")));
            paciente.put("fechaNacimiento", cursor.getString(cursor.getColumnIndexOrThrow("fechaNacimiento")));
            paciente.put("sexo", cursor.getString(cursor.getColumnIndexOrThrow("sexo")));
            paciente.put("diagnostico", cursor.getString(cursor.getColumnIndexOrThrow("diagnostico")));
            paciente.put("identificacion", cursor.getString(cursor.getColumnIndexOrThrow("identificacion")));
            Log.d("debugger", "CursorPaciente: " + paciente);
        } else {
            Log.d("debugger", "No se encontró paciente con ID: " + id);
        }

        if (cursor != null) {
            cursor.close();
        }
        close();
        return paciente;
    }
}

