package com.sena.qfinder.models;

public class RegisterRequest {
    private String nombre;
    private String correo;
    private String password;

    public RegisterRequest(String nombre, String correo, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
    }
}
