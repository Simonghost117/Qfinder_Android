package com.sena.qfinder.model;

public class Constantes {

    public static final String NAME_BD = "QfindeR";
    public static final int NUM_VERSION = 1;


    public static final String SENTENCIA_TABLA_PACIENTE = "CREATE TABLE Paciente (" +
            "id INTEGER, " +
            "nombres TEXT, " +
            "apellidos TEXT, " +
            "fechaNacimiento TEXT, " +
            "sexo TEXT, " +
            "diagnostico TEXT, " +
            "identificacion INTEGER UNIQUE" +
            ");";
}
