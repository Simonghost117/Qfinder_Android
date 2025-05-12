package com.sena.qfinder.model;

public class Constantes {
    // Nombre y versión de la base de datos (usa un solo nombre)
    public static final String NAME_BD = "QfinderAndroid2";
    public static final int NUM_VERSION = 1;

    // Sentencias SQL para crear tablas
    public static final String SENTENCIA_CREAR_USUARIO =
            "CREATE TABLE usuario (" +
                    "nombre_usuario TEXT NOT NULL, " +
                    "apellido_usuario TEXT NOT NULL, " +
                    "identificacion_usuario TEXT NOT NULL, " +
                    "direccion_usuario TEXT NOT NULL, " +
                    "telefono_usuario TEXT NOT NULL, " +
                    "correo_usuario TEXT NOT NULL UNIQUE, " +
                    "contraseña_usuario TEXT NOT NULL" +
                    ");";

    public static final String SENTENCIA_TABLA_PACIENTE =
            "CREATE TABLE Paciente (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombres TEXT, " +
                    "apellidos TEXT, " +
                    "fechaNacimiento TEXT, " +
                    "sexo TEXT, " +
                    "diagnostico TEXT, " +
                    "identificacion INTEGER UNIQUE" +
                    ");";

    public static final String SENTENCIA_TABLA_COMUNIDAD =
            "CREATE TABLE mensaje (" +
                    "id INTEGER, " +
                    "nombre_usuario TEXT, " +
                    "contenido TEXT, " +
                    "hora TEXT, " +
                    "comunidad TEXT);";

}