package com.sena.qfinder.model;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, Constantes.NAME_BD, null, Constantes.NUM_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constantes.SENTENCIA_CREAR_USUARIO);
        db.execSQL(Constantes.SENTENCIA_TABLA_PACIENTE);
        db.execSQL(Constantes.SENTENCIA_TABLA_COMUNIDAD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar ambas tablas si existen
        db.execSQL("DROP TABLE IF EXISTS Usuario");
        db.execSQL("DROP TABLE IF EXISTS Paciente"); // Asegúrate que el nombre coincida con el usado en la creación

        // Volver a crear las tablas
        onCreate(db);
    }

}