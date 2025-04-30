package com.sena.qfinder.model;

public class Constantes {
    public static final String NAME_BD = "QfinderAndroid2";
    public static final int NUM_VERSION = 1;


    // Tabla Usuario
    public static final String SENTENCIA_CREAR_USUARIO = "CREATE TABLE usuario (" +
            "nombre_usuario TEXT NOT NULL, " +
            "apellido_usuario TEXT NOT NULL, " +
            "identificacion_usuario TEXT NOT NULL, " +
            "direccion_usuario TEXT NOT NULL, " +
            "telefono_usuario TEXT NOT NULL, " +
            "correo_usuario TEXT NOT NULL UNIQUE, " +
            "contraseña_usuario TEXT NOT NULL" +
            ");";
}
