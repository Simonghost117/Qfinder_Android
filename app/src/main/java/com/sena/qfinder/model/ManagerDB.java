package com.sena.qfinder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
}

