package com.sena.qfinder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.sena.qfinder.model.DbHelper;

public class ManagerDB {
    private DbHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public ManagerDB(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }

    private void openDBWr() {
        this.db = this.dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }



    public long crearUsuario(String nombres, String apellidos, String identificacion, String direccion, String telefono, String email, String password) {
        openDBWr();
        // Verificar si el correo ya está registrado
        if (correoExiste(email)) {
            Toast.makeText(context, "Este correo ya está registrado, intenta con otro.", Toast.LENGTH_SHORT).show();
            return -1; // Retorna un valor negativo para indicar que la inserción falló
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
        return result;
    }
    public boolean correoExiste(String correo) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT correo_usuario FROM usuario WHERE correo_usuario = ?", new String[]{correo});

        boolean existe = cursor.getCount() > 0;
        cursor.close();

        return existe;
    }




}

