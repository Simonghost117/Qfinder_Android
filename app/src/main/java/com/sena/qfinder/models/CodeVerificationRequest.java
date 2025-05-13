package com.sena.qfinder.models;

public class CodeVerificationRequest {
    private String correo_usuario;
    private String codigo;

    public CodeVerificationRequest(String correo_usuario, String codigo) {
        this.correo_usuario = correo_usuario;
        this.codigo = codigo;
    }
}