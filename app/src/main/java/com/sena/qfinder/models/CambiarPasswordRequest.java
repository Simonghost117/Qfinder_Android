package com.sena.qfinder.models;

public class CambiarPasswordRequest {
    private String correo_usuario;  // nombre debe coincidir con el backend
    private String nuevaContrasena;

    public CambiarPasswordRequest(String correo_usuario, String nuevaContrasena) {
        this.correo_usuario = correo_usuario;
        this.nuevaContrasena = nuevaContrasena;
    }

    // Getters y setters
    public String getCorreo_usuario() {
        return correo_usuario;
    }

    public void setCorreo_usuario(String correo_usuario) {
        this.correo_usuario = correo_usuario;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
}
